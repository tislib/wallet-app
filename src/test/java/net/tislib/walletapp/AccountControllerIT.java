package net.tislib.walletapp;

import net.tislib.walletapp.dto.AccountDto;
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
public class AccountControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    // Helper method to create a test account
    private AccountDto createTestAccount(String name, String currency, BigDecimal balance) {
        AccountDto newAccount = new AccountDto();
        newAccount.setName(name);
        newAccount.setCurrency(currency);
        newAccount.setBalance(balance);

        ResponseEntity<AccountDto> response = restTemplate.postForEntity(
                "/accounts", newAccount, AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }

    @Test
    public void testCreateAccount() {
        // Create a new account
        AccountDto newAccount = new AccountDto();
        newAccount.setName("Test Account");
        newAccount.setCurrency("USD");
        newAccount.setBalance(new BigDecimal("100.00"));

        ResponseEntity<AccountDto> response = restTemplate.postForEntity(
                "/accounts", newAccount, AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Test Account");
        assertThat(response.getBody().getCurrency()).isEqualTo("USD");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.getBody().getCreatedAt()).isNotNull();
    }

    @Test
    public void testGetAccountById() {
        // Create a test account first
        AccountDto createdAccount = createTestAccount("Get Test Account", "EUR", new BigDecimal("200.00"));

        // Get the account by ID
        ResponseEntity<AccountDto> response = restTemplate.getForEntity(
                "/accounts/" + createdAccount.getId(), AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(createdAccount.getId());
        assertThat(response.getBody().getName()).isEqualTo("Get Test Account");
        assertThat(response.getBody().getCurrency()).isEqualTo("EUR");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
    }

    @Test
    public void testGetAccountById_NotFound() {
        // Try to get a non-existent account
        ResponseEntity<AccountDto> response = restTemplate.getForEntity(
                "/accounts/999", AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testGetAllAccounts() {
        // Create a few test accounts
        createTestAccount("Account 1", "USD", new BigDecimal("100.00"));
        createTestAccount("Account 2", "EUR", new BigDecimal("200.00"));

        // Get all accounts
        ResponseEntity<List<AccountDto>> response = restTemplate.exchange(
                "/accounts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AccountDto>>() {});

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isGreaterThanOrEqualTo(2);

        // Verify the accounts we created are in the list
        boolean foundAccount1 = false;
        boolean foundAccount2 = false;

        for (AccountDto account : response.getBody()) {
            if ("Account 1".equals(account.getName())) {
                foundAccount1 = true;
                assertThat(account.getCurrency()).isEqualTo("USD");
                assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
            } else if ("Account 2".equals(account.getName())) {
                foundAccount2 = true;
                assertThat(account.getCurrency()).isEqualTo("EUR");
                assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("200.00"));
            }
        }

        assertThat(foundAccount1).isTrue();
        assertThat(foundAccount2).isTrue();
    }

    @Test
    public void testUpdateAccount() {
        // Create a test account first
        AccountDto createdAccount = createTestAccount("Update Test Account", "USD", new BigDecimal("300.00"));

        // Update the account
        createdAccount.setName("Updated Account");
        createdAccount.setCurrency("GBP");
        createdAccount.setBalance(new BigDecimal("350.00"));

        HttpEntity<AccountDto> requestEntity = new HttpEntity<>(createdAccount);

        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/accounts/" + createdAccount.getId(),
                HttpMethod.PUT,
                requestEntity,
                AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(createdAccount.getId());
        assertThat(response.getBody().getName()).isEqualTo("Updated Account");
        assertThat(response.getBody().getCurrency()).isEqualTo("GBP");
        assertThat(response.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("350.00"));

        // Verify the update by getting the account again
        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + createdAccount.getId(), AccountDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getName()).isEqualTo("Updated Account");
        assertThat(getResponse.getBody().getCurrency()).isEqualTo("GBP");
        assertThat(getResponse.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("350.00"));
        assertThat(getResponse.getBody().getUpdatedAt()).isNotNull();
    }

    @Test
    public void testUpdateAccount_NotFound() {
        // Try to update a non-existent account
        AccountDto nonExistentAccount = new AccountDto();
        nonExistentAccount.setName("Non-existent Account");
        nonExistentAccount.setCurrency("USD");
        nonExistentAccount.setBalance(new BigDecimal("100.00"));

        HttpEntity<AccountDto> requestEntity = new HttpEntity<>(nonExistentAccount);

        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/accounts/999",
                HttpMethod.PUT,
                requestEntity,
                AccountDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteAccount() {
        // Create a test account first
        AccountDto createdAccount = createTestAccount("Delete Test Account", "JPY", new BigDecimal("0.00"));

        // Delete the account
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/accounts/" + createdAccount.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify the account is deleted by trying to get it
        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + createdAccount.getId(), AccountDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeleteAccountWithBalance() {
        // Create a test account with a balance
        AccountDto createdAccount = createTestAccount("Delete Test Account With Balance", "JPY", new BigDecimal("500.00"));

        // Try to delete the account with balance
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/accounts/" + createdAccount.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        // Should return a bad request status
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Verify the account still exists
        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + createdAccount.getId(), AccountDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(createdAccount.getId());
    }

    @Test
    public void testCreateAccountWithNullName() {
        // Create an account DTO with null name
        AccountDto accountDto = new AccountDto();
        accountDto.setCurrency("USD");
        accountDto.setBalance(new BigDecimal("100.00"));

        HttpEntity<AccountDto> requestEntity = new HttpEntity<>(accountDto);

        // Try to create the account
        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/accounts",
                HttpMethod.POST,
                requestEntity,
                AccountDto.class);

        // Should return a bad request status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testCreateAccountWithNegativeBalance() {
        // Create an account DTO with negative balance
        AccountDto accountDto = new AccountDto();
        accountDto.setName("Negative Balance Account");
        accountDto.setCurrency("USD");
        accountDto.setBalance(new BigDecimal("-100.00"));

        HttpEntity<AccountDto> requestEntity = new HttpEntity<>(accountDto);

        // Try to create the account
        ResponseEntity<AccountDto> response = restTemplate.exchange(
                "/accounts",
                HttpMethod.POST,
                requestEntity,
                AccountDto.class);

        // Should return a bad request status
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testDeleteAccount_NotFound() {
        // Try to delete a non-existent account
        ResponseEntity<Void> response = restTemplate.exchange(
                "/accounts/999",
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testCreateListDeleteFlow() {
        // Create multiple accounts
        AccountDto account1 = createTestAccount("Flow Account 1", "USD", new BigDecimal("100.00"));
        AccountDto account2 = createTestAccount("Flow Account 2", "EUR", new BigDecimal("200.00"));

        // Get all accounts and verify our accounts are there
        ResponseEntity<List<AccountDto>> listResponse = restTemplate.exchange(
                "/accounts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AccountDto>>() {});

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotNull();

        boolean foundAccount1 = false;
        boolean foundAccount2 = false;

        for (AccountDto account : listResponse.getBody()) {
            if (account.getId().equals(account1.getId())) {
                foundAccount1 = true;
            } else if (account.getId().equals(account2.getId())) {
                foundAccount2 = true;
            }
        }

        assertThat(foundAccount1).isTrue();
        assertThat(foundAccount2).isTrue();

        // Set balance to zero before deleting
        account1.setBalance(BigDecimal.ZERO);
        HttpEntity<AccountDto> updateRequest = new HttpEntity<>(account1);
        restTemplate.exchange(
                "/accounts/" + account1.getId(),
                HttpMethod.PUT,
                updateRequest,
                AccountDto.class);

        // Delete one account
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/accounts/" + account1.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Get all accounts again and verify only one account remains
        ResponseEntity<List<AccountDto>> listAfterDeleteResponse = restTemplate.exchange(
                "/accounts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AccountDto>>() {});

        assertThat(listAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listAfterDeleteResponse.getBody()).isNotNull();

        boolean foundDeletedAccount = false;
        boolean foundRemainingAccount = false;

        for (AccountDto account : listAfterDeleteResponse.getBody()) {
            if (account.getId().equals(account1.getId())) {
                foundDeletedAccount = true;
            } else if (account.getId().equals(account2.getId())) {
                foundRemainingAccount = true;
            }
        }

        assertThat(foundDeletedAccount).isFalse();
        assertThat(foundRemainingAccount).isTrue();
    }

    @Test
    public void testCreateUpdateGetFlow() {
        // Create an account
        AccountDto createdAccount = createTestAccount("Update Flow Account", "USD", new BigDecimal("100.00"));

        // Update the account
        createdAccount.setName("Updated Flow Account");
        createdAccount.setBalance(new BigDecimal("150.00"));

        HttpEntity<AccountDto> requestEntity = new HttpEntity<>(createdAccount);

        ResponseEntity<AccountDto> updateResponse = restTemplate.exchange(
                "/accounts/" + createdAccount.getId(),
                HttpMethod.PUT,
                requestEntity,
                AccountDto.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getName()).isEqualTo("Updated Flow Account");
        assertThat(updateResponse.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));

        // Get the account and verify the updates
        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + createdAccount.getId(), AccountDto.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getName()).isEqualTo("Updated Flow Account");
        assertThat(getResponse.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(getResponse.getBody().getUpdatedAt()).isNotNull();
    }
}
