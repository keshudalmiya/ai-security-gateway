"""
Data Transfer Objects and Pydantic Schemas for ML Inference Service.
Adheres strictly to docs/05-BACKEND-SCHEMA.md.
"""
from pydantic import BaseModel, Field
from typing import Optional, List, Dict

class FeaturePayload(BaseModel):
    requestLength: int = Field(..., ge=0, le=1048576, description="Total request length in bytes")
    urlLength: int = Field(..., ge=1, le=8192, description="URL length (path + query)")
    parameterCount: int = Field(0, ge=0, le=1000, description="Total parameter count")
    specialCharCount: int = Field(0, ge=0, le=10000, description="Count of suspicious punctuation characters")
    httpMethodCode: int = Field(0, ge=0, le=7, description="Method code: 0=GET, 1=POST, 2=PUT, 3=PATCH, 4=DELETE, 5=OPTIONS, 6=HEAD, 7=OTHER")
    headerCount: int = Field(0, ge=0, le=200, description="Number of HTTP headers")
    bodyLength: int = Field(0, ge=0, le=1048576, description="Body length in bytes")
    requestRate1m: float = Field(1.0, ge=0.0, le=10000.0, description="Requests per minute for client")

class PredictRequest(BaseModel):
    featureSchemaVersion: str = Field("v1", description="Feature schema version")
    features: FeaturePayload

class PredictResponse(BaseModel):
    status: str = Field("OK", description="Inference status: OK, ERROR, or DEGRADED")
    isAnomaly: bool = Field(False, description="True if request is classified as an anomaly")
    anomalyScore: float = Field(..., ge=0.0, le=1.0, description="Calibrated anomaly score in [0.0, 1.0]")
    modelVersion: str = Field(..., description="Active model artifact version identifier")
    latencyMs: float = Field(..., ge=0.0, description="Inference latency in milliseconds")
    error: Optional[str] = None

class HealthResponse(BaseModel):
    status: str = Field("OK", description="Service health: OK or DEGRADED")
    service: str = Field("ml-service", description="Service name")
    modelLoaded: bool = Field(..., description="Whether model artifact is loaded and operational")
    modelVersion: str = Field(..., description="Active model version")
    uptimeSeconds: float = Field(..., description="Service uptime in seconds")
