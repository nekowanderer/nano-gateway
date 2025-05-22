#!/usr/bin/env python3

"""
Utility functions for benchmark test results aggregator.
"""

import os
import shutil


def copy_template(src_dir, dst_dir):
    """Copy HTML report template"""
    try:
        # Copy all necessary resource files
        resource_dirs = ["style", "js"]
        for resource_dir in resource_dirs:
            src_resource_dir = os.path.join(src_dir, resource_dir)
            dst_resource_dir = os.path.join(dst_dir, resource_dir)
            
            if os.path.exists(src_resource_dir):
                if os.path.exists(dst_resource_dir):
                    shutil.rmtree(dst_resource_dir)
                shutil.copytree(src_resource_dir, dst_resource_dir)
        
        # Copy index.html
        src_index = os.path.join(src_dir, "index.html")
        dst_index = os.path.join(dst_dir, "index.html")
        
        if os.path.exists(src_index):
            shutil.copy2(src_index, dst_index)
            print(f"Template files copied to: {dst_dir}")
            return True
        else:
            print(f"Source index.html file not found: {src_index}")
            return False
    
    except (IOError, OSError) as e:
        print(f"Error copying template files: {e}")
        return False


def ensure_directory(directory):
    """Ensure directory exists, create if not"""
    if not os.path.exists(directory):
        os.makedirs(directory, exist_ok=True)
        print(f"Created directory: {directory}")
    return os.path.abspath(directory) 