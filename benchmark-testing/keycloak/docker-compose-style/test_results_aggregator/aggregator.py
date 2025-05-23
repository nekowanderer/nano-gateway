#!/usr/bin/env python3

"""
Core aggregation logic for benchmark test results.
"""

import os
import glob
from collections import defaultdict

from .models import BenchmarkStat, TestResult


class ResultsAggregator:
    """Test Results Aggregator"""
    
    def __init__(self, scenario, results_dir="./results"):
        """Initialize the aggregator"""
        self.scenario = scenario
        self.results_dir = results_dir
        self.test_results = []
        self.request_type_stats = defaultdict(BenchmarkStat)
        self.html_template_dir = None  # Directory for storing found HTML template
        
        # Sum statistics
        self.total_requests_sum = 0
        self.ok_requests_sum = 0
        self.ko_requests_sum = 0
        self.mean_time_sum = 0
        self.min_time_min = float('inf')
        self.max_time_max = 0
        self.median_time_sum = 0
        self.percentiles2_sum = 0  # 75th percentile sum
        self.percentiles3_sum = 0  # 95th percentile sum
        self.percentiles4_sum = 0  # 99th percentile sum
        self.std_dev_sum = 0       # Standard deviation sum
        self.rps_sum = 0
        
        # Aggregated information
        self.sample_name = ""
        self.earliest_time = ""
        self.latest_time = ""
        self.sample_scenario = ""
        self.sample_params = ""
        
        # Percentiles and standard deviation averages
        self.percentiles2_avg = 0
        self.percentiles3_avg = 0
        self.percentiles4_avg = 0
        self.std_dev_avg = 0
        self.median_time_avg = 0
        self.mean_time_avg = 0
        self.rps_avg = 0
    
    def find_result_files(self):
        """Find matching result files"""
        # Search for directories with specific scenario
        search_pattern = os.path.join(self.results_dir, f"{self.scenario}*")
        
        # Find all relevant directories
        result_dirs = glob.glob(search_pattern)
        if not result_dirs:
            print(f"Could not find directories starting with '{self.scenario}'")
            return
        
        # Traverse each directory to find JSON result files
        for result_dir in result_dirs:
            print(f"Directory: {result_dir}")
            
            # Check if index.html exists, if so record the template directory
            if not self.html_template_dir and os.path.exists(os.path.join(result_dir, "index.html")):
                self.html_template_dir = result_dir
            
            # Find JSON result files
            json_pattern = os.path.join(result_dir, "result-*.json")
            json_files = glob.glob(json_pattern)
            
            if not json_files:
                print("  - No result files found")
                continue
            
            # Process each found file
            for json_file in json_files:
                print(f"  File: {os.path.basename(json_file)}")
                test_result = TestResult(json_file)
                test_result.print_summary()
                self.test_results.append(test_result)
                print("")
        
        print(f"Total processed result files: {len(self.test_results)}")
    
    def aggregate_results(self):
        """Aggregate all test results"""
        if not self.test_results:
            print("No result files found")
            return
        
        count = len(self.test_results)
        
        # Extract sample information
        if count > 0:
            self.sample_name = self.test_results[0].name
            self.sample_scenario = self.test_results[0].scenario
            self.sample_params = self.test_results[0].params
        
        # Aggregate statistics
        for result in self.test_results:
            self.total_requests_sum += result.total_requests
            self.ok_requests_sum += result.ok_requests
            self.ko_requests_sum += result.ko_requests
            self.mean_time_sum += result.mean_time
            self.median_time_sum += result.median_time
            self.percentiles2_sum += result.percentiles2  # 75th
            self.percentiles3_sum += result.percentiles3  # 95th
            self.percentiles4_sum += result.percentiles4  # 99th
            self.std_dev_sum += result.std_dev           # Standard deviation
            self.rps_sum += result.rps
            
            # Find minimum and maximum values
            if result.min_time < self.min_time_min:
                self.min_time_min = result.min_time
            if result.max_time > self.max_time_max:
                self.max_time_max = result.max_time
            
            # Find earliest and latest times
            if not self.earliest_time or result.start_time < self.earliest_time:
                self.earliest_time = result.start_time
            if not self.latest_time or result.end_time > self.latest_time:
                self.latest_time = result.end_time
            
            # Aggregate request type statistics
            for req_name, req_stats in result.request_types.items():
                self.request_type_stats[req_name].update(
                    req_stats['count'],
                    req_stats['mean'],
                    req_stats['min'],
                    req_stats['max'],
                    req_stats.get('median', 0),
                    req_stats.get('percentiles2', 0),
                    req_stats.get('percentiles3', 0), 
                    req_stats.get('percentiles4', 0),
                    req_stats.get('std_dev', 0),
                    req_stats.get('rps', 0)
                )
        
        # Calculate averages
        self.mean_time_avg = round(self.mean_time_sum / count, 2)
        self.median_time_avg = round(self.median_time_sum / count, 2)
        self.percentiles2_avg = round(self.percentiles2_sum / count, 2)
        self.percentiles3_avg = round(self.percentiles3_sum / count, 2)
        self.percentiles4_avg = round(self.percentiles4_sum / count, 2)
        self.std_dev_avg = round(self.std_dev_sum / count, 2)
        self.rps_avg = round(self.rps_sum / count, 2)
        
        # Calculate averages for each request type
        for req_name, stats in self.request_type_stats.items():
            stats.mean = round(stats.mean / count, 2)
            stats.median = round(stats.median / count, 2)
            stats.percentiles2 = round(stats.percentiles2 / count, 2)
            stats.percentiles3 = round(stats.percentiles3 / count, 2)
            stats.percentiles4 = round(stats.percentiles4 / count, 2)
            stats.std_dev = round(stats.std_dev / count, 2)
            stats.rps = round(stats.rps / count, 2)
    
    def print_aggregated_summary(self):
        """Print aggregated summary"""
        count = len(self.test_results)
        if count == 0:
            print("No result files found")
            return
        
        print(f"Test Results Summary (Scenario: {self.scenario}, Sample Count: {count}):")
        print(f"  Test Name: {self.sample_name}")
        print(f"  Test Time Range: {self.earliest_time} to {self.latest_time}")
        print(f"  Test Scenario: {self.sample_scenario}")
        print(f"  Test Parameters: {self.sample_params}")
        
        print("  Overall Request Statistics:")
        print(f"    Total Requests: {self.total_requests_sum}")
        print(f"    Successful Requests: {self.ok_requests_sum}")
        print(f"    Failed Requests: {self.ko_requests_sum}")
        print(f"    Average Response Time: {self.mean_time_avg} ms")
        print(f"    Minimum Response Time: {self.min_time_min} ms")
        print(f"    Maximum Response Time: {self.max_time_max} ms")
        print(f"    Median Response Time: {self.median_time_avg} ms")
        print(f"    75th Percentile: {self.percentiles2_avg} ms")
        print(f"    95th Percentile: {self.percentiles3_avg} ms")
        print(f"    99th Percentile: {self.percentiles4_avg} ms")
        print(f"    Standard Deviation: {self.std_dev_avg} ms")
        print(f"    Requests Per Second: {self.rps_avg}")
        
        print("  Request Type Details:")
        for req_name, stats in self.request_type_stats.items():
            print(f"    Request Type: {req_name}")
            print(f"      Total Requests: {stats.count}")
            print(f"      Average Response Time: {stats.mean} ms")
            print(f"      Minimum Response Time: {stats.min_val} ms")
            print(f"      Maximum Response Time: {stats.max_val} ms")
            print(f"      Median Response Time: {stats.median} ms")
            print(f"      75th Percentile: {stats.percentiles2} ms")
            print(f"      95th Percentile: {stats.percentiles3} ms")
            print(f"      99th Percentile: {stats.percentiles4} ms")
            print(f"      Standard Deviation: {stats.std_dev} ms")
            print(f"      Requests Per Second: {stats.rps}")
    
    def find_html_template(self):
        """Find HTML report template"""
        if self.html_template_dir:
            return True
        
        # First, try to use aggregated-report-template if it exists
        template_dir = "./aggregated-report-template"
        if os.path.exists(os.path.join(template_dir, "index.html")):
            self.html_template_dir = template_dir
            print(f"HTML template directory found: {self.html_template_dir}")
            return True
        
        # Fall back to search for directories with index.html in results
        result_dirs = glob.glob(os.path.join(self.results_dir, f"{self.scenario}*"))
        
        for result_dir in result_dirs:
            if os.path.exists(os.path.join(result_dir, "index.html")):
                self.html_template_dir = result_dir
                print(f"HTML template directory found: {self.html_template_dir}")
                return True
        
        return False 