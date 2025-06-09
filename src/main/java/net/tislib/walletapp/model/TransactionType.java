package net.tislib.walletapp.model;

/**
 * Enum representing the types of transactions that can be performed.
 */
public enum TransactionType {
    /**
     * Represents a deposit transaction (adding money to an account).
     */
    DEPOSIT,

    /**
     * Represents a withdrawal transaction (removing money from an account).
     */
    WITHDRAW,

    /**
     * Represents a transfer transaction (moving money from one account to another).
     */
    TRANSFER

    // Additional transaction types can be added here in the future
}
