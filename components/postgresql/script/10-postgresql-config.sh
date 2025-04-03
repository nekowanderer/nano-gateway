#!/bin/bash
set -e

cp /etc/postgresql/postgresql.conf /var/lib/postgresql/data/postgresql.conf
chown postgres:postgres /var/lib/postgresql/data/postgresql.conf

echo "Configuration file copied to data directory"
