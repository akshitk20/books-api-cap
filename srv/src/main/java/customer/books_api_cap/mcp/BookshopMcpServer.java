package customer.books_api_cap.mcp;

import cds.gen.catalogservice.Books;
import cds.gen.catalogservice.Books_;
import cds.gen.catalogservice.Authors;
import cds.gen.catalogservice.Authors_;
import cds.gen.catalogservice.Publishers;
import cds.gen.catalogservice.Publishers_;
import cds.gen.catalogservice.Reviews;
import cds.gen.catalogservice.Reviews_;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

/**
 * Bookshop-specific MCP Server Implementation
 * Exposes bookshop catalog operations as MCP tools and resources
 */
@Component
public class BookshopMcpServer {

    private static final Logger LOGGER = Logger.getLogger(BookshopMcpServer.class.getName());

    @Autowired
    private PersistenceService persistenceService;

    private final Gson gson = new Gson();
    private McpServer mcpServer;

    public void start() {
        mcpServer = new McpServer();

        // Register tools
        mcpServer.registerHandler("tools/list", this::handleToolsList);
        mcpServer.registerHandler("tools/call", this::handleToolCall);

        // Register resources
        mcpServer.registerHandler("resources/list", this::handleResourcesList);
        mcpServer.registerHandler("resources/read", this::handleResourceRead);

        LOGGER.info("Starting Bookshop MCP Server...");
        mcpServer.start();
    }

    private JsonObject handleToolsList(JsonObject params) {
        JsonArray tools = new JsonArray();

        // Tool: List all books
        JsonObject listBooks = new JsonObject();
        listBooks.addProperty("name", "list_books");
        listBooks.addProperty("description", "List all books in the catalog. Returns book details including title, author, stock, and publisher.");
        JsonObject listBooksSchema = new JsonObject();
        listBooksSchema.addProperty("type", "object");
        listBooksSchema.add("properties", new JsonObject());
        listBooks.add("inputSchema", listBooksSchema);
        tools.add(listBooks);

        // Tool: Search books
        JsonObject searchBooks = new JsonObject();
        searchBooks.addProperty("name", "search_books");
        searchBooks.addProperty("description", "Search for books by title. Returns matching books with full details.");
        JsonObject searchBooksSchema = new JsonObject();
        searchBooksSchema.addProperty("type", "object");
        JsonObject searchProps = new JsonObject();
        JsonObject titleProp = new JsonObject();
        titleProp.addProperty("type", "string");
        titleProp.addProperty("description", "Title or partial title to search for");
        searchProps.add("title", titleProp);
        searchBooksSchema.add("properties", searchProps);
        JsonArray required = new JsonArray();
        required.add("title");
        searchBooksSchema.add("required", required);
        searchBooks.add("inputSchema", searchBooksSchema);
        tools.add(searchBooks);

        // Tool: Get book by ID
        JsonObject getBook = new JsonObject();
        getBook.addProperty("name", "get_book");
        getBook.addProperty("description", "Get detailed information about a specific book by its ID, including reviews.");
        JsonObject getBookSchema = new JsonObject();
        getBookSchema.addProperty("type", "object");
        JsonObject getBookProps = new JsonObject();
        JsonObject idProp = new JsonObject();
        idProp.addProperty("type", "string");
        idProp.addProperty("description", "The unique identifier (UUID) of the book");
        getBookProps.add("id", idProp);
        getBookSchema.add("properties", getBookProps);
        JsonArray getBookRequired = new JsonArray();
        getBookRequired.add("id");
        getBookSchema.add("required", getBookRequired);
        getBook.add("inputSchema", getBookSchema);
        tools.add(getBook);

        // Tool: List authors
        JsonObject listAuthors = new JsonObject();
        listAuthors.addProperty("name", "list_authors");
        listAuthors.addProperty("description", "List all authors in the catalog with their biographical information.");
        JsonObject listAuthorsSchema = new JsonObject();
        listAuthorsSchema.addProperty("type", "object");
        listAuthorsSchema.add("properties", new JsonObject());
        listAuthors.add("inputSchema", listAuthorsSchema);
        tools.add(listAuthors);

        // Tool: Get author details
        JsonObject getAuthor = new JsonObject();
        getAuthor.addProperty("name", "get_author");
        getAuthor.addProperty("description", "Get detailed information about an author including their books.");
        JsonObject getAuthorSchema = new JsonObject();
        getAuthorSchema.addProperty("type", "object");
        JsonObject getAuthorProps = new JsonObject();
        JsonObject authorIdProp = new JsonObject();
        authorIdProp.addProperty("type", "string");
        authorIdProp.addProperty("description", "The unique identifier of the author");
        getAuthorProps.add("id", authorIdProp);
        getAuthorSchema.add("properties", getAuthorProps);
        JsonArray getAuthorRequired = new JsonArray();
        getAuthorRequired.add("id");
        getAuthorSchema.add("required", getAuthorRequired);
        getAuthor.add("inputSchema", getAuthorSchema);
        tools.add(getAuthor);

        // Tool: List publishers
        JsonObject listPublishers = new JsonObject();
        listPublishers.addProperty("name", "list_publishers");
        listPublishers.addProperty("description", "List all publishers in the catalog.");
        JsonObject listPubSchema = new JsonObject();
        listPubSchema.addProperty("type", "object");
        listPubSchema.add("properties", new JsonObject());
        listPublishers.add("inputSchema", listPubSchema);
        tools.add(listPublishers);

        // Tool: Get books by stock level
        JsonObject highStockBooks = new JsonObject();
        highStockBooks.addProperty("name", "get_high_stock_books");
        highStockBooks.addProperty("description", "Get all books with stock above a specified threshold.");
        JsonObject highStockSchema = new JsonObject();
        highStockSchema.addProperty("type", "object");
        JsonObject highStockProps = new JsonObject();
        JsonObject minStockProp = new JsonObject();
        minStockProp.addProperty("type", "integer");
        minStockProp.addProperty("description", "Minimum stock level (default: 300)");
        minStockProp.addProperty("default", 300);
        highStockProps.add("minStock", minStockProp);
        highStockSchema.add("properties", highStockProps);
        highStockBooks.add("inputSchema", highStockSchema);
        tools.add(highStockBooks);

        // Tool: Get book reviews
        JsonObject getReviews = new JsonObject();
        getReviews.addProperty("name", "get_book_reviews");
        getReviews.addProperty("description", "Get all reviews for a specific book.");
        JsonObject getReviewsSchema = new JsonObject();
        getReviewsSchema.addProperty("type", "object");
        JsonObject getReviewsProps = new JsonObject();
        JsonObject bookIdProp = new JsonObject();
        bookIdProp.addProperty("type", "string");
        bookIdProp.addProperty("description", "The book ID to get reviews for");
        getReviewsProps.add("bookId", bookIdProp);
        getReviewsSchema.add("properties", getReviewsProps);
        JsonArray getReviewsRequired = new JsonArray();
        getReviewsRequired.add("bookId");
        getReviewsSchema.add("required", getReviewsRequired);
        getReviews.add("inputSchema", getReviewsSchema);
        tools.add(getReviews);

        JsonObject result = new JsonObject();
        result.add("tools", tools);
        return result;
    }

    private JsonObject handleToolCall(JsonObject params) {
        try {
            String toolName = params.get("name").getAsString();
            JsonObject arguments = params.has("arguments") ?
                params.get("arguments").getAsJsonObject() : new JsonObject();

            String resultText;

            switch (toolName) {
                case "list_books":
                    resultText = listBooks();
                    break;
                case "search_books":
                    String title = arguments.get("title").getAsString();
                    resultText = searchBooks(title);
                    break;
                case "get_book":
                    String bookId = arguments.get("id").getAsString();
                    resultText = getBook(bookId);
                    break;
                case "list_authors":
                    resultText = listAuthors();
                    break;
                case "get_author":
                    String authorId = arguments.get("id").getAsString();
                    resultText = getAuthor(authorId);
                    break;
                case "list_publishers":
                    resultText = listPublishers();
                    break;
                case "get_high_stock_books":
                    int minStock = arguments.has("minStock") ?
                        arguments.get("minStock").getAsInt() : 300;
                    resultText = getHighStockBooks(minStock);
                    break;
                case "get_book_reviews":
                    String reviewBookId = arguments.get("bookId").getAsString();
                    resultText = getBookReviews(reviewBookId);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tool: " + toolName);
            }

            JsonArray content = new JsonArray();
            JsonObject textContent = new JsonObject();
            textContent.addProperty("type", "text");
            textContent.addProperty("text", resultText);
            content.add(textContent);

            JsonObject result = new JsonObject();
            result.add("content", content);
            return result;

        } catch (Exception e) {
            LOGGER.severe("Error executing tool: " + e.getMessage());
            throw new RuntimeException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    private String listBooks() {
        CqnSelect query = Select.from(Books_.class);
        List<Books> books = persistenceService.run(query).listOf(Books.class);

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(books.size()).append(" books in catalog:\n\n");

        for (Books book : books) {
            result.append("• ").append(book.getTitle())
                  .append(" (ID: ").append(book.getId()).append(")\n");
            if (book.getStock() != null) {
                result.append("  Stock: ").append(book.getStock()).append("\n");
            }
        }

        return result.toString();
    }

    private String searchBooks(String searchTitle) {
        CqnSelect query = Select.from(Books_.class)
            .where(b -> b.title().contains(searchTitle));
        List<Books> books = persistenceService.run(query).listOf(Books.class);

        if (books.isEmpty()) {
            return "No books found matching: " + searchTitle;
        }

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(books.size()).append(" book(s) matching '")
              .append(searchTitle).append("':\n\n");

        for (Books book : books) {
            result.append("📚 ").append(book.getTitle()).append("\n");
            result.append("   ID: ").append(book.getId()).append("\n");
            if (book.getStock() != null) {
                result.append("   Stock: ").append(book.getStock()).append(" copies\n");
            }
            result.append("\n");
        }

        return result.toString();
    }

    private String getBook(String bookId) {
        CqnSelect query = Select.from(Books_.class).where(b -> b.ID().eq(bookId));
        Books book = persistenceService.run(query).single(Books.class);

        if (book == null) {
            return "Book not found with ID: " + bookId;
        }

        StringBuilder result = new StringBuilder();
        result.append("📚 ").append(book.getTitle()).append("\n");
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        result.append("ID: ").append(book.getId()).append("\n");
        if (book.getStock() != null) {
            result.append("Stock: ").append(book.getStock()).append(" copies\n");
        }

        return result.toString();
    }

    private String listAuthors() {
        CqnSelect query = Select.from(Authors_.class);
        List<Authors> authors = persistenceService.run(query).listOf(Authors.class);

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(authors.size()).append(" authors:\n\n");

        for (Authors author : authors) {
            result.append("✍️  ").append(author.getName())
                  .append(" (ID: ").append(author.getId()).append(")\n");
            if (author.getNationality() != null) {
                result.append("   Nationality: ").append(author.getNationality()).append("\n");
            }
            if (author.getBirthDate() != null) {
                result.append("   Birth Date: ").append(author.getBirthDate()).append("\n");
            }
        }

        return result.toString();
    }

    private String getAuthor(String authorId) {
        CqnSelect query = Select.from(Authors_.class).where(a -> a.ID().eq(authorId));
        Authors author = persistenceService.run(query).single(Authors.class);

        if (author == null) {
            return "Author not found with ID: " + authorId;
        }

        StringBuilder result = new StringBuilder();
        result.append("✍️  ").append(author.getName()).append("\n");
        result.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        result.append("ID: ").append(author.getId()).append("\n");
        if (author.getNationality() != null) {
            result.append("Nationality: ").append(author.getNationality()).append("\n");
        }
        if (author.getBirthDate() != null) {
            result.append("Birth Date: ").append(author.getBirthDate()).append("\n");
        }
        if (author.getBiography() != null) {
            result.append("\nBiography:\n").append(author.getBiography()).append("\n");
        }

        return result.toString();
    }

    private String listPublishers() {
        CqnSelect query = Select.from(Publishers_.class);
        List<Publishers> publishers = persistenceService.run(query).listOf(Publishers.class);

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(publishers.size()).append(" publishers:\n\n");

        for (Publishers publisher : publishers) {
            result.append("🏢 ").append(publisher.getName())
                  .append(" (ID: ").append(publisher.getId()).append(")\n");
            if (publisher.getCountry() != null) {
                result.append("   Country: ").append(publisher.getCountry()).append("\n");
            }
        }

        return result.toString();
    }

    private String getHighStockBooks(int minStock) {
        CqnSelect query = Select.from(Books_.class)
            .where(b -> b.stock().ge(minStock));
        List<Books> books = persistenceService.run(query).listOf(Books.class);

        StringBuilder result = new StringBuilder();
        result.append("Books with stock >= ").append(minStock).append(":\n\n");

        if (books.isEmpty()) {
            result.append("No books found with stock level >= ").append(minStock);
        } else {
            for (Books book : books) {
                result.append("📚 ").append(book.getTitle())
                      .append(" - ").append(book.getStock()).append(" copies\n");
            }
        }

        return result.toString();
    }

    private String getBookReviews(String bookId) {
        CqnSelect query = Select.from(Reviews_.class)
            .where(r -> r.book_ID().eq(bookId));
        List<Reviews> reviews = persistenceService.run(query).listOf(Reviews.class);

        StringBuilder result = new StringBuilder();
        result.append("Reviews for book ID ").append(bookId).append(":\n\n");

        if (reviews.isEmpty()) {
            result.append("No reviews found for this book.");
        } else {
            for (Reviews review : reviews) {
                result.append("⭐ Rating: ").append(review.getRating()).append("/5\n");
                if (review.getComment() != null) {
                    result.append("   Comment: ").append(review.getComment()).append("\n");
                }
                result.append("\n");
            }
        }

        return result.toString();
    }

    private JsonObject handleResourcesList(JsonObject params) {
        JsonArray resources = new JsonArray();

        // Resource: Books catalog
        JsonObject booksResource = new JsonObject();
        booksResource.addProperty("uri", "bookshop://catalog/books");
        booksResource.addProperty("name", "Books Catalog");
        booksResource.addProperty("description", "Complete catalog of all books");
        booksResource.addProperty("mimeType", "application/json");
        resources.add(booksResource);

        // Resource: Authors catalog
        JsonObject authorsResource = new JsonObject();
        authorsResource.addProperty("uri", "bookshop://catalog/authors");
        authorsResource.addProperty("name", "Authors Catalog");
        authorsResource.addProperty("description", "Complete list of all authors");
        authorsResource.addProperty("mimeType", "application/json");
        resources.add(authorsResource);

        // Resource: Publishers catalog
        JsonObject publishersResource = new JsonObject();
        publishersResource.addProperty("uri", "bookshop://catalog/publishers");
        publishersResource.addProperty("name", "Publishers Catalog");
        publishersResource.addProperty("description", "Complete list of all publishers");
        publishersResource.addProperty("mimeType", "application/json");
        resources.add(publishersResource);

        JsonObject result = new JsonObject();
        result.add("resources", resources);
        return result;
    }

    private JsonObject handleResourceRead(JsonObject params) {
        String uri = params.get("uri").getAsString();

        String content;
        String mimeType = "application/json";

        switch (uri) {
            case "bookshop://catalog/books":
                content = getBooksJson();
                break;
            case "bookshop://catalog/authors":
                content = getAuthorsJson();
                break;
            case "bookshop://catalog/publishers":
                content = getPublishersJson();
                break;
            default:
                throw new IllegalArgumentException("Unknown resource URI: " + uri);
        }

        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        contentObj.addProperty("uri", uri);
        contentObj.addProperty("mimeType", mimeType);
        contentObj.addProperty("text", content);
        contents.add(contentObj);

        JsonObject result = new JsonObject();
        result.add("contents", contents);
        return result;
    }

    private String getBooksJson() {
        CqnSelect query = Select.from(Books_.class);
        List<Books> books = persistenceService.run(query).listOf(Books.class);
        return gson.toJson(books);
    }

    private String getAuthorsJson() {
        CqnSelect query = Select.from(Authors_.class);
        List<Authors> authors = persistenceService.run(query).listOf(Authors.class);
        return gson.toJson(authors);
    }

    private String getPublishersJson() {
        CqnSelect query = Select.from(Publishers_.class);
        List<Publishers> publishers = persistenceService.run(query).listOf(Publishers.class);
        return gson.toJson(publishers);
    }
}
