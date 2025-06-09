package net.tislib.walletapp.dto;

import lombok.Data;
import net.tislib.walletapp.model.TransactionStatus;
import net.tislib.walletapp.model.TransactionType;

import java.time.LocalDateTime;

@Data
public class TransactionDto {
    private Long id;
    private TransactionType type;
    private TransactionStatus status;
    private Long accountId;
    private TransactionData data;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}