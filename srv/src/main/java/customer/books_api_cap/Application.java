package customer.books_api_cap;

import customer.books_api_cap.mcp.BookshopMcpServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		// Check if running in MCP mode
		if (Arrays.asList(args).contains("--mcp-mode")) {
			runMcpMode(args);
		} else {
			// Normal web server mode
			SpringApplication.run(Application.class, args);
		}
	}

	private static void runMcpMode(String[] args) {
		// Configure for MCP mode (no web server, minimal logging)
		System.setProperty("spring.main.banner-mode", "off");
		System.setProperty("spring.main.web-application-type", "none");
		System.setProperty("logging.level.root", "WARN");
		System.setProperty("logging.level.customer.books_api_cap.mcp", "INFO");

		// Disable security for MCP mode (local development)
		System.setProperty("spring.security.enabled", "false");

		// Activate MCP profile (lighter caching)
		System.setProperty("spring.profiles.active", "mcp");

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

