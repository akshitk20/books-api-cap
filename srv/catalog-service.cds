using {my.bookshop as db} from '../db/schema';

@path: '/CatalogService'
service CatalogService {
  @readonly
  entity Books as projection on db.Books {
    key ID,
    title,
    author,
    publisher,
    reviews
  }
  type BookInfo {
    title : String;
    author : String;
  }

  @readonly
  entity Publishers as projection on db.Publishers {
    key ID,
    name,
    country,
    books
  }

  @readonly
  entity Authors as projection on db.Authors {
    key ID,
    name,
    birthDate,
    nationality,
    biography,
    books
  }

  @readonly
  entity Reviews as projection on db.Reviews {
    key ID,
    rating,
    comment,
    book
  }

  // Custom function to get books with high stock
  function getHighStockBooks() returns array of BookInfo;
}