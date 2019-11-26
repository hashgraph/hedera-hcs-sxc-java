DROP TABLE IF EXISTS credits;

CREATE TABLE credits (
    thread_id VARCHAR(20) PRIMARY KEY
    ,transaction_id VARCHAR(100) 
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

DROP TABLE IF EXISTS settlements;

CREATE TABLE settlements (
    thread_id VARCHAR(20) PRIMARY KEY
    ,transaction_id VARCHAR(100) 
    ,payer_name VARCHAR(88) NOT NULL
    ,recipient_name VARCHAR(88) NOT NULL
    ,additional_notes VARCHAR(100) DEFAULT NULL
    ,net_value BIGINT NOT NULL
    ,currency VARCHAR(3) NOT NULL
    ,status VARCHAR(30) DEFAULT 'CREDIT_NEW'
    ,created_date VARCHAR(30) NOT NULL
    ,created_time VARCHAR(30) NOT NULL
);

DROP TABLE IF EXISTS settlement_items;

CREATE TABLE settlement_items (
    thread_id VARCHAR(20)
    ,settled_thread_id VARCHAR(20) 
);
