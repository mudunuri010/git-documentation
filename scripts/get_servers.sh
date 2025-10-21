#!/bin/sh
ENVIRONMENT=$1
case "$ENVIRONMENT" in
  dev)
    echo "dev-server-1"
    echo "dev-server-2"
    ;;
  staging)
    echo "staging-server-1"
    echo "staging-server-2"
    ;;
  prod)
    echo "prod-server-1"
    echo "prod-server-2"
    ;;
  *)
    echo "No servers available"
    ;;
esac
