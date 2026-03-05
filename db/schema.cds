namespace my.bookshop;

using {cuid} from '@sap/cds/common';
using {managed} from '@sap/cds/common';

aspect Archivable {
  isArchived : Boolean default false;
  archivedAt : Timestamp;
}
entity Books : cuid, managed, Archivable {
  title  : String(100);
  author : Association to Authors;
  stock  : Integer;

  // Association to publishers
  publisher : Association to Publishers;

  // Composition of reviews(one to many, managed)
  reviews : Composition of many Reviews on reviews.book = $self;
}

entity Publishers : cuid, managed, Archivable {
  name : String;
  country : String;

  // Association to Books
  books : Association to many Books on books.publisher = $self;
}

entity Authors : cuid, managed, Archivable {
  name : String(100);
  birthDate : Date;
  nationality : String(50);
  biography : String(1000);

  // Association to Books
  books : Association to many Books on books.author = $self;
}

entity Reviews : cuid, managed , Archivable {
  key ID : Integer;
  rating : Integer;
  comment : String;

  // Association to parent
  book : Association to Books;
}