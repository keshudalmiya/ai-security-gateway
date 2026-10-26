"""
Unit and Integration Tests for FastAPI ML Inference Service.
"""
import pytest
from fastapi.testclient import TestClient
import os
import sys

# Ensure current dir is in sys.path
CURRENT_DIR = os.path.dirname(os.path.abspath(__file__))
if CURRENT_DIR not in sys.path:
    sys.path.insert(0, CURRENT_DIR)

from main import app, load_model_artifact

client = TestClient(app)

def test_health_endpoint():
    """Verifies GET /health contract."""
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["service"] == "ml-service"
    assert "status" in data
    assert "modelLoaded" in data
    assert "uptimeSeconds" in data
    assert data["uptimeSeconds"] >= 0

def test_predict_normal_request():
    """Verifies POST /predict with typical benign HTTP request features."""
    payload = {
        "featureSchemaVersion": "v1",
        "features": {
            "requestLength": 128,
            "urlLength": 24,
            "parameterCount": 1,
            "specialCharCount": 1,
            "httpMethodCode": 0,
            "headerCount": 4,
            "bodyLength": 0,
            "requestRate1m": 2.0
        }
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] in ["OK", "DEGRADED"]
    assert "isAnomaly" in data
    assert isinstance(data["isAnomaly"], bool)
    assert 0.0 <= data["anomalyScore"] <= 1.0
    assert "modelVersion" in data
    assert data["latencyMs"] >= 0.0

def test_predict_anomalous_request():
    """Verifies POST /predict with extreme anomalous feature values."""
    payload = {
        "featureSchemaVersion": "v1",
        "features": {
            "requestLength": 8500,
            "urlLength": 1200,
            "parameterCount": 25,
            "specialCharCount": 180,
            "httpMethodCode": 1,
            "headerCount": 12,
            "bodyLength": 7200,
            "requestRate1m": 120.0
        }
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] in ["OK", "DEGRADED"]
    assert 0.0 <= data["anomalyScore"] <= 1.0
    # For extreme anomaly, score should be elevated
    if data["status"] == "OK":
        assert data["anomalyScore"] > 0.40

def test_predict_invalid_schema():
    """Verifies validation failure on invalid input (e.g. negative requestLength)."""
    payload = {
        "featureSchemaVersion": "v1",
        "features": {
            "requestLength": -5,
            "urlLength": 24
        }
    }
    response = client.post("/predict", json=payload)
    assert response.status_code == 422  # Pydantic validation error
