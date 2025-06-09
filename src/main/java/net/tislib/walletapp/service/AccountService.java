package net.tislib.walletapp.service;

import net.tislib.walletapp.dto.AccountDto;
import net.tislib.walletapp.entity.AccountEntity;
import net.tislib.walletapp.mapper.AccountMapper;
import net.tislib.walletapp.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        AccountEntity account = accountMapper.toEntity(accountDto);
        AccountEntity savedAccount = accountRepository.save(account);
        return accountMapper.toDto(savedAccount);
    }

    @Transactional
    public AccountDto updateAccount(Long id, AccountDto accountDto) {
        AccountEntity existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found with id: " + id));

        accountMapper.updateEntityFromDto(accountDto, existingAccount);
        AccountEntity updatedAccount = accountRepository.save(existingAccount);
        return accountMapper.toDto(updatedAccount);
    }

    @Transactional
    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new NoSuchElementException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
    }
}
