#!/usr/bin/env python3

"""
Execute the main program of the Keycloak test results aggregator.
This script serves as a simplified entry point for invoking the test_results_aggregator package.
"""

import sys
from test_results_aggregator.main import main

if __name__ == "__main__":
    # Pass command line arguments to the main program
    sys.exit(main()) 