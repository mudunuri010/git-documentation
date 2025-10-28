#!/bin/bash
ENV=$1
case $ENV in
  dev) echo "3000" ;;
  qa) echo "4000" ;;
  staging) echo "5000" ;;
  prod) echo "6000" ;;
  *) echo "3000" ;;
esac

