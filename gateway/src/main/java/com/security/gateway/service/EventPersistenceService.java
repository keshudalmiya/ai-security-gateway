package com.security.gateway.service;

import com.security.gateway.domain.Decision;
import com.security.gateway.domain.RequestEvent;
import com.security.gateway.domain.RiskLevel;
import com.security.gateway.dto.MetricsSummaryResponse;
import com.security.gateway.repository.RequestEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Service
public class EventPersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(EventPersistenceService.class);
    private static final int IN_MEMORY_CAPACITY = 1000;

    private final RequestEventRepository repository;

    // Resilient in-memory circular buffer for offline fallback
    private final ConcurrentLinkedDeque<RequestEvent> memoryBuffer = new ConcurrentLinkedDeque<>();
    private final Map<String, RequestEvent> memoryIndex = new ConcurrentHashMap<>();

    private volatile boolean mongoOperational = true;

    public EventPersistenceService(RequestEventRepository repository) {
        this.repository = repository;
    }

    public RequestEvent save(RequestEvent event) {
        // Always store in in-memory ring buffer for fast fallback
        storeInMemory(event);

        try {
            RequestEvent saved = repository.save(event);
            mongoOperational = true;
            return saved;
        } catch (Exception e) {
            if (mongoOperational) {
                logger.warn("MongoDB connection failed or unavailable ({}). Gracefully falling back to in-memory buffer.", e.getMessage());
                mongoOperational = false;
            }
            return event;
        }
    }

    public Optional<RequestEvent> findByRequestId(String requestId) {
        if (mongoOperational) {
            try {
                Optional<RequestEvent> result = repository.findByRequestId(requestId);
                if (result.isPresent()) return result;
            } catch (Exception e) {
                mongoOperational = false;
            }
        }
        return Optional.ofNullable(memoryIndex.get(requestId));
    }

    public Page<RequestEvent> findFiltered(Decision decision, RiskLevel riskLevel, String method, String query, Pageable pageable) {
        List<RequestEvent> sourceList;

        if (mongoOperational) {
            try {
                Page<RequestEvent> page;
                if (decision != null) {
                    page = repository.findByDecisionOrderByCreatedAtDesc(decision, pageable);
                } else if (riskLevel != null) {
                    page = repository.findByRiskLevelOrderByCreatedAtDesc(riskLevel, pageable);
                } else {
                    page = repository.findAllByOrderByCreatedAtDesc(pageable);
                }
                return page;
            } catch (Exception e) {
                mongoOperational = false;
            }
        }

        // In-memory fallback filtering
        sourceList = new ArrayList<>(memoryBuffer);
        return filterInMemory(sourceList, decision, riskLevel, method, query, pageable);
    }

    public MetricsSummaryResponse getMetricsSummary(String mlServiceStatus) {
        MetricsSummaryResponse summary = new MetricsSummaryResponse();
        summary.setMlServiceStatus(mlServiceStatus != null ? mlServiceStatus : "OK");

        if (mongoOperational) {
            try {
                long total = repository.count();
                long allowed = repository.countByDecision(Decision.ALLOW);
                long flagged = repository.countByDecision(Decision.FLAG);
                long blocked = repository.countByDecision(Decision.BLOCK);

                summary.setTotalRequests(total);
                summary.setAllowed(allowed);
                summary.setFlagged(flagged);
                summary.setBlocked(blocked);

                Map<String, Long> dist = new HashMap<>();
                dist.put("LOW", repository.countByRiskLevel(RiskLevel.LOW));
                dist.put("MEDIUM", repository.countByRiskLevel(RiskLevel.MEDIUM));
                dist.put("HIGH", repository.countByRiskLevel(RiskLevel.HIGH));
                summary.setRiskDistribution(dist);

                summary.setTimeSeries(computeTimeSeries(new ArrayList<>(memoryBuffer)));
                return summary;
            } catch (Exception e) {
                mongoOperational = false;
            }
        }

        // In-memory metrics fallback
        long total = memoryBuffer.size();
        long allowed = memoryBuffer.stream().filter(e -> e.getDecision() == Decision.ALLOW).count();
        long flagged = memoryBuffer.stream().filter(e -> e.getDecision() == Decision.FLAG).count();
        long blocked = memoryBuffer.stream().filter(e -> e.getDecision() == Decision.BLOCK).count();

        summary.setTotalRequests(total);
        summary.setAllowed(allowed);
        summary.setFlagged(flagged);
        summary.setBlocked(blocked);

        Map<String, Long> dist = new HashMap<>();
        dist.put("LOW", memoryBuffer.stream().filter(e -> e.getRiskLevel() == RiskLevel.LOW).count());
        dist.put("MEDIUM", memoryBuffer.stream().filter(e -> e.getRiskLevel() == RiskLevel.MEDIUM).count());
        dist.put("HIGH", memoryBuffer.stream().filter(e -> e.getRiskLevel() == RiskLevel.HIGH).count());
        summary.setRiskDistribution(dist);

        summary.setTimeSeries(computeTimeSeries(new ArrayList<>(memoryBuffer)));
        return summary;
    }

    public boolean isMongoOperational() {
        return mongoOperational;
    }

    private void storeInMemory(RequestEvent event) {
        memoryBuffer.addFirst(event);
        memoryIndex.put(event.getRequestId(), event);
        while (memoryBuffer.size() > IN_MEMORY_CAPACITY) {
            RequestEvent removed = memoryBuffer.removeLast();
            if (removed != null) {
                memoryIndex.remove(removed.getRequestId());
            }
        }
    }

    private Page<RequestEvent> filterInMemory(List<RequestEvent> list, Decision decision, RiskLevel riskLevel,
                                              String method, String query, Pageable pageable) {
        List<RequestEvent> filtered = list.stream()
                .filter(e -> decision == null || e.getDecision() == decision)
                .filter(e -> riskLevel == null || e.getRiskLevel() == riskLevel)
                .filter(e -> method == null || method.equalsIgnoreCase(e.getMethod()))
                .filter(e -> {
                    if (query == null || query.isBlank()) return true;
                    String q = query.toLowerCase();
                    return (e.getPath() != null && e.getPath().toLowerCase().contains(q)) ||
                           (e.getRequestId() != null && e.getRequestId().toLowerCase().contains(q));
                })
                .sorted(Comparator.comparing(RequestEvent::getCreatedAt).reversed())
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<RequestEvent> sublist = (start <= end && start < filtered.size())
                ? filtered.subList(start, end)
                : Collections.emptyList();

        return new PageImpl<>(sublist, pageable, filtered.size());
    }

    private List<MetricsSummaryResponse.TimeSeriesPoint> computeTimeSeries(List<RequestEvent> events) {
        // Group into last 6 10-minute buckets or hours
        Map<String, long[]> buckets = new LinkedHashMap<>();
        Instant now = Instant.now();

        for (int i = 5; i >= 0; i--) {
            Instant bucketTime = now.minus(i * 10L, ChronoUnit.MINUTES);
            String label = bucketTime.toString().substring(11, 16); // HH:mm
            buckets.put(label, new long[]{0, 0, 0, 0}); // total, allowed, flagged, blocked
        }

        for (RequestEvent e : events) {
            if (e.getCreatedAt() != null) {
                long minutesAgo = ChronoUnit.MINUTES.between(e.getCreatedAt(), now);
                int bucketIdx = (int) (minutesAgo / 10);
                if (bucketIdx >= 0 && bucketIdx < 6) {
                    Instant bucketTime = now.minus(bucketIdx * 10L, ChronoUnit.MINUTES);
                    String label = bucketTime.toString().substring(11, 16);
                    long[] counts = buckets.get(label);
                    if (counts != null) {
                        counts[0]++; // total
                        if (e.getDecision() == Decision.ALLOW) counts[1]++;
                        else if (e.getDecision() == Decision.FLAG) counts[2]++;
                        else if (e.getDecision() == Decision.BLOCK) counts[3]++;
                    }
                }
            }
        }

        List<MetricsSummaryResponse.TimeSeriesPoint> points = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : buckets.entrySet()) {
            long[] c = entry.getValue();
            points.add(new MetricsSummaryResponse.TimeSeriesPoint(entry.getKey(), c[0], c[1], c[2], c[3]));
        }
        return points;
    }
}
