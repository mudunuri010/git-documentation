#!/bin/bash
ENV=$1

case $ENV in
  dev)
    echo "dev-server-01"
    echo "dev-server-02"
    ;;
  qa)
    echo "qa-server-01"
    echo "qa-server-02"
    ;;
  staging)
    echo "staging-server-01"
    ;;
  prod)
    echo "prod-server-01"
    ;;
  *)
    echo "unknown-server"
    ;;
esac
