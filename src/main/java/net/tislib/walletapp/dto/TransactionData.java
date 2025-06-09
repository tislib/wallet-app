package net.tislib.walletapp.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.tislib.walletapp.model.TransactionType;

/**
 * Sealed interface for transaction data with different implementations based on transaction type.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = DepositTransactionData.class, name = "DEPOSIT"),
    @JsonSubTypes.Type(value = WithdrawTransactionData.class, name = "WITHDRAW"),
    @JsonSubTypes.Type(value = TransferTransactionData.class, name = "TRANSFER")
})
public sealed interface TransactionData permits DepositTransactionData, WithdrawTransactionData, TransferTransactionData {
    /**
     * Gets the transaction type.
     *
     * @return the transaction type
     */
    TransactionType type();
}
