package net.tislib.walletapp.controller;

import lombok.RequiredArgsConstructor;
import net.tislib.walletapp.dto.TransactionDto;
import net.tislib.walletapp.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts/{accountId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getTransactionsByAccountId(@PathVariable Long accountId) {
        List<TransactionDto> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(@PathVariable Long accountId, @PathVariable Long id) {
        TransactionDto transaction = transactionService.getTransaction(id, accountId);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @PathVariable Long accountId,
            @RequestBody TransactionDto transactionDto) {

        // Ensure the transaction is associated with the correct account
        transactionDto.setAccountId(accountId);

        TransactionDto createdTransaction = transactionService.createTransaction(transactionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long accountId,
            @PathVariable Long id,
            @RequestBody TransactionDto transactionDto) {

        // Ensure the transaction is associated with the correct account
        transactionDto.setAccountId(accountId);

        TransactionDto updatedTransaction = transactionService.updateTransaction(id, accountId, transactionDto);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long accountId, @PathVariable Long id) {
        transactionService.deleteTransaction(id, accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/execute")
    public ResponseEntity<TransactionDto> executeTransaction(@PathVariable Long accountId, @PathVariable Long id) {
        TransactionDto executedTransaction = transactionService.executeTransaction(id, accountId);
        return ResponseEntity.ok(executedTransaction);
    }
}
