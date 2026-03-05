package customer.books_api_cap.handlers;

import cds.gen.catalogservice.Books;
import cds.gen.catalogservice.CatalogService_;
import com.sap.cds.ql.Select;
import com.sap.cds.ql.cqn.CqnSelect;
import com.sap.cds.services.EventContext;
import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cds.services.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ServiceName(CatalogService_.CDS_NAME)
public class CatalogServiceHandler implements EventHandler {
    @Autowired
    private PersistenceService persistenceService;

    @On(event = "getHighStockBooks")
    public void filterBooksWithHighStock(EventContext eventContext) {
        CqnSelect query = Select.from(cds.gen.catalogservice.Books_.class);
                //.where(b -> b.stock().gt(300));

        List<Books> books = persistenceService.run(query).listOf(Books.class);
        eventContext.setCompleted();
        eventContext.put("result", books);
    }
}
