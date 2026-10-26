"""
CSIC 2010 Raw HTTP Dataset Parser

Parses HTTP requests from raw CSIC text files into structured dictionaries.
Extracts method, path, query, headers, and body according to RFC HTTP standards.
"""
import urllib.parse
from typing import List, Dict, Any, Generator

def parse_http_requests(file_path: str, label: int, max_samples: int = None) -> List[Dict[str, Any]]:
    """
    Parses a CSIC 2010 raw file into a list of request dictionaries.
    
    Args:
        file_path: Absolute or relative path to the text file
        label: 0 for normal traffic, 1 for attack/anomalous traffic
        max_samples: Optional limit on number of requests to parse
        
    Returns:
        List of parsed request records
    """
    requests = []
    
    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
        content = f.read()
    
    # In CSIC 2010, requests are separated by blank lines before next METHOD (GET/POST/PUT)
    # Each request starts with a request line matching: METHOD URI HTTP/1.x
    raw_blocks = content.split("\n\n")
    
    current_block_lines = []
    for block in raw_blocks:
        trimmed = block.strip()
        if not trimmed:
            continue
        
        lines = trimmed.split("\n")
        first_line = lines[0].strip()
        
        # Check if this starts a new request
        parts = first_line.split(" ")
        if len(parts) >= 2 and parts[0] in ["GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS"]:
            if current_block_lines:
                req = _parse_single_request(current_block_lines, label)
                if req:
                    requests.append(req)
                    if max_samples and len(requests) >= max_samples:
                        break
                current_block_lines = []
        
        current_block_lines.extend(lines)
    
    # Process final accumulated block
    if current_block_lines and (not max_samples or len(requests) < max_samples):
        req = _parse_single_request(current_block_lines, label)
        if req:
            requests.append(req)
            
    return requests

def _parse_single_request(lines: List[str], label: int) -> Dict[str, Any]:
    """Parses lines of a single HTTP request block."""
    if not lines:
        return None
    
    request_line = lines[0].strip()
    parts = request_line.split(" ")
    if len(parts) < 2:
        return None
    
    method = parts[0].strip().upper()
    raw_uri = parts[1].strip()
    
    # Parse URI into path and query
    parsed_url = urllib.parse.urlsplit(raw_uri)
    path = parsed_url.path or "/"
    query = parsed_url.query or ""
    
    headers = {}
    body_lines = []
    in_body = False
    
    for line in lines[1:]:
        line_stripped = line.strip()
        if in_body:
            body_lines.append(line)
        elif line_stripped == "":
            in_body = True
        elif ":" in line:
            hkey, hval = line.split(":", 1)
            headers[hkey.strip().lower()] = hval.strip()
        else:
            # If not a header and no blank line, treat as body continuation (e.g. POST payload)
            body_lines.append(line)
            in_body = True
            
    body = "\n".join(body_lines).strip()
    
    return {
        "method": method,
        "path": path,
        "query": query,
        "headers": headers,
        "body": body,
        "label": label  # 0: normal, 1: attack
    }
