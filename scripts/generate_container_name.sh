#!/bin/sh
SERVER=$1
if [ -z "$SERVER" ]; then
  echo "default-container"
else
  echo "${SERVER}-container"
fi
