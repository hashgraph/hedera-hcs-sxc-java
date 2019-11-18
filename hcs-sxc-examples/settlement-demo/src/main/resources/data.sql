DROP TABLE IF EXISTS credits;

CREATE TABLE credits (
    transaction_id VARCHAR2(100) PRIMARY KEY
    ,thread_id BIGINT NOT NULL
    ,payer_name VARCHAR(88) NOT NULL
    ,recipient_name VARCHAR(88) NOT NULL
    ,reference VARCHAR(50) DEFAULT NULL
    ,amount BIGINT NOT NULL
    ,currency VARCHAR(3) NOT NULL
    ,additional_notes VARCHAR(100) DEFAULT NULL
    ,status VARCHAR(30) DEFAULT 'CREDIT_NEW'
    ,created_date VARCHAR(30) NOT NULL
    ,created_time VARCHAR(30) NOT NULL
);

DROP TABLE IF EXISTS address_book;

CREATE TABLE address_book (
    public_key VARCHAR(88) PRIMARY KEY
    ,name VARCHAR(50) NOT NULL
    ,roles VARCHAR(100) NOT NULL
);