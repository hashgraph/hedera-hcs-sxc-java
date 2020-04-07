create sequence HIBERNATE_SEQUENCE;

create table behaviors (
  id                    BIGINT PRIMARY KEY NOT NULL
  ,name                 VARCHAR(100) NOT NULL
  ,UNIQUE KEY behavior_name (name)
);

create table token_templates (
  id                    BIGINT PRIMARY KEY NOT NULL
  ,name                 VARCHAR(100) NOT NULL
  ,UNIQUE KEY token_templates_name (name)
);

create table token_template_behaviors (
  token_template_id     BIGINT NOT NULL
  ,behavior_id          BIGINT NOT NULL
  ,UNIQUE KEY token_template_behavior(token_template_id, behavior_id)
);

create table tokens (
  id                    BIGINT PRIMARY KEY NOT NULL
  ,owner_user_id        BIGINT NOT NULL
  ,name                 VARCHAR(100) NOT NULL
  ,symbol               VARCHAR(10) NOT NULL
  ,decimals             INT
  ,quantity             BIGINT NOT NULL
  ,cap                  BIGINT NOT NULL
  ,balance              BIGINT NOT NULL
  ,UNIQUE KEY tokens_name (name)
);

create table token_behaviors (
  token_id              BIGINT NOT NULL
  ,behavior_id          BIGINT NOT NULL
  ,UNIQUE KEY token_behavior(token_id, behavior_id)
);

create table users (
  id                    BIGINT PRIMARY KEY NOT NULL
  ,name                 VARCHAR(100) NOT NULL
  ,public_keys          VARCHAR2(100)
  ,user_of              VARCHAR2(100)
);

create table accounts (
  id                    BIGINT PRIMARY KEY NOT NULL
  ,keys                 VARCHAR(100) 
  ,token_id             BIGINT NOT NULL
  ,balance              BIGINT NOT NULL
);

create table user_accounts (
  user_id               BIGINT NOT NULL
  ,account_id           BIGINT NOT NULL
  ,hedera_account_id    VARCHAR(100)
  ,UNIQUE KEY user_accounts(user_id, account_id)
);

create table account_behaviors (
  account_id            BIGINT NOT NULL
  ,behavior_id          BIGINT NOT NULL
  ,UNIQUE KEY account_behaviors(account_id, behavior_id)
);
