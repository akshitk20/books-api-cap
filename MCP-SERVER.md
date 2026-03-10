# Bookshop MCP Server

A Model Context Protocol (MCP) server implementation for the Bookshop API, enabling AI assistants like Claude to interact with the bookshop catalog through a standardized protocol.

## Features

### 🛠️ **Tools (8 Available)**

The MCP server exposes these tools for AI assistants:

1. **`list_books`** - List all books in the catalog
2. **`search_books`** - Search for books by title
3. **`get_book`** - Get detailed information about a specific book
4. **`list_authors`** - List all authors with biographical information
5. **`get_author`** - Get detailed author information including their books
6. **`list_publishers`** - List all publishers
7. **`get_high_stock_books`** - Find books with stock above a threshold
8. **`get_book_reviews`** - Get all reviews for a specific book

### 📚 **Resources (3 Available)**

The MCP server provides direct access to catalog data:

1. **`bookshop://catalog/books`** - Complete books catalog (JSON)
2. **`bookshop://catalog/authors`** - Complete authors catalog (JSON)
3. **`bookshop://catalog/publishers`** - Complete publishers catalog (JSON)

## Quick Start

### Prerequisites

- Java 21+
- Maven 3.6.3+
- Built project: `mvn clean install`

### Option 1: Using the Startup Script (Recommended)

```bash
./start-mcp-server.sh
```

### Option 2: Direct Java Command

```bash
java -jar srv/target/books-api-cap-exec.jar --mcp-mode
```

### Option 3: Maven Command

```bash
mvn spring-boot:run -pl srv -Dspring-boot.run.arguments=--mcp-mode
```

## Integration with Claude Code

To use this MCP server with Claude Code, add it to your MCP configuration:

### macOS/Linux Configuration

Edit: `~/.claude/config.json`

```json
{
  "mcpServers": {
    "bookshop": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/I771699/Documents/project/books-api-cap/srv/target/books-api-cap-exec.jar",
        "--mcp-mode"
      ],
      "env": {}
    }
  }
}
```

Or using the script:

```json
{
  "mcpServers": {
    "bookshop": {
      "command": "/Users/I771699/Documents/project/books-api-cap/start-mcp-server.sh",
      "args": []
    }
  }
}
```

### Windows Configuration

Edit: `%APPDATA%\.claude\config.json`

```json
{
  "mcpServers": {
    "bookshop": {
      "command": "java",
      "args": [
        "-jar",
        "C:\\path\\to\\books-api-cap\\srv\\target\\books-api-cap-exec.jar",
        "--mcp-mode"
      ]
    }
  }
}
```

## Usage Examples

Once configured, you can interact with the bookshop through Claude:

### Example Interactions

**"List all books in the catalog"**
```
Claude will call list_books tool and display all books
```

**"Find books with 'Jane' in the title"**
```
Claude will call search_books with title="Jane"
```

**"Show me books with more than 400 copies in stock"**
```
Claude will call get_high_stock_books with minStock=400
```

**"Tell me about author with ID 1"**
```
Claude will call get_author with id="1"
```

**"What are the reviews for book ID 2?"**
```
Claude will call get_book_reviews with bookId="2"
```

## Architecture

```
┌─────────────────┐
│   Claude Code   │
│   AI Assistant  │
└────────┬────────┘
         │ JSON-RPC 2.0
         │ (stdio)
         v
┌─────────────────┐
│   MCP Server    │
│  (McpServer)    │
└────────┬────────┘
         │
         v
┌─────────────────┐
│ BookshopMcpServer│
│   (Spring Bean) │
└────────┬────────┘
         │
         v
┌─────────────────┐
│ Persistence     │
│ Service (CDS)   │
└────────┬────────┘
         │
         v
┌─────────────────┐
│   H2 Database   │
└─────────────────┘
```

## Implementation Details

### Components

1. **`McpServer.java`**
   - Base MCP protocol implementation
   - JSON-RPC 2.0 handler
   - stdio transport layer
   - Protocol version: 2024-11-05

2. **`BookshopMcpServer.java`**
   - Bookshop-specific tools and resources
   - Spring integration
   - CDS query execution

3. **`Application.java`**
   - Detects `--mcp-mode` flag
   - Switches between web server and MCP server

### MCP Protocol Compliance

This implementation follows the [Model Context Protocol Specification](https://spec.modelcontextprotocol.io/):

- ✅ JSON-RPC 2.0 over stdio
- ✅ Server capabilities negotiation
- ✅ Tools with JSON Schema validation
- ✅ Resources with URI-based access
- ✅ Proper error handling
- ✅ Initialize/initialized handshake

## Development

### Testing the MCP Server

1. **Start the server:**
   ```bash
   ./start-mcp-server.sh
   ```

2. **Send test request (in another terminal):**
   ```bash
   echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}}}' | java -jar srv/target/books-api-cap-exec.jar --mcp-mode
   ```

### Adding New Tools

To add a new tool, edit `BookshopMcpServer.java`:

1. Add tool definition in `handleToolsList()`
2. Add handler case in `handleToolCall()`
3. Implement the query logic

Example:

```java
// In handleToolsList()
JsonObject newTool = new JsonObject();
newTool.addProperty("name", "my_new_tool");
newTool.addProperty("description", "Does something cool");
// ... add schema
tools.add(newTool);

// In handleToolCall()
case "my_new_tool":
    resultText = myNewToolLogic(arguments);
    break;

// Implementation
private String myNewToolLogic(JsonObject args) {
    // Your CDS query here
    return "Result";
}
```

## Debugging

### Enable Debug Logging

Set environment variable:

```bash
export LOGGING_LEVEL_CUSTOMER_BOOKS_API_CAP_MCP=DEBUG
java -jar srv/target/books-api-cap-exec.jar --mcp-mode
```

### Common Issues

**Issue:** "Connection refused" or server not starting
- **Solution:** Ensure the JAR is built: `mvn clean install`

**Issue:** "Class not found" errors
- **Solution:** Rebuild with: `mvn clean compile`

**Issue:** Claude Code doesn't see the tools
- **Solution:** Check `~/.claude/config.json` path is absolute
- Restart Claude Code after config changes

**Issue:** Database errors
- **Solution:** The H2 database is initialized on startup. Ensure no other instance is running.

## Performance

- **Cold start:** ~3-5 seconds (Spring Boot initialization)
- **Query response:** <100ms (in-memory H2 database)
- **Concurrent requests:** Single-threaded (stdio limitation)

## Security Considerations

⚠️ **Important:** This MCP server inherits the security posture of the main application:

- No authentication (anyone with access can query)
- Read-only operations only
- Runs with file system access of the user
- Should only be used in development/trusted environments

For production:
1. Add authentication to the Spring application
2. Implement MCP session tokens
3. Run in containerized environment
4. Use production database (not H2)

## Roadmap

- [ ] Add write operations (create/update books)
- [ ] Implement prompts for common queries
- [ ] Add pagination for large result sets
- [ ] Support for OData filters
- [ ] Cache frequently accessed data
- [ ] Metrics and monitoring

## Troubleshooting

### Verify Installation

```bash
# Check Java version
java -version  # Should be 21+

# Check Maven version
mvn -version   # Should be 3.6.3+

# Build project
mvn clean install

# Test MCP server
echo '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{}}' | ./start-mcp-server.sh
```

### Logs

MCP server logs are written to stderr:
- INFO level: Connection and request info
- WARN level: Errors and warnings
- Stdout: JSON-RPC responses only (don't pollute!)

## Contributing

When adding features:
1. Keep tools focused and single-purpose
2. Add comprehensive descriptions for AI understanding
3. Use proper JSON Schema for input validation
4. Return human-readable text responses
5. Log errors appropriately

## Resources

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [SAP CAP Documentation](https://cap.cloud.sap/docs/)
- [Claude Code Documentation](https://claude.ai/code)

## License

Same as the parent project.

---

**Built with:** Java 21 • Spring Boot 3.5.6 • SAP CAP 4.4.2 • Model Context Protocol 2024-11-05
