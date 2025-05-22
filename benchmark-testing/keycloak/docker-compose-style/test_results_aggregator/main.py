#!/usr/bin/env python3

"""
Command-line interface for benchmark test results aggregator.
"""

import os
import sys
import argparse

from .aggregator import ResultsAggregator
from .reporters import JsonReporter, HtmlReporter, JavaScriptReporter
from .utils import ensure_directory


def main():
    """Main entry point"""
    # Parse command line arguments
    parser = argparse.ArgumentParser(description="Keycloak Benchmark Test Results Aggregator")
    parser.add_argument("scenario", help="Test scenario name, e.g.: authorizationcode")
    parser.add_argument("--dir", "-d", default="./results", help="Results directory path (default: ./results)")
    parser.add_argument("--output", "-o", help="JSON summary output file path")
    parser.add_argument("--html", "-html", help="HTML report output directory path")
    parser.add_argument("--aggregated", "-a", default="./aggregated-report", help="Update existing aggregated report directory (default: ./aggregated-report)")
    args = parser.parse_args()
    
    # Check if results directory exists
    if not os.path.isdir(args.dir):
        print(f"Error: Results directory '{args.dir}' does not exist")
        sys.exit(1)
    
    # Create aggregator and process results
    aggregator = ResultsAggregator(args.scenario, args.dir)
    aggregator.find_result_files()
    aggregator.aggregate_results()
    aggregator.print_aggregated_summary()
    
    # Export JSON summary if needed
    if args.output:
        json_reporter = JsonReporter(aggregator)
        json_reporter.export_json_summary(args.output)
    
    # Export HTML report if needed
    if args.html:
        if aggregator.find_html_template():
            html_reporter = HtmlReporter(aggregator)
            if html_reporter.export_html_report(args.html):
                abs_path = os.path.abspath(os.path.join(args.html, 'index.html'))
                print(f"Please open the following file in a browser to view the HTML report: file://{abs_path}")
        else:
            print("Could not find HTML template for report generation")
    
    # Update aggregated report if needed
    if os.path.exists(args.aggregated) or args.aggregated:
        ensure_directory(args.aggregated)
        if aggregator.find_html_template():
            html_reporter = HtmlReporter(aggregator)
            js_reporter = JavaScriptReporter(aggregator)
            
            if html_reporter.export_html_report(args.aggregated):
                # Generate stats.js file
                stats_js_path = os.path.join(args.aggregated, "js", "stats.js")
                js_reporter.generate_stats_js(stats_js_path)
                
                abs_path = os.path.abspath(os.path.join(args.aggregated, 'index.html'))
                print(f"Please open the following file in a browser to view the updated aggregated report: file://{abs_path}")
        else:
            print("Could not find HTML template for report generation")


if __name__ == "__main__":
    main() 