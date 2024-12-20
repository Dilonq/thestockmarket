CREATE TABLE IF NOT EXISTS ACCOUNTS(
    ID INT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(32) UNIQUE NOT NULL,
    HASHED_PASSWORD VARCHAR(255) NOT NULL,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PROVISIONAL_CASH_BALANCE INT DEFAULT 0,
    PROVISIONAL_CASH_BALANCE_UPDATE_TIMESTAMP TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS COMPANIES (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    OWNER_ACCOUNT_ID INT,
    TICKER VARCHAR(8),
    NAME VARCHAR(32),
    DESCRIPTION VARCHAR(255),
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (OWNER_ACCOUNT_ID) REFERENCES ACCOUNTS(ID)
);

CREATE TABLE IF NOT EXISTS TRANSACTIONS (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ACCOUNT_ID INT NOT NULL,
    AMOUNT DECIMAL(10, 2) NOT NULL,
    TRANSACTION_TYPE ENUM('DEPOSIT','WITHDRAWAL') NOT NULL,
    SECURITY_TYPE ENUM('CASH','SHARE','BOND','CALLOPTION','PUTOPTION','FUTURE') NOT NULL,
    COMPANY_ID INT,
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNTS(ID),
    FOREIGN KEY (COMPANY_ID) REFERENCES COMPANIES(ID),
    CHECK (COMPANY_ID IS NOT NULL OR SECURITY_TYPE = 'CASH')
);

CREATE TABLE IF NOT EXISTS SESSIONS (
    TOKEN VARCHAR(255) PRIMARY KEY,
    ACCOUNT_ID INT UNIQUE NOT NULL,

    FOREIGN KEY (ACCOUNT_ID) REFERENCES ACCOUNTS(ID)
);