package net.tislib.walletapp.repository;

import net.tislib.walletapp.entity.TransactionEntity;
import net.tislib.walletapp.model.TransactionStatus;
import net.tislib.walletapp.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    List<TransactionEntity> findByAccountId(Long accountId);

    List<TransactionEntity> findByAccountIdAndStatus(Long accountId, TransactionStatus status);

    Optional<TransactionEntity> findByIdAndAccountId(Long id, Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransactionEntity t WHERE t.id = :id")
    Optional<TransactionEntity> findByIdWithLock(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransactionEntity t WHERE t.id = :id AND t.account.id = :accountId")
    Optional<TransactionEntity> findByIdAndAccountIdWithLock(@Param("id") Long id, @Param("accountId") Long accountId);

    @Query(value = "SELECT COALESCE(SUM(CAST(transaction_data->>'amount' AS numeric)), 0) " +
            "FROM transaction " +
            "WHERE account_id = :accountId " +
            "AND status = 'DONE' " +
            "AND type = 'DEPOSIT'", nativeQuery = true)
    BigDecimal getSumOfDepositTransactionsForAccount(@Param("accountId") Long accountId);

    @Query(value = "SELECT COALESCE(SUM(CAST(transaction_data->>'amount' AS numeric)), 0) " +
            "FROM transaction " +
            "WHERE account_id = :accountId " +
            "AND status = 'DONE' " +
            "AND type = 'WITHDRAW'", nativeQuery = true)
    BigDecimal getSumOfWithdrawTransactionsForAccount(@Param("accountId") Long accountId);

    @Query(value = "SELECT COALESCE(SUM(CAST(transaction_data->>'amount' AS numeric)), 0) " +
            "FROM transaction " +
            "WHERE account_id = :accountId " +
            "AND status = 'DONE' " +
            "AND type = 'TRANSFER'", nativeQuery = true)
    BigDecimal getSumOfOutgoingTransferTransactionsForAccount(@Param("accountId") Long accountId);

    @Query(value = "SELECT COALESCE(SUM(CAST(transaction_data->>'amount' AS numeric)), 0) " +
            "FROM transaction " +
            "WHERE status = 'DONE' " +
            "AND type = 'TRANSFER' " +
            "AND CAST(transaction_data->>'destinationAccountId' AS bigint) = :accountId", nativeQuery = true)
    BigDecimal getSumOfIncomingTransferTransactionsForAccount(@Param("accountId") Long accountId);
}
