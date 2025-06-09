package net.tislib.walletapp.service;

import lombok.RequiredArgsConstructor;
import net.tislib.walletapp.dto.DepositTransactionData;
import net.tislib.walletapp.dto.TransactionData;
import net.tislib.walletapp.dto.TransactionDto;
import net.tislib.walletapp.dto.TransferTransactionData;
import net.tislib.walletapp.dto.WithdrawTransactionData;
import net.tislib.walletapp.entity.AccountEntity;
import net.tislib.walletapp.entity.TransactionEntity;
import net.tislib.walletapp.mapper.TransactionMapper;
import net.tislib.walletapp.model.TransactionStatus;
import net.tislib.walletapp.model.TransactionType;
import net.tislib.walletapp.repository.AccountRepository;
import net.tislib.walletapp.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactionsByAccountId(Long accountId) {
        List<TransactionEntity> transactions = transactionRepository.findByAccountId(accountId);
        return transactionMapper.toDtoList(transactions);
    }

    @Transactional(readOnly = true)
    public TransactionDto getTransaction(Long id, Long accountId) {
        TransactionEntity transaction = transactionRepository.findByIdAndAccountId(id, accountId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id + " for account: " + accountId));
        return transactionMapper.toDto(transaction);
    }

    @Transactional
    public TransactionDto createTransaction(TransactionDto transactionDto) {
        validateTransactionDto(transactionDto);

        TransactionEntity transaction = transactionMapper.toEntity(transactionDto);
        transaction.setStatus(TransactionStatus.PENDING);
        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toDto(savedTransaction);
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, Long accountId, TransactionDto transactionDto) {
        validateTransactionDto(transactionDto);

        TransactionEntity existingTransaction = transactionRepository.findByIdAndAccountId(id, accountId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id + " for account: " + accountId));

        // Don't allow updating executed transactions
        if (existingTransaction.getStatus() == TransactionStatus.DONE) {
            throw new IllegalStateException("Cannot update a completed transaction");
        }

        // Ensure the transaction is associated with the correct account
        transactionDto.setAccountId(accountId);

        transactionMapper.updateEntityFromDto(transactionDto, existingTransaction);
        TransactionEntity updatedTransaction = transactionRepository.save(existingTransaction);

        return transactionMapper.toDto(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(Long id, Long accountId) {
        TransactionEntity transaction = transactionRepository.findByIdAndAccountId(id, accountId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id + " for account: " + accountId));

        // Don't allow deleting executed transactions
        if (transaction.getStatus() == TransactionStatus.DONE) {
            throw new IllegalStateException("Cannot delete a completed transaction");
        }

        transactionRepository.deleteById(id);
    }

    @Transactional
    public TransactionDto executeTransaction(Long id, Long accountId) {
        TransactionEntity transaction = transactionRepository.findByIdAndAccountIdWithLock(id, accountId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id + " for account: " + accountId));

        // Don't execute already completed transactions
        if (transaction.getStatus() == TransactionStatus.DONE) {
            return transactionMapper.toDto(transaction);
        }

        // Update status to EXECUTING
        transaction.setStatus(TransactionStatus.EXECUTING);
        transactionRepository.save(transaction);

        try {
            // Process the transaction based on its type
            switch (transaction.getType()) {
                case DEPOSIT:
                    processDeposit(transaction);
                    break;
                case WITHDRAW:
                    processWithdraw(transaction);
                    break;
                case TRANSFER:
                    processTransfer(transaction);
                    break;
                default:
                    throw new IllegalStateException("Unsupported transaction type: " + transaction.getType());
            }

            // Update status to DONE
            transaction.setStatus(TransactionStatus.DONE);
            TransactionEntity savedTransaction = transactionRepository.save(transaction);

            return transactionMapper.toDto(savedTransaction);
        } catch (Exception e) {
            // Update status to FAILED
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            throw e;
        }
    }

    private void processDeposit(TransactionEntity transaction) {
        TransactionData data = transaction.getTransactionData();
        if (!(data instanceof DepositTransactionData depositData)) {
            throw new IllegalStateException("Transaction data type does not match transaction type");
        }

        // No need to update balance as it's calculated dynamically
    }

    private void processWithdraw(TransactionEntity transaction) {
        TransactionData data = transaction.getTransactionData();
        if (!(data instanceof WithdrawTransactionData withdrawData)) {
            throw new IllegalStateException("Transaction data type does not match transaction type");
        }

        Long accountId = transaction.getAccount().getId();
        BigDecimal currentBalance = calculateAccountBalance(accountId);
        BigDecimal newBalance = currentBalance.subtract(withdrawData.getAmount());

        // Ensure balance doesn't become negative
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds for withdrawal");
        }

        // No need to update balance as it's calculated dynamically
    }

    private void processTransfer(TransactionEntity transaction) {
        TransactionData data = transaction.getTransactionData();
        if (!(data instanceof TransferTransactionData transferData)) {
            throw new IllegalStateException("Transaction data type does not match transaction type");
        }

        Long sourceAccountId = transaction.getAccount().getId();
        Long destinationAccountId = transferData.getDestinationAccountId();

        // Verify destination account exists
        AccountEntity destinationAccount = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new NoSuchElementException("Destination account not found with id: " + destinationAccountId));

        BigDecimal currentBalance = calculateAccountBalance(sourceAccountId);
        BigDecimal transferAmount = transferData.getAmount();
        BigDecimal newBalance = currentBalance.subtract(transferAmount);

        // Ensure source account has enough balance
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds for transfer");
        }

        // No need to update balances as they're calculated dynamically
    }

    private void validateTransactionDto(TransactionDto transactionDto) {
        if (transactionDto.getType() == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        if (transactionDto.getAccountId() == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }

        if (transactionDto.getData() == null) {
            throw new IllegalArgumentException("Transaction data cannot be null");
        }

        // Validate that transaction data type matches transaction type
        TransactionData data = transactionDto.getData();
        TransactionType type = transactionDto.getType();

        if (type != data.type()) {
            throw new IllegalArgumentException(
                    "Transaction data type (" + data.type() +
                    ") does not match transaction type (" + type + ")");
        }

        // Additional validation based on transaction type
        switch (type) {
            case DEPOSIT:
                validateDepositData((DepositTransactionData) data);
                break;
            case WITHDRAW:
                validateWithdrawData((WithdrawTransactionData) data);
                break;
            case TRANSFER:
                validateTransferData((TransferTransactionData) data);
                break;
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
    }

    private void validateDepositData(DepositTransactionData data) {
        if (data.getAmount() == null || data.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
    }

    private void validateWithdrawData(WithdrawTransactionData data) {
        if (data.getAmount() == null || data.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
    }

    private void validateTransferData(TransferTransactionData data) {
        if (data.getAmount() == null || data.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (data.getDestinationAccountId() == null) {
            throw new IllegalArgumentException("Destination account ID cannot be null");
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAccountBalance(Long accountId) {
        // Verify account exists
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + accountId));

        // Get sum of deposit transactions for the account
        BigDecimal depositSum = transactionRepository.getSumOfDepositTransactionsForAccount(accountId);

        // Get sum of withdraw transactions for the account
        BigDecimal withdrawSum = transactionRepository.getSumOfWithdrawTransactionsForAccount(accountId);

        // Get sum of outgoing transfer transactions for the account
        BigDecimal outgoingTransferSum = transactionRepository.getSumOfOutgoingTransferTransactionsForAccount(accountId);

        // Get sum of incoming transfer transactions for the account
        BigDecimal incomingTransferSum = transactionRepository.getSumOfIncomingTransferTransactionsForAccount(accountId);

        // Calculate final balance: deposits + incoming transfers - withdrawals - outgoing transfers
        return depositSum.add(incomingTransferSum).subtract(withdrawSum).subtract(outgoingTransferSum);
    }
}
