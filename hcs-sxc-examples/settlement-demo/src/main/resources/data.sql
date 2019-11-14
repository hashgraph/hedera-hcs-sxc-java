DROP TABLE IF EXISTS credits;
DROP TABLE IF EXISTS credit;
CREATE TABLE credits (
    transaction_id VARCHAR2(100) PRIMARY KEY
    ,thread_id BIGINT NOT NULL
    ,payer_public_key VARCHAR(88) NOT NULL
    ,recipient_public_key VARCHAR(88) NOT NULL
    ,service_ref VARCHAR(50) DEFAULT NULL
    ,amount BIGINT NOT NULL
    ,currency VARCHAR(3) NOT NULL
    ,memo VARCHAR(100) DEFAULT NULL
    ,status VARCHAR(30) DEFAULT 'CREDIT_NEW'
);
