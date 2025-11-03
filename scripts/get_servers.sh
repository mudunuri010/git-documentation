#!/bin/bash
# File: scripts/get_servers.sh
# Purpose: Return available servers for the given environment

ENV=$1

case "$ENV" in
    dev)
        echo "dev-server-01"
        echo "dev-server-02"
        ;;
    qa)
        echo "qa-server-01"
        echo "qa-server-02"
        echo "qa-server-03"
        ;;
    staging)
        echo "staging-server-01"
        ;;
    prod)
        echo "prod-server-01"
        echo "prod-server-02"
        echo "prod-server-03"
        echo "prod-server-04"
        ;;
    *)
        echo "unknown-server"
        ;;
esac
