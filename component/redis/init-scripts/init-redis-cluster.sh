#!/bin/bash

echo "Waiting for Redis nodes to be ready..."
sleep 5

echo "Initializing Redis Cluster..."
redis-cli --cluster create \
  redis-1:6379 redis-2:6380 redis-3:6381 \
  redis-4:6382 redis-5:6383 redis-6:6384 \
  --cluster-replicas 1

echo "Fixing Redis Cluster (if necessary)..."
yes 'yes'| redis-cli --cluster fix redis-1:6379

echo "Redis Cluster initialized successfully!"
