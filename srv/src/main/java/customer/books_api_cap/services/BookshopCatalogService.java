package customer.books_api_cap.services;

import cds.gen.catalogservice.*;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cached Bookshop Catalog Service
 *
 * Provides cached access to catalog data with configured TTL.
 */
@Service
public class BookshopCatalogService {

    @Autowired
    private PersistenceService persistenceService;

    @Cacheable(value = "books:all")
    public List<Books> getAllBooks() {
        CqnSelect query = Select.from(Books_.class);
        return persistenceService.run(query).listOf(Books.class);
    }

    @Cacheable(value = "books:search", key = "#title")
    public List<Books> searchBooks(String title) {
        CqnSelect query = Select.from(Books_.class)
            .where(b -> b.title().contains(title));
        return persistenceService.run(query).listOf(Books.class);
    }

    @Cacheable(value = "book:single", key = "#bookId")
    public Books getBook(String bookId) {
        CqnSelect query = Select.from(Books_.class)
            .where(b -> b.ID().eq(bookId));
        return persistenceService.run(query).single(Books.class);
    }

    @Cacheable(value = "authors:all")
    public List<Authors> getAllAuthors() {
        CqnSelect query = Select.from(Authors_.class);
        return persistenceService.run(query).listOf(Authors.class);
    }

    @Cacheable(value = "author:single", key = "#authorId")
    public Authors getAuthor(String authorId) {
        CqnSelect query = Select.from(Authors_.class)
            .where(a -> a.ID().eq(authorId));
        return persistenceService.run(query).single(Authors.class);
    }

    @Cacheable(value = "publishers:all")
    public List<Publishers> getAllPublishers() {
        CqnSelect query = Select.from(Publishers_.class);
        return persistenceService.run(query).listOf(Publishers.class);
    }

    @Cacheable(value = "books:highstock", key = "#minStock")
    public List<Books> getHighStockBooks(int minStock) {
        CqnSelect query = Select.from(Books_.class)
            .where(b -> b.stock().ge(minStock));
        return persistenceService.run(query).listOf(Books.class);
    }

    @Cacheable(value = "reviews:book", key = "#bookId")
    public List<Reviews> getBookReviews(String bookId) {
        CqnSelect query = Select.from(Reviews_.class)
            .where(r -> r.book_ID().eq(bookId));
        return persistenceService.run(query).listOf(Reviews.class);
    }
}
