package customer.books_api_cap.mcp;

import customer.books_api_cap.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Standalone MCP Server Launcher
 * Run this to start the bookshop MCP server in stdio mode
 *
 * Usage: java -jar books-api-cap.jar --mcp-mode
 */
public class McpServerLauncher {

    public static void main(String[] args) {
        // Disable Spring Boot banner and web server for MCP mode
        System.setProperty("spring.main.banner-mode", "off");
        System.setProperty("spring.main.web-application-type", "none");
        System.setProperty("logging.level.root", "WARN");
        System.setProperty("logging.level.customer.books_api_cap.mcp", "INFO");

        // Start Spring context
        ConfigurableApplicationContext context =
            SpringApplication.run(Application.class, args);

        // Get MCP server bean and start it
        BookshopMcpServer mcpServer = context.getBean(BookshopMcpServer.class);
        mcpServer.start();

        // Shutdown Spring context when MCP server stops
        context.close();
    }
}
