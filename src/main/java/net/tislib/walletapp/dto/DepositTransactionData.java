package net.tislib.walletapp.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import net.tislib.walletapp.model.TransactionType;

import java.math.BigDecimal;

/**
 * Data for deposit transactions.
 */
@Data
@JsonTypeName("DEPOSIT")
public final class DepositTransactionData implements TransactionData {
    private BigDecimal amount;
    private String description;

    @Override
    public TransactionType getType() {
        return TransactionType.DEPOSIT;
    }
}
