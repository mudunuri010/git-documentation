#!/bin/bash
ENV=$1

case $ENV in
  dev)
    echo "8085"
    ;;
  staging)
    echo "8086"
    ;;
  prod)
    echo "8087"
    ;;
  *)
    echo "8080"
    ;;
esac