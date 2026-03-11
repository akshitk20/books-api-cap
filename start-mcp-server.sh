#!/bin/bash
# Bookshop MCP Server Startup Script

# Change to the project root directory
cd "$(dirname "$0")"

# Build the project if needed
if [ ! -f "srv/target/books-api-cap-exec.jar" ]; then
    echo "Building project..."
    mvn clean install
fi

# Start the MCP server
echo "Starting Bookshop MCP Server..."
java -jar srv/target/books-api-cap-exec.jar --mcp-mode
