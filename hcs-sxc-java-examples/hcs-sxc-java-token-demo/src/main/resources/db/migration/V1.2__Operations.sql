create table operations (
  id                    BIGINT PRIMARY KEY NOT NULL
  ,operator             VARCHAR2(100) NOT NULL
  ,recipient            VARCHAR2(100)
  ,operation            VARCHAR2(200) NOT NULL
);