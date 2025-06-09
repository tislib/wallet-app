package net.tislib.walletapp.service;

import net.tislib.walletapp.dto.AccountDto;
import net.tislib.walletapp.entity.AccountEntity;
import net.tislib.walletapp.mapper.AccountMapper;
import net.tislib.walletapp.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public List<AccountDto> getAllAccounts() {
        List<AccountEntity> accounts = accountRepository.findAll();
        return accountMapper.toDtoList(accounts);
    }

    @Transactional(readOnly = true)
    public AccountDto getAccountById(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));
        return accountMapper.toDto(account);
    }

    @Transactional
    public AccountDto createAccount(AccountDto accountDto) {
        validateAccountDto(accountDto);
        AccountEntity account = accountMapper.toEntity(accountDto);
        AccountEntity savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    private void validateAccountDto(AccountDto accountDto) {
        if (accountDto.getName() == null) {
            throw new IllegalArgumentException("Account name cannot be null");
        }

        if (accountDto.getBalance() != null && accountDto.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Account balance must be positive or zero");
        }
    }

    @Transactional
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        validateAccountDto(accountDto);

        AccountEntity existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));

        accountMapper.updateEntityFromDto(accountDto, existingAccount);
        AccountEntity updatedAccount = accountRepository.save(existingAccount);
        return accountMapper.toDto(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));

        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete account with a positive balance");
        }

        accountRepository.deleteById(id);
    }
}
