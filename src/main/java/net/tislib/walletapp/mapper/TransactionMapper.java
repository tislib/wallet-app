package net.tislib.walletapp.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.tislib.walletapp.dto.TransactionData;
import net.tislib.walletapp.dto.TransactionDto;
import net.tislib.walletapp.entity.AccountEntity;
import net.tislib.walletapp.entity.TransactionEntity;
import net.tislib.walletapp.repository.AccountRepository;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;

@Mapper(componentModel = "spring", uses = {})
public abstract class TransactionMapper {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "data", expression = "java(deserializeTransactionData(entity.getTransactionData()))")
    public abstract TransactionDto toDto(TransactionEntity entity);

    public abstract List<TransactionDto> toDtoList(List<TransactionEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", expression = "java(findAccountById(dto.getAccountId()))")
    @Mapping(target = "transactionData", expression = "java(serializeTransactionData(dto.getData()))")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract TransactionEntity toEntity(TransactionDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "account", expression = "java(dto.getAccountId() != null ? findAccountById(dto.getAccountId()) : entity.getAccount())")
    @Mapping(target = "transactionData", expression = "java(dto.getData() != null ? serializeTransactionData(dto.getData()) : entity.getTransactionData())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateEntityFromDto(TransactionDto dto, @MappingTarget TransactionEntity entity);

    protected AccountEntity findAccountById(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + accountId));
    }

    public String serializeTransactionData(TransactionData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing transaction data", e);
        }
    }

    public TransactionData deserializeTransactionData(String json) {
        try {
            return objectMapper.readValue(json, TransactionData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing transaction data", e);
        }
    }
}
