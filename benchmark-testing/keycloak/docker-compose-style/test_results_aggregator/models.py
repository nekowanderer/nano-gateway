#!/usr/bin/env python3

"""
Data models for benchmark result aggregation.
"""

import json


class BenchmarkStat:
    """Class representing benchmark statistics"""
    
    def __init__(self, count=0, mean=0, min_val=float('inf'), max_val=0):
        self.count = count
        self.mean = mean
        self.min_val = min_val
        self.max_val = max_val
        self.median = 0         # Median (50th percentile)
        self.percentiles2 = 0   # 75th percentile
        self.percentiles3 = 0   # 95th percentile
        self.percentiles4 = 0   # 99th percentile
        self.std_dev = 0        # Standard deviation
        self.rps = 0            # Requests per second
    
    def update(self, count, mean, min_val, max_val, median=0, percentiles2=0, percentiles3=0, percentiles4=0, std_dev=0, rps=0):
        """Update statistics"""
        self.count += count
        self.mean += mean
        if min_val < self.min_val and min_val > 0:
            self.min_val = min_val
        if max_val > self.max_val:
            self.max_val = max_val
        self.median += median
        self.percentiles2 += percentiles2
        self.percentiles3 += percentiles3
        self.percentiles4 += percentiles4
        self.std_dev += std_dev
        self.rps += rps
    
    def to_dict(self):
        """Convert to dictionary format"""
        return {
            "count": self.count,
            "mean": self.mean,
            "min": self.min_val if self.min_val != float('inf') else 0,
            "max": self.max_val,
            "median": self.median,
            "percentiles2": self.percentiles2,
            "percentiles3": self.percentiles3,
            "percentiles4": self.percentiles4,
            "std_dev": self.std_dev,
            "rps": self.rps
        }


class TestResult:
    """Class representing a test result"""
    
    def __init__(self, file_path):
        """Initialize test result from a JSON file"""
        self.file_path = file_path
        self.name = ""
        self.start_time = ""
        self.end_time = ""
        self.scenario = ""
        self.params = ""
        self.total_requests = 0
        self.ok_requests = 0
        self.ko_requests = 0
        self.mean_time = 0
        self.min_time = 0
        self.max_time = 0
        self.median_time = 0
        self.percentiles2 = 0  # 75th
        self.percentiles3 = 0  # 95th
        self.percentiles4 = 0  # 99th
        self.std_dev = 0       # Standard deviation
        self.rps = 0
        self.request_types = {}  # Request type statistics
        
        # Parse JSON file
        self._parse_file()
    
    def _parse_file(self):
        """Parse JSON file and extract relevant data"""
        try:
            with open(self.file_path, 'r') as f:
                data = json.load(f)
            
            # Basic information
            self.name = data.get("name", "")
            self.start_time = data.get("grafana_input", {}).get("start", {}).get("iso", "")
            self.end_time = data.get("grafana_input", {}).get("end", {}).get("iso", "")
            self.scenario = data.get("grafana_input", {}).get("input", {}).get("scenario", "")
            self.params = data.get("grafana_input", {}).get("input", {}).get("config", "")
            
            # Overall statistics
            stats = data.get("grafana_output", {}).get("stats", {}).get("stats", {})
            self.total_requests = stats.get("numberOfRequests", {}).get("total", 0)
            self.ok_requests = stats.get("numberOfRequests", {}).get("ok", 0)
            self.ko_requests = stats.get("numberOfRequests", {}).get("ko", 0)
            self.mean_time = stats.get("meanResponseTime", {}).get("total", 0)
            self.min_time = stats.get("minResponseTime", {}).get("total", 0)
            self.max_time = stats.get("maxResponseTime", {}).get("total", 0)
            self.median_time = stats.get("percentiles1", {}).get("total", 0)
            self.percentiles2 = stats.get("percentiles2", {}).get("total", 0)  # 75th
            self.percentiles3 = stats.get("percentiles3", {}).get("total", 0)  # 95th
            self.percentiles4 = stats.get("percentiles4", {}).get("total", 0)  # 99th
            self.std_dev = stats.get("standardDeviation", {}).get("total", 0)  # Standard deviation
            self.rps = stats.get("meanNumberOfRequestsPerSecond", {}).get("total", 0)
            
            # Request type statistics
            contents = data.get("grafana_output", {}).get("stats", {}).get("contents", {})
            for req_key, req_data in contents.items():
                req_name = req_data.get("name", "Unknown request")
                req_stats = req_data.get("stats", {})
                
                req_rps = req_stats.get("meanNumberOfRequestsPerSecond", {}).get("total", 0)
                req_percentiles2 = req_stats.get("percentiles2", {}).get("total", 0)
                req_percentiles3 = req_stats.get("percentiles3", {}).get("total", 0)
                req_percentiles4 = req_stats.get("percentiles4", {}).get("total", 0)
                req_std_dev = req_stats.get("standardDeviation", {}).get("total", 0)
                
                self.request_types[req_name] = {
                    "count": req_stats.get("numberOfRequests", {}).get("total", 0),
                    "mean": req_stats.get("meanResponseTime", {}).get("total", 0),
                    "min": req_stats.get("minResponseTime", {}).get("total", 0),
                    "max": req_stats.get("maxResponseTime", {}).get("total", 0),
                    "median": req_stats.get("percentiles1", {}).get("total", 0),
                    "percentiles2": req_percentiles2,  # 75th
                    "percentiles3": req_percentiles3,  # 95th
                    "percentiles4": req_percentiles4,  # 99th
                    "std_dev": req_std_dev,           # Standard deviation
                    "rps": req_rps
                }
        
        except (json.JSONDecodeError, IOError, KeyError) as e:
            print(f"Error parsing file {self.file_path}: {e}")
    
    def print_summary(self):
        """Print test summary"""
        print(f"    Test Name: {self.name}")
        print(f"    Test Time: {self.start_time} to {self.end_time}")
        print(f"    Test Scenario: {self.scenario}")
        print(f"    Test Parameters: {self.params}")
        
        print("    Overall Request Statistics:")
        print(f"      Total Requests: {self.total_requests}")
        print(f"      Successful Requests: {self.ok_requests}")
        print(f"      Failed Requests: {self.ko_requests}")
        print(f"      Average Response Time: {self.mean_time} ms")
        print(f"      Minimum Response Time: {self.min_time} ms")
        print(f"      Maximum Response Time: {self.max_time} ms")
        print(f"      Median Response Time: {self.median_time} ms")
        print(f"      Requests Per Second: {self.rps}")
        
        print("    Request Type Details:")
        for req_name, req_stats in self.request_types.items():
            print(f"      Request Type: {req_name}")
            print(f"        Total Requests: {req_stats['count']}")
            print(f"        Average Response Time: {req_stats['mean']} ms")
            print(f"        Minimum Response Time: {req_stats['min']} ms")
            print(f"        Maximum Response Time: {req_stats['max']} ms") 