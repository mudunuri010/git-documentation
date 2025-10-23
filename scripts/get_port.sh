#!/bin/bash
ENV=$1

if [ "$ENV" == "dev" ]; then
  echo "8081"
elif [ "$ENV" == "qa" ]; then
  echo "8082"
elif [ "$ENV" == "prod" ]; then
  echo "8080"
else
  echo "3000"
fi