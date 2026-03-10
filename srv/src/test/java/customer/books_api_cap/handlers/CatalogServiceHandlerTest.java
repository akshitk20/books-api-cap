package customer.books_api_cap.handlers;

import cds.gen.catalogservice.Books;
import com.sap.cds.Result;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.persistence.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceHandlerTest {

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private EventContext eventContext;

    @Mock
    private Result result;

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
        when(persistenceService.run(any(CqnSelect.class))).thenReturn(result);
        when(result.listOf(Books.class)).thenReturn(mockBooks);

        handler.filterBooksWithHighStock(eventContext);

        verify(persistenceService).run(any(CqnSelect.class));
        verify(result).listOf(Books.class);
        verify(eventContext).setCompleted();
        verify(eventContext).put("result", mockBooks);
    }

    @Test
    void filterBooksWithHighStock_shouldSetEventAsCompleted() {
        when(persistenceService.run(any(CqnSelect.class))).thenReturn(result);
        when(result.listOf(Books.class)).thenReturn(mockBooks);

        handler.filterBooksWithHighStock(eventContext);

        verify(eventContext).setCompleted();
    }

    @Test
    void filterBooksWithHighStock_withEmptyResult_shouldReturnEmptyList() {
        when(persistenceService.run(any(CqnSelect.class))).thenReturn(result);
        when(result.listOf(Books.class)).thenReturn(List.of());

        handler.filterBooksWithHighStock(eventContext);

        verify(eventContext).put("result", List.of());
        verify(eventContext).setCompleted();
    }
}
