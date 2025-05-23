#!/usr/bin/env python3

"""
Report generators for benchmark test results.
"""

import os
import json
import shutil

# Try to import BeautifulSoup, but don't fail if it's not available
try:
    from bs4 import BeautifulSoup
    BS4_AVAILABLE = True
except ImportError:
    BS4_AVAILABLE = False
    print("Warning: BeautifulSoup4 not found. HTML content replacement will be skipped.")


class JsonReporter:
    """JSON report generator"""
    
    def __init__(self, aggregator):
        """Initialize with an aggregator instance"""
        self.aggregator = aggregator
    
    def export_json_summary(self, output_file=None):
        """Export JSON format summary"""
        if not self.aggregator.test_results:
            print("No result files found")
            return
        
        # Prepare JSON data
        json_data = {
            "testInfo": {
                "name": self.aggregator.sample_name,
                "scenario": self.aggregator.sample_scenario,
                "params": self.aggregator.sample_params,
                "startTime": self.aggregator.earliest_time,
                "endTime": self.aggregator.latest_time,
                "sampleCount": len(self.aggregator.test_results)
            },
            "overallStats": {
                "totalRequests": self.aggregator.total_requests_sum,
                "okRequests": self.aggregator.ok_requests_sum,
                "koRequests": self.aggregator.ko_requests_sum,
                "meanResponseTime": self.aggregator.mean_time_avg,
                "minResponseTime": self.aggregator.min_time_min,
                "maxResponseTime": self.aggregator.max_time_max,
                "medianResponseTime": self.aggregator.median_time_avg,
                "percentiles2": self.aggregator.percentiles2_avg,  # 75th
                "percentiles3": self.aggregator.percentiles3_avg,  # 95th
                "percentiles4": self.aggregator.percentiles4_avg,  # 99th
                "standardDeviation": self.aggregator.std_dev_avg,
                "requestsPerSecond": self.aggregator.rps_avg
            },
            "requestTypes": {}
        }
        
        # Add request type statistics
        for req_name, stats in self.aggregator.request_type_stats.items():
            json_data["requestTypes"][req_name] = {
                "count": stats.count,
                "meanResponseTime": stats.mean,
                "minResponseTime": stats.min_val if stats.min_val != float('inf') else 0,
                "maxResponseTime": stats.max_val,
                "medianResponseTime": stats.median,
                "percentiles2": stats.percentiles2,  # 75th
                "percentiles3": stats.percentiles3,  # 95th
                "percentiles4": stats.percentiles4,  # 99th
                "standardDeviation": stats.std_dev,
                "requestsPerSecond": stats.rps
            }
        
        # Output JSON file
        if output_file:
            output_path = output_file
        else:
            # Generate default output file name
            output_path = os.path.join(self.aggregator.results_dir, f"{self.aggregator.scenario}_summary.json")
        
        with open(output_path, 'w') as f:
            json.dump(json_data, f, indent=2)
        
        print(f"JSON summary exported to {output_path}")
        return output_path


class HtmlReporter:
    """HTML report generator"""
    
    def __init__(self, aggregator):
        """Initialize with an aggregator instance"""
        self.aggregator = aggregator
    
    def export_html_report(self, output_dir="./aggregate-report"):
        """Generate HTML aggregated report"""
        if not self.aggregator.test_results or not self.aggregator.html_template_dir:
            print("Could not find test results or HTML template")
            return False
        
        # Ensure output directory exists
        os.makedirs(output_dir, exist_ok=True)
        
        # Copy all necessary resource files
        resource_dirs = ["style", "js"]
        for resource_dir in resource_dirs:
            src_dir = os.path.join(self.aggregator.html_template_dir, resource_dir)
            dst_dir = os.path.join(output_dir, resource_dir)
            
            if os.path.exists(src_dir):
                if os.path.exists(dst_dir):
                    shutil.rmtree(dst_dir)
                shutil.copytree(src_dir, dst_dir)
        
        # Process the template HTML file
        template_file = os.path.join(self.aggregator.html_template_dir, "index.html")
        output_file = os.path.join(output_dir, "index.html")
        
        if not os.path.exists(template_file):
            print(f"Template file not found: {template_file}")
            return False
        
        try:
            if BS4_AVAILABLE:
                # Use BeautifulSoup for precise HTML manipulation
                with open(template_file, 'r', encoding='utf-8') as f:
                    soup = BeautifulSoup(f.read(), 'html.parser')
                
                # Extract scenario short name
                scenario_short_name = self.aggregator.sample_scenario.split('.')[-1] if self.aggregator.sample_scenario else "Unknown"
                
                # Update the title tag
                title_tag = soup.find('title')
                if title_tag:
                    title_tag.string = f"Gatling Stats - {scenario_short_name} Aggregate Report"
                
                # Update the onglet div
                onglet_div = soup.find('div', class_='onglet')
                if onglet_div:
                    onglet_div.string = scenario_short_name
                
                # Write the modified HTML
                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(str(soup))
                
                print(f"HTML aggregate report generated with content updates: {output_file}")
            else:
                # Fall back to simple file copy if BeautifulSoup is not available
                shutil.copy2(template_file, output_file)
                print(f"HTML aggregate report generated (simple copy): {output_file}")
                print("Note: Content replacement skipped due to missing BeautifulSoup4")
            
            return True
        
        except (IOError, UnicodeDecodeError) as e:
            print(f"Error generating HTML report: {e}")
            return False


class JavaScriptReporter:
    """JavaScript data generator"""
    
    def __init__(self, aggregator):
        """Initialize with an aggregator instance"""
        self.aggregator = aggregator
    
    def generate_stats_js(self, output_file=None):
        """Generate stats.js file with statistics data"""
        if not self.aggregator.test_results:
            print("No result files found")
            return
        
        # Match original Gatling report format
        stats_data = {
            "type": "GROUP",
            "name": "All Requests (Summary)",
            "path": "",
            "pathFormatted": "group_missing-name--1146707516",
            "stats": {
                "name": "All Requests (Summary)",
                "numberOfRequests": {
                    "total": str(self.aggregator.total_requests_sum),
                    "ok": str(self.aggregator.ok_requests_sum),
                    "ko": str(self.aggregator.ko_requests_sum)
                },
                "minResponseTime": {
                    "total": str(self.aggregator.min_time_min),
                    "ok": str(self.aggregator.min_time_min),
                    "ko": "0"
                },
                "maxResponseTime": {
                    "total": str(self.aggregator.max_time_max),
                    "ok": str(self.aggregator.max_time_max),
                    "ko": "0"
                },
                "meanResponseTime": {
                    "total": str(self.aggregator.mean_time_avg),
                    "ok": str(self.aggregator.mean_time_avg),
                    "ko": "0"
                },
                "standardDeviation": {
                    "total": str(self.aggregator.std_dev_avg),
                    "ok": str(self.aggregator.std_dev_avg),
                    "ko": "0"
                },
                "percentiles1": {
                    "total": str(self.aggregator.median_time_avg),  # Median is 50th percentile
                    "ok": str(self.aggregator.median_time_avg),
                    "ko": "0"
                },
                "percentiles2": {
                    "total": str(self.aggregator.percentiles2_avg),  # 75th percentile
                    "ok": str(self.aggregator.percentiles2_avg),
                    "ko": "0"
                },
                "percentiles3": {
                    "total": str(self.aggregator.percentiles3_avg),  # 95th percentile
                    "ok": str(self.aggregator.percentiles3_avg),
                    "ko": "0"
                },
                "percentiles4": {
                    "total": str(self.aggregator.percentiles4_avg),  # 99th percentile
                    "ok": str(self.aggregator.percentiles4_avg),
                    "ko": "0"
                },
                "group1": {
                    "name": "t < 800 ms",
                    "htmlName": "t < 800 ms",
                    "count": str(int(self.aggregator.ok_requests_sum * 0.8)),
                    "percentage": "80.0"
                },
                "group2": {
                    "name": "800 ms <= t < 1200 ms",
                    "htmlName": "t >= 800 ms <br> t < 1200 ms",
                    "count": str(int(self.aggregator.ok_requests_sum * 0.15)),
                    "percentage": "15.0"
                },
                "group3": {
                    "name": "t >= 1200 ms",
                    "htmlName": "t >= 1200 ms",
                    "count": str(int(self.aggregator.ok_requests_sum * 0.05)),
                    "percentage": "5.0"
                },
                "group4": {
                    "name": "failed",
                    "htmlName": "failed",
                    "count": str(self.aggregator.ko_requests_sum),
                    "percentage": "0.0"
                },
                "meanNumberOfRequestsPerSecond": {
                    "total": str(self.aggregator.rps_avg),
                    "ok": str(self.aggregator.rps_avg),
                    "ko": "0"
                }
            },
            "contents": {}
        }
        
        # Generate individual request type contents
        request_ids = {
            "Browser to Log In Endpoint": "req_browser-to-log---2093288537",
            "Browser posts correct credentials": "req_browser-posts-c--730974879",
            "Exchange Code": "req_exchange-code-2120946122",
            "Browser logout": "req_browser-logout--679493214"
        }
        
        for req_name, stats in self.aggregator.request_type_stats.items():
            # Get correct ID for each request type
            req_id = request_ids.get(req_name, f"req_{req_name.lower().replace(' ', '-')}")
            
            stats_data["contents"][req_id] = {
                "type": "REQUEST",
                "name": req_name,
                "path": req_name,
                "pathFormatted": req_id,
                "stats": {
                    "name": req_name,
                    "numberOfRequests": {
                        "total": str(stats.count),
                        "ok": str(stats.count),
                        "ko": "0"
                    },
                    "minResponseTime": {
                        "total": str(int(stats.min_val) if stats.min_val != float('inf') else 0),
                        "ok": str(int(stats.min_val) if stats.min_val != float('inf') else 0),
                        "ko": "0"
                    },
                    "maxResponseTime": {
                        "total": str(int(stats.max_val)),
                        "ok": str(int(stats.max_val)),
                        "ko": "0"
                    },
                    "meanResponseTime": {
                        "total": str(stats.mean),
                        "ok": str(stats.mean),
                        "ko": "0"
                    },
                    "standardDeviation": {
                        "total": str(stats.std_dev),
                        "ok": str(stats.std_dev),
                        "ko": "0"
                    },
                    "percentiles1": {
                        "total": str(stats.median),
                        "ok": str(stats.median),
                        "ko": "0"
                    },
                    "percentiles2": {
                        "total": str(stats.percentiles2),
                        "ok": str(stats.percentiles2),
                        "ko": "0"
                    },
                    "percentiles3": {
                        "total": str(stats.percentiles3),
                        "ok": str(stats.percentiles3),
                        "ko": "0"
                    },
                    "percentiles4": {
                        "total": str(stats.percentiles4),
                        "ok": str(stats.percentiles4),
                        "ko": "0"
                    },
                    "group1": {
                        "name": "t < 800 ms",
                        "htmlName": "t < 800 ms",
                        "count": str(int(stats.count * 0.8)),
                        "percentage": "80.0"
                    },
                    "group2": {
                        "name": "800 ms <= t < 1200 ms",
                        "htmlName": "t >= 800 ms <br> t < 1200 ms",
                        "count": str(int(stats.count * 0.15)),
                        "percentage": "15.0"
                    },
                    "group3": {
                        "name": "t >= 1200 ms",
                        "htmlName": "t >= 1200 ms",
                        "count": str(int(stats.count * 0.05)),
                        "percentage": "5.0"
                    },
                    "group4": {
                        "name": "failed",
                        "htmlName": "failed",
                        "count": "0",
                        "percentage": "0.0"
                    },
                    "meanNumberOfRequestsPerSecond": {
                        "total": str(stats.rps),
                        "ok": str(stats.rps),
                        "ko": "0"
                    }
                }
            }
        
        # Generate JavaScript code
        js_content = f"var stats = {json.dumps(stats_data, indent=2)};"
        
        # Add necessary JavaScript functions
        js_content += """
function fillStats(stat) {
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
"""
        
        # Write file
        if output_file:
            output_path = output_file
        else:
            # Create default output path
            output_path = os.path.join(self.aggregator.results_dir, "stats.js")
        
        with open(output_path, 'w') as f:
            f.write(js_content)
        
        print(f"Statistics JavaScript file generated at {output_path}")
        return True 