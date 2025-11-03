# -----------------------------------------------------------

#!/bin/bash
# File: scripts/generate_container_name.sh
# Purpose: Generate container name based on server name

SERVER=$1

if [ -z "$SERVER" ]; then
    echo "error-no-server"
    exit 1
fi

# Remove special characters and convert to lowercase
CLEAN_SERVER=$(echo "$SERVER" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9-]/-/g')

# Generate container name
CONTAINER_NAME="git-doc-${CLEAN_SERVER}"

echo "$CONTAINER_NAME"
