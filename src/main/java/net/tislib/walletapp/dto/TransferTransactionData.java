package net.tislib.walletapp.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import net.tislib.walletapp.model.TransactionType;

import java.math.BigDecimal;

/**
 * Data for transfer transactions.
 */
@Data
@JsonTypeName("TRANSFER")
public final class TransferTransactionData implements TransactionData {
    private Long destinationAccountId;
    private BigDecimal amount;
    private String description;

    @Override
    public TransactionType type() {
        return TransactionType.TRANSFER;
    }
}