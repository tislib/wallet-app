package net.tislib.walletapp;

import net.tislib.walletapp.dto.AccountDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testCreateAndGetAccount() {
        // Create a new account
        AccountDto newAccount = new AccountDto();
        newAccount.setName("Test Account");
        newAccount.setCurrency("USD");
        newAccount.setBalance(new BigDecimal("100.00"));

        ResponseEntity<AccountDto> createResponse = restTemplate.postForEntity(
                "/accounts", newAccount, AccountDto.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();
        assertThat(createResponse.getBody().getName()).isEqualTo("Test Account");
        assertThat(createResponse.getBody().getCurrency()).isEqualTo("USD");
        assertThat(createResponse.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        
        // Get the created account
        Long accountId = createResponse.getBody().getId();
        ResponseEntity<AccountDto> getResponse = restTemplate.getForEntity(
                "/accounts/" + accountId, AccountDto.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getId()).isEqualTo(accountId);
        assertThat(getResponse.getBody().getName()).isEqualTo("Test Account");
        assertThat(getResponse.getBody().getCurrency()).isEqualTo("USD");
        assertThat(getResponse.getBody().getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}