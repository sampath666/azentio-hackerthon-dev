package com.starter.development.application.port.in;

import com.starter.development.domain.model.Account;

import java.util.List;

public interface AccountUseCase {

    Account createAccount(Long customerId, String accountNumber, String type,
                          String currency, String openingDate, String status);

    List<Account> getAccounts();

    List<Account> getAccountsByCustomer(Long customerId);

    Account getAccount(Long id);
}
