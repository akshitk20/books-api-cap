package customer.books_api_cap.handlers;

import cds.gen.catalogservice.Books;
import customer.books_api_cap.services.BookshopCatalogService;
import com.sap.cds.services.EventContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceHandlerTest {

    @Mock
    private BookshopCatalogService catalogService;

    @Mock
    private EventContext eventContext;

    @InjectMocks
    private CatalogServiceHandler handler;

    private List<Books> mockBooks;

    @BeforeEach
    void setUp() {
        Books book1 = Books.create();
        book1.setTitle("Test Book 1");

        Books book2 = Books.create();
        book2.setTitle("Test Book 2");

        mockBooks = List.of(book1, book2);
    }

    @Test
    void filterBooksWithHighStock_shouldQueryAndReturnBooks() {
        when(catalogService.getHighStockBooks(anyInt())).thenReturn(mockBooks);

        handler.filterBooksWithHighStock(eventContext);

        verify(catalogService).getHighStockBooks(300);
        verify(eventContext).setCompleted();
        verify(eventContext).put("result", mockBooks);
    }

    @Test
    void filterBooksWithHighStock_shouldSetEventAsCompleted() {
        when(catalogService.getHighStockBooks(anyInt())).thenReturn(mockBooks);

        handler.filterBooksWithHighStock(eventContext);

        verify(eventContext).setCompleted();
    }

    @Test
    void filterBooksWithHighStock_withEmptyResult_shouldReturnEmptyList() {
        when(catalogService.getHighStockBooks(anyInt())).thenReturn(List.of());

        handler.filterBooksWithHighStock(eventContext);

        verify(eventContext).put("result", List.of());
        verify(eventContext).setCompleted();
    }
}
