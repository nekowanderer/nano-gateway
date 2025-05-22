#!/usr/bin/env python3

"""
Command line entry point for the benchmark_aggregator package.
Usage: python -m test_results_aggregator.run [args]
"""

import sys
import os

# Add the parent directory to the Python path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Use relative import
from .main import main

if __name__ == "__main__":
    main() 