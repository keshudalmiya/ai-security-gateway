"""
Feature Extraction Module for Web API Security Gateway (Schema v1)

Extracts an 8-dimensional numerical feature vector from HTTP request metadata.
Used consistently for both offline dataset training and online inference.
"""
from typing import Dict, Any, List
import urllib.parse
import re

FEATURE_SCHEMA_VERSION = "v1"

HTTP_METHOD_MAP = {
    "GET": 0,
    "POST": 1,
    "PUT": 2,
    "PATCH": 3,
    "DELETE": 4,
    "OPTIONS": 5,
    "HEAD": 6,
}

FEATURE_NAMES = [
    "requestLength",
    "urlLength",
    "parameterCount",
    "specialCharCount",
    "httpMethodCode",
    "headerCount",
    "bodyLength",
    "requestRate1m"
]

# Selected suspicious punctuation characters common in web attacks
SUSPICIOUS_CHARS = set(['<', '>', "'", '"', '\\', ';', '%', '(', ')', '=', '*', '-', '|', '&', '`', '$', '!', '^', '{', '}', '[', ']'])

def extract_features(
    method: str,
    path: str,
    query: str = "",
    headers: Dict[str, str] = None,
    body: str = "",
    request_rate_1m: float = 1.0
) -> Dict[str, Any]:
    """
    Extracts numerical features from an HTTP request.
    
    Args:
        method: HTTP method (e.g., 'GET', 'POST')
        path: URI path (e.g., '/api/products')
        query: Query string (e.g., 'id=1&name=test')
        headers: Dictionary of headers (values not inspected for ML features to avoid leakage/PII)
        body: Request payload string
        request_rate_1m: Estimated request rate per minute for the client
    
    Returns:
        Dictionary containing extracted features adhering to schema v1.
    """
    method_upper = (method or "GET").strip().upper()
    method_code = HTTP_METHOD_MAP.get(method_upper, 7)  # 7 for OTHER
    
    clean_path = path or "/"
    clean_query = query or ""
    clean_body = body or ""
    headers_dict = headers or {}
    
    # 1. URL Length (path + query length)
    full_url = clean_path + (("?" + clean_query) if clean_query else "")
    url_length = len(full_url)
    
    # 2. Body Length
    body_length = len(clean_body.encode('utf-8', errors='ignore'))
    
    # 3. Request Length (approximate total size in bytes)
    header_approx_size = sum(len(k) + len(v) + 4 for k, v in headers_dict.items())
    request_length = len(method_upper) + 1 + url_length + 11 + header_approx_size + body_length
    
    # 4. Parameter Count (query params + body params if form/json encoded)
    param_count = 0
    if clean_query:
        param_count += len(urllib.parse.parse_qs(clean_query, keep_blank_values=True))
    
    # If body is URL-encoded or contains key-value pairs
    if clean_body:
        if "=" in clean_body and ("&" in clean_body or not clean_body.startswith("{")):
            param_count += len(urllib.parse.parse_qs(clean_body, keep_blank_values=True))
        elif clean_body.strip().startswith("{") and clean_body.strip().endswith("}"):
            # Simple JSON keys count without full recursive eval
            param_count += len(re.findall(r'"([^"\\]+)":', clean_body))
    
    # 5. Special Character Count (in query, path, and body)
    combined_content = full_url + clean_body
    special_char_count = sum(1 for c in combined_content if c in SUSPICIOUS_CHARS)
    
    # 6. Header Count
    header_count = len(headers_dict)
    
    # 7. Request Rate
    rate = max(1.0, float(request_rate_1m))
    
    return {
        "requestLength": int(request_length),
        "urlLength": int(url_length),
        "parameterCount": int(param_count),
        "specialCharCount": int(special_char_count),
        "httpMethodCode": int(method_code),
        "headerCount": int(header_count),
        "bodyLength": int(body_length),
        "requestRate1m": float(rate),
    }

def features_to_vector(features: Dict[str, Any]) -> List[float]:
    """Converts feature dict to ordered float list."""
    return [float(features[name]) for name in FEATURE_NAMES]
