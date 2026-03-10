package customer.books_api_cap.handlers;

import cds.gen.catalogservice.Books;
import cds.gen.catalogservice.CatalogService_;
import customer.books_api_cap.services.BookshopCatalogService;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ServiceName(CatalogService_.CDS_NAME)
public class CatalogServiceHandler implements EventHandler {
    @Autowired
    private BookshopCatalogService catalogService;

    @On(event = "getHighStockBooks")
    public void filterBooksWithHighStock(EventContext eventContext) {
        int minStock = 300;  // Default threshold

        List<Books> books = catalogService.getHighStockBooks(minStock);
        eventContext.setCompleted();
        eventContext.put("result", books);
    }
}
