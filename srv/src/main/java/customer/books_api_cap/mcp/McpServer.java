package customer.books_api_cap.mcp;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Model Context Protocol (MCP) Server Implementation
 * Provides JSON-RPC 2.0 interface over stdio for AI assistants
 */
public class McpServer {

    private static final Logger LOGGER = Logger.getLogger(McpServer.class.getName());
    private static final String MCP_VERSION = "2024-11-05";

    private final Gson gson;
    private final Map<String, RequestHandler> handlers;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private boolean running;

    public McpServer() {
        this.gson = new Gson();
        this.handlers = new HashMap<>();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.writer = new PrintWriter(System.out, true);
        this.running = false;

        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        // Initialize method
        handlers.put("initialize", this::handleInitialize);

        // List methods
        handlers.put("tools/list", this::handleToolsList);
        handlers.put("resources/list", this::handleResourcesList);
        handlers.put("prompts/list", this::handlePromptsList);

        // Notifications
        handlers.put("notifications/initialized", this::handleInitializedNotification);
    }

    public void registerHandler(String method, RequestHandler handler) {
        handlers.put(method, handler);
    }

    public void start() {
        running = true;
        LOGGER.info("MCP Server started. Listening on stdio...");

        try {
            while (running) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                handleRequest(line);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading from stdin", e);
        } finally {
            stop();
        }
    }

    private void handleRequest(String line) {
        try {
            JsonObject request = JsonParser.parseString(line).getAsJsonObject();

            String method = request.has("method") ? request.get("method").getAsString() : null;
            JsonElement id = request.has("id") ? request.get("id") : null;
            JsonObject params = request.has("params") ? request.get("params").getAsJsonObject() : new JsonObject();

            if (method == null) {
                sendError(id, -32600, "Invalid Request: missing method");
                return;
            }

            RequestHandler handler = handlers.get(method);
            if (handler == null) {
                sendError(id, -32601, "Method not found: " + method);
                return;
            }

            JsonObject result = handler.handle(params);
            sendResponse(id, result);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling request", e);
            sendError(null, -32603, "Internal error: " + e.getMessage());
        }
    }

    private JsonObject handleInitialize(JsonObject params) {
        JsonObject result = new JsonObject();
        result.addProperty("protocolVersion", MCP_VERSION);

        JsonObject serverInfo = new JsonObject();
        serverInfo.addProperty("name", "bookshop-mcp-server");
        serverInfo.addProperty("version", "1.0.0");
        result.add("serverInfo", serverInfo);

        JsonObject capabilities = new JsonObject();

        JsonObject tools = new JsonObject();
        tools.addProperty("listChanged", false);
        capabilities.add("tools", tools);

        JsonObject resources = new JsonObject();
        resources.addProperty("subscribe", false);
        resources.addProperty("listChanged", false);
        capabilities.add("resources", resources);

        JsonObject prompts = new JsonObject();
        prompts.addProperty("listChanged", false);
        capabilities.add("prompts", prompts);

        result.add("capabilities", capabilities);

        return result;
    }

    private JsonObject handleToolsList(JsonObject params) {
        // Will be overridden by BookshopMcpServer
        JsonObject result = new JsonObject();
        result.add("tools", gson.toJsonTree(new Object[]{}));
        return result;
    }

    private JsonObject handleResourcesList(JsonObject params) {
        // Will be overridden by BookshopMcpServer
        JsonObject result = new JsonObject();
        result.add("resources", gson.toJsonTree(new Object[]{}));
        return result;
    }

    private JsonObject handlePromptsList(JsonObject params) {
        JsonObject result = new JsonObject();
        result.add("prompts", gson.toJsonTree(new Object[]{}));
        return result;
    }

    private JsonObject handleInitializedNotification(JsonObject params) {
        LOGGER.info("Client initialized notification received");
        return new JsonObject();
    }

    private void sendResponse(JsonElement id, JsonObject result) {
        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        response.add("id", id);
        response.add("result", result);

        writer.println(gson.toJson(response));
        writer.flush();
    }

    private void sendError(JsonElement id, int code, String message) {
        JsonObject error = new JsonObject();
        error.addProperty("code", code);
        error.addProperty("message", message);

        JsonObject response = new JsonObject();
        response.addProperty("jsonrpc", "2.0");
        if (id != null) {
            response.add("id", id);
        }
        response.add("error", error);

        writer.println(gson.toJson(response));
        writer.flush();
    }

    public void stop() {
        running = false;
        LOGGER.info("MCP Server stopped");
    }

    @FunctionalInterface
    public interface RequestHandler {
        JsonObject handle(JsonObject params) throws Exception;
    }
}
