package net.tislib.walletapp.repository;

import net.tislib.walletapp.entity.TransactionEntity;
import net.tislib.walletapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
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
}
