#!/bin/bash
ENV=$1

if [ "$ENV" == "dev" ]; then
  echo "dev-server-01"
  echo "dev-server-02"
elif [ "$ENV" == "qa" ]; then
  echo "qa-server-01"
elif [ "$ENV" == "prod" ]; then
  echo "prod-server-01"
  echo "prod-server-02"
  echo "prod-server-03"
else
  echo "unknown-server"
fi