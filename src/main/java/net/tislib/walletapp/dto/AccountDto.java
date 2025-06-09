package net.tislib.walletapp.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {
    private Long id;
    private String name;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
