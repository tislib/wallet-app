package net.tislib.walletapp.model;

/**
 * Enum representing the possible statuses of a transaction.
 */
public enum TransactionStatus {
    /**
     * Transaction has been created but not yet processed.
     */
    PENDING,
    
    /**
     * Transaction is currently being processed.
     */
    EXECUTING,
    
    /**
     * Transaction has been successfully completed.
     */
    DONE,
    
    /**
     * Transaction has failed.
     */
    FAILED
}