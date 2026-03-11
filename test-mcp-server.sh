#!/bin/bash
# Simple MCP Server Test Script

echo "Testing Bookshop MCP Server..."
echo "================================"
echo ""

# Test 1: Initialize
echo "Test 1: Initialize handshake"
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test-client","version":"1.0"}}}' | timeout 10 java -jar srv/target/books-api-cap-exec.jar --mcp-mode 2>/dev/null | head -1
echo ""

echo "✅ If you see a response above with 'protocolVersion', the MCP server is working!"
echo ""
echo "Next steps:"
echo "1. Add to Claude Code config: ~/.claude/config.json"
echo "2. See MCP-SERVER.md for full configuration"
