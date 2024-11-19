package net.javaguides.demoProject.service;

import net.javaguides.demoProject.entities.Account;
import net.javaguides.demoProject.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImple implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean login(Account account) {

        return accountRepository.login(account.getEmail(), account.getPass()) != null;
    }
}
