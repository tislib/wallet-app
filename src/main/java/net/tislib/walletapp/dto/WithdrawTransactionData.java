package net.tislib.walletapp.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import net.tislib.walletapp.model.TransactionType;

import java.math.BigDecimal;

/**
 * Data for withdraw transactions.
 */
@Data
@JsonTypeName("WITHDRAW")
public final class WithdrawTransactionData implements TransactionData {
    private BigDecimal amount;
    private String description;

    @Override
    public TransactionType getType() {
        return TransactionType.WITHDRAW;
    }
}