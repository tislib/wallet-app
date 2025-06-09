package net.tislib.walletapp;

import net.tislib.walletapp.dto.AccountDto;
import net.tislib.walletapp.dto.DepositTransactionData;
import net.tislib.walletapp.dto.TransactionDto;
import net.tislib.walletapp.dto.WithdrawTransactionData;
import net.tislib.walletapp.model.TransactionStatus;
import net.tislib.walletapp.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    // Helper method to create a test account
    private AccountDto createTestAccount(String name, String currency) {
        AccountDto newAccount = new AccountDto();
        newAccount.setName(name);
        newAccount.setCurrency(currency);

        ResponseEntity<AccountDto> response = restTemplate.postForEntity(
                "/accounts", newAccount, AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    // Helper method to create a deposit transaction
    private TransactionDto createDepositTransaction(Long accountId, BigDecimal amount, String description) {
        DepositTransactionData depositData = new DepositTransactionData();
        depositData.setAmount(amount);
        depositData.setDescription(description);

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setType(TransactionType.DEPOSIT);
        transactionDto.setAccountId(accountId);
        transactionDto.setData(depositData);

        ResponseEntity<TransactionDto> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions", transactionDto, TransactionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    // Helper method to create a withdraw transaction
    private TransactionDto createWithdrawTransaction(Long accountId, BigDecimal amount, String description) {
        WithdrawTransactionData withdrawData = new WithdrawTransactionData();
        withdrawData.setAmount(amount);
        withdrawData.setDescription(description);

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setType(TransactionType.WITHDRAW);
        transactionDto.setAccountId(accountId);
        transactionDto.setData(withdrawData);

        ResponseEntity<TransactionDto> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions", transactionDto, TransactionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    // Helper method to execute a transaction
    private TransactionDto executeTransaction(Long accountId, Long transactionId) {
        ResponseEntity<TransactionDto> response = restTemplate.postForEntity(
                "/accounts/" + accountId + "/transactions/" + transactionId + "/execute", null, TransactionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    // Helper method to get account balance
    private BigDecimal getAccountBalance(Long accountId) {
        ResponseEntity<BigDecimal> response = restTemplate.getForEntity(
                "/accounts/" + accountId + "/balance", BigDecimal.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    @Test
    public void testCreateTransaction() {
        // Create a test account
        AccountDto account = createTestAccount("Transaction Test Account", "USD");

        // Create a deposit transaction
        TransactionDto depositTransaction = createDepositTransaction(account.getId(), new BigDecimal("100.00"), "Test deposit");

        assertThat(depositTransaction.getId()).isNotNull();
        assertThat(depositTransaction.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(depositTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(depositTransaction.getAccountId()).isEqualTo(account.getId());
        assertThat(depositTransaction.getData()).isInstanceOf(DepositTransactionData.class);
        assertThat(((DepositTransactionData) depositTransaction.getData()).getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(((DepositTransactionData) depositTransaction.getData()).getDescription()).isEqualTo("Test deposit");
    }

    @Test
    public void testGetTransactionById() {
        // Create a test account
        AccountDto account = createTestAccount("Get Transaction Test Account", "EUR");

        // Create a deposit transaction
        TransactionDto createdTransaction = createDepositTransaction(account.getId(), new BigDecimal("200.00"), "Get test");

        // Get the transaction by ID
        ResponseEntity<TransactionDto> response = restTemplate.getForEntity(
                "/accounts/" + account.getId() + "/transactions/" + createdTransaction.getId(), TransactionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(createdTransaction.getId());
        assertThat(response.getBody().getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getBody().getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(response.getBody().getAccountId()).isEqualTo(account.getId());
    }

    @Test
    public void testGetTransactionById_NotFound() {
        // Create a test account
        AccountDto account = createTestAccount("Not Found Transaction Test Account", "USD");

        // Try to get a non-existent transaction
        ResponseEntity<TransactionDto> response = restTemplate.getForEntity(
                "/accounts/" + account.getId() + "/transactions/999", TransactionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetAllTransactions() {
        // Create a test account
        AccountDto account = createTestAccount("All Transactions Test Account", "USD");

        // Create a few transactions
        createDepositTransaction(account.getId(), new BigDecimal("100.00"), "First deposit");
        createDepositTransaction(account.getId(), new BigDecimal("200.00"), "Second deposit");

        // Get all transactions
        ResponseEntity<List<TransactionDto>> response = restTemplate.exchange(
                "/accounts/" + account.getId() + "/transactions",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TransactionDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void testUpdateTransaction() {
        // Create a test account
        AccountDto account = createTestAccount("Update Transaction Test Account", "USD");

        // Create a deposit transaction
        TransactionDto createdTransaction = createDepositTransaction(account.getId(), new BigDecimal("300.00"), "Original description");

        // Update the transaction description
        DepositTransactionData updatedData = new DepositTransactionData();
        updatedData.setAmount(new BigDecimal("300.00"));
        updatedData.setDescription("Updated description");

        createdTransaction.setData(updatedData);

        HttpEntity<TransactionDto> requestEntity = new HttpEntity<>(createdTransaction);

        ResponseEntity<TransactionDto> response = restTemplate.exchange(
                "/accounts/" + account.getId() + "/transactions/" + createdTransaction.getId(),
                HttpMethod.PUT,
                requestEntity,
                TransactionDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(createdTransaction.getId());
        assertThat(((DepositTransactionData) response.getBody().getData()).getDescription()).isEqualTo("Updated description");
    }

    @Test
    public void testDeleteTransaction() {
        // Create a test account
        AccountDto account = createTestAccount("Delete Transaction Test Account", "USD");

        // Create a deposit transaction
        TransactionDto createdTransaction = createDepositTransaction(account.getId(), new BigDecimal("400.00"), "To be deleted");

        // Delete the transaction
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/accounts/" + account.getId() + "/transactions/" + createdTransaction.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify the transaction is deleted by trying to get it
        ResponseEntity<TransactionDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + account.getId() + "/transactions/" + createdTransaction.getId(), TransactionDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testExecuteTransaction() {
        // Create a test account
        AccountDto account = createTestAccount("Execute Transaction Test Account", "USD");

        // Create a deposit transaction
        TransactionDto depositTransaction = createDepositTransaction(account.getId(), new BigDecimal("500.00"), "Deposit to execute");

        // Execute the transaction
        TransactionDto executedTransaction = executeTransaction(account.getId(), depositTransaction.getId());

        assertThat(executedTransaction.getStatus()).isEqualTo(TransactionStatus.DONE);

        // Check the account balance
        BigDecimal balance = getAccountBalance(account.getId());
        assertThat(balance).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    public void testCannotDeleteExecutedTransaction() {
        // Create a test account
        AccountDto account = createTestAccount("Cannot Delete Executed Transaction Test Account", "USD");

        // Create a deposit transaction
        TransactionDto depositTransaction = createDepositTransaction(account.getId(), new BigDecimal("600.00"), "Deposit to execute and try to delete");

        // Execute the transaction
        executeTransaction(account.getId(), depositTransaction.getId());

        // Try to delete the executed transaction
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/accounts/" + account.getId() + "/transactions/" + depositTransaction.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        // Should return a bad request status
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify the transaction still exists
        ResponseEntity<TransactionDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + account.getId() + "/transactions/" + depositTransaction.getId(), TransactionDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(depositTransaction.getId());
    }

    @Test
    public void testInsufficientFundsForWithdrawal() {
        // Create a test account
        AccountDto account = createTestAccount("Insufficient Funds Test Account", "USD");

        // Create a withdraw transaction with an amount greater than the account balance
        TransactionDto withdrawTransaction = createWithdrawTransaction(account.getId(), new BigDecimal("100.00"), "Withdraw with insufficient funds");

        // Try to execute the transaction
        ResponseEntity<TransactionDto> response = restTemplate.postForEntity(
                "/accounts/" + account.getId() + "/transactions/" + withdrawTransaction.getId() + "/execute", null, TransactionDto.class);

        // Should return a bad request status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testDepositWithdrawFlow() {
        // Create a test account
        AccountDto account = createTestAccount("Deposit Withdraw Flow Test Account", "USD");

        // Create and execute a deposit transaction
        TransactionDto depositTransaction = createDepositTransaction(account.getId(), new BigDecimal("1000.00"), "Initial deposit");
        executeTransaction(account.getId(), depositTransaction.getId());

        // Check the account balance
        BigDecimal balanceAfterDeposit = getAccountBalance(account.getId());
        assertThat(balanceAfterDeposit).isEqualByComparingTo(new BigDecimal("1000.00"));

        // Create and execute a withdraw transaction
        TransactionDto withdrawTransaction = createWithdrawTransaction(account.getId(), new BigDecimal("300.00"), "Partial withdrawal");
        executeTransaction(account.getId(), withdrawTransaction.getId());

        // Check the account balance again
        BigDecimal balanceAfterWithdraw = getAccountBalance(account.getId());
        assertThat(balanceAfterWithdraw).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    public void testCreateAccountCreateTransactionExecuteCheckBalance() {
        // Create a test account
        AccountDto account = createTestAccount("Full Flow Test Account", "USD");

        // Create a deposit transaction
        TransactionDto depositTransaction = createDepositTransaction(account.getId(), new BigDecimal("500.00"), "Test deposit");

        // Execute the transaction
        executeTransaction(account.getId(), depositTransaction.getId());

        // Check the account balance
        BigDecimal balance = getAccountBalance(account.getId());
        assertThat(balance).isEqualByComparingTo(new BigDecimal("500.00"));
    }
}