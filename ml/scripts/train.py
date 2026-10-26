"""
Model Training Script for AI-Powered Web API Security Gateway.

Trains an Isolation Forest anomaly detector on benign HTTP traffic from the CSIC 2010 dataset.
Extracts 8-dimensional numerical feature vectors and normalizes anomaly scores to the [0, 1] range.
Saves model artifact, scaler, and training metadata to ml/artifacts/.
"""
import os
import sys
import time
import json
import logging
import joblib
import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler

# Ensure scripts dir is on sys.path
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
if SCRIPT_DIR not in sys.path:
    sys.path.insert(0, SCRIPT_DIR)

from feature_extractor import extract_features, features_to_vector, FEATURE_NAMES, FEATURE_SCHEMA_VERSION
from parse_csic import parse_http_requests

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("train_model")

DATA_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "data"))
ARTIFACTS_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "artifacts"))

MODEL_VERSION = "iforest-csic-v1"
RANDOM_SEED = 42

def train():
    os.makedirs(ARTIFACTS_DIR, exist_ok=True)
    training_file = os.path.join(DATA_DIR, "normalTrafficTraining.txt")
    
    if not os.path.exists(training_file):
        logger.error(f"Training dataset not found at {training_file}. Run download_dataset.py first.")
        return 1
        
    logger.info("Parsing normal HTTP training traffic from CSIC 2010...")
    # Train on up to 20,000 requests for robust lightweight representation
    training_requests = parse_http_requests(training_file, label=0, max_samples=20000)
    logger.info(f"Loaded {len(training_requests)} benign HTTP requests for training.")
    
    logger.info("Extracting numerical feature vectors...")
    X_train_raw = []
    for req in training_requests:
        feat_dict = extract_features(
            method=req["method"],
            path=req["path"],
            query=req["query"],
            headers=req["headers"],
            body=req["body"],
            request_rate_1m=1.0
        )
        X_train_raw.append(features_to_vector(feat_dict))
        
    X_train = np.array(X_train_raw, dtype=np.float32)
    logger.info(f"Feature matrix shape: {X_train.shape}")
    
    # Standardize features for stable distance metrics
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    
    # Train Isolation Forest
    # Contamination set to 0.05 (expecting up to 5% potential noise/outliers in training distribution)
    logger.info("Training Isolation Forest model (n_estimators=100, contamination=0.05)...")
    start_time = time.time()
    model = IsolationForest(
        n_estimators=100,
        max_samples="auto",
        contamination=0.05,
        random_state=RANDOM_SEED,
        n_jobs=-1
    )
    model.fit(X_train_scaled)
    train_duration = time.time() - start_time
    logger.info(f"Model training completed in {train_duration:.2f} seconds.")
    
    # Calculate baseline scores on training data to establish normalization bounds
    # IsolationForest.score_samples returns negative anomaly score (lower means more anomalous)
    raw_scores = model.score_samples(X_train_scaled)
    min_score = float(np.min(raw_scores))
    max_score = float(np.max(raw_scores))
    p5_threshold = float(np.percentile(raw_scores, 5))
    logger.info(f"Baseline raw score bounds: min={min_score:.4f}, max={max_score:.4f}, 5th percentile={p5_threshold:.4f}")
    
    # Save model and scaler artifacts
    model_bundle = {
        "model": model,
        "scaler": scaler,
        "feature_names": FEATURE_NAMES,
        "feature_schema_version": FEATURE_SCHEMA_VERSION,
        "model_version": MODEL_VERSION,
        "score_bounds": {
            "min_score": min_score,
            "max_score": max_score,
            "decision_threshold": p5_threshold
        }
    }
    
    model_path = os.path.join(ARTIFACTS_DIR, "isolation_forest_model.joblib")
    joblib.dump(model_bundle, model_path, compress=3)
    logger.info(f"Saved model bundle to {model_path} ({os.path.getsize(model_path)} bytes).")
    
    metadata = {
        "model_version": MODEL_VERSION,
        "algorithm": "IsolationForest",
        "n_estimators": 100,
        "contamination": 0.05,
        "random_seed": RANDOM_SEED,
        "training_sample_count": len(X_train),
        "feature_names": FEATURE_NAMES,
        "feature_schema_version": FEATURE_SCHEMA_VERSION,
        "train_duration_seconds": round(train_duration, 3),
        "score_bounds": {
            "min_score": min_score,
            "max_score": max_score,
            "decision_threshold": p5_threshold
        },
        "dataset_name": "CSIC 2010 HTTP Dataset (Spanish National Research Council)",
        "createdAt": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
    }
    
    metadata_path = os.path.join(ARTIFACTS_DIR, "model_metadata.json")
    with open(metadata_path, "w", encoding="utf-8") as f:
        json.dump(metadata, f, indent=2)
    logger.info(f"Saved training metadata to {metadata_path}.")
    
    return 0

if __name__ == "__main__":
    sys.exit(train())
