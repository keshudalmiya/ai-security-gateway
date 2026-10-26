"""
FastAPI Machine Learning Inference Microservice.

Provides REST endpoints for HTTP request anomaly detection using a trained
Isolation Forest model artifact.
"""
import os
import sys
import time
import logging
import joblib
import numpy as np
from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException, Request, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

from models import PredictRequest, PredictResponse, HealthResponse

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] [%(name)s] %(message)s")
logger = logging.getLogger("ml_service")

REPO_ROOT = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
MODEL_ARTIFACT_PATH = os.path.join(REPO_ROOT, "ml", "artifacts", "isolation_forest_model.joblib")

START_TIME = time.time()
MODEL_BUNDLE = None

def load_model_artifact():
    global MODEL_BUNDLE
    if os.path.exists(MODEL_ARTIFACT_PATH):
        try:
            logger.info(f"Loading model artifact from {MODEL_ARTIFACT_PATH}...")
            MODEL_BUNDLE = joblib.load(MODEL_ARTIFACT_PATH)
            logger.info(f"Loaded model version: {MODEL_BUNDLE.get('model_version', 'unknown')}")
        except Exception as e:
            logger.error(f"Failed to load model artifact: {e}")
            MODEL_BUNDLE = None
    else:
        logger.warning(f"Model artifact not found at {MODEL_ARTIFACT_PATH}. Service running in uncalibrated mode.")
        MODEL_BUNDLE = None

@asynccontextmanager
async def lifespan(app: FastAPI):
    load_model_artifact()
    yield

app = FastAPI(
    title="Web API Security Gateway - ML Inference Service",
    version="1.0.0",
    description="Lightweight Isolation Forest anomaly detection microservice for Web API attacks",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/health", response_model=HealthResponse)
def get_health():
    """Returns service operational health and model status."""
    uptime = time.time() - START_TIME
    is_loaded = MODEL_BUNDLE is not None
    model_ver = MODEL_BUNDLE.get("model_version", "none") if is_loaded else "none"
    
    return HealthResponse(
        status="OK" if is_loaded else "DEGRADED",
        service="ml-service",
        modelLoaded=is_loaded,
        modelVersion=model_ver,
        uptimeSeconds=round(uptime, 2)
    )

@app.post("/predict", response_model=PredictResponse)
def predict_anomaly(req: PredictRequest):
    """
    Accepts 8-dimensional feature vector, performs feature scaling,
    and calculates anomaly classification and normalized score.
    """
    t_start = time.perf_counter()
    
    if MODEL_BUNDLE is None:
        load_model_artifact()
        if MODEL_BUNDLE is None:
            latency_ms = (time.perf_counter() - t_start) * 1000.0
            return PredictResponse(
                status="DEGRADED",
                isAnomaly=False,
                anomalyScore=0.50,
                modelVersion="unloaded-fallback",
                latencyMs=round(latency_ms, 3),
                error="Model artifact not loaded on server."
            )
            
    try:
        f = req.features
        feature_vector = np.array([[
            f.requestLength,
            f.urlLength,
            f.parameterCount,
            f.specialCharCount,
            f.httpMethodCode,
            f.headerCount,
            f.bodyLength,
            f.requestRate1m
        ]], dtype=np.float32)
        
        scaler = MODEL_BUNDLE["scaler"]
        model = MODEL_BUNDLE["model"]
        bounds = MODEL_BUNDLE.get("score_bounds", {})
        min_score = bounds.get("min_score", -0.75)
        max_score = bounds.get("max_score", -0.35)
        threshold = bounds.get("decision_threshold", -0.62)
        
        # Scale features using fitted scaler from training
        scaled_vector = scaler.transform(feature_vector)
        
        # Score sample: lower score indicates higher anomaly
        raw_score = float(model.score_samples(scaled_vector)[0])
        
        # Calibrate anomaly score to [0.0, 1.0] (1.0 = high anomaly, 0.0 = completely benign)
        norm_score = (max_score - raw_score) / (max_score - min_score + 1e-6)
        calibrated_score = float(max(0.0, min(1.0, norm_score)))
        
        # Anomaly decision: raw score <= threshold OR calibrated score >= 0.55
        is_anomaly = bool(raw_score <= threshold or calibrated_score >= 0.55)
        
        latency_ms = (time.perf_counter() - t_start) * 1000.0
        
        return PredictResponse(
            status="OK",
            isAnomaly=is_anomaly,
            anomalyScore=round(calibrated_score, 4),
            modelVersion=MODEL_BUNDLE.get("model_version", "iforest-csic-v1"),
            latencyMs=round(latency_ms, 3)
        )
        
    except Exception as e:
        logger.error(f"Prediction error: {e}", exc_info=True)
        latency_ms = (time.perf_counter() - t_start) * 1000.0
        return PredictResponse(
            status="ERROR",
            isAnomaly=False,
            anomalyScore=0.50,
            modelVersion="error-fallback",
            latencyMs=round(latency_ms, 3),
            error=str(e)
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
