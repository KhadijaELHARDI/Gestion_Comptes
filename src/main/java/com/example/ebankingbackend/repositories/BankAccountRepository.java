package com.example.ebankingbackend.repositories;

import com.example.ebankingbackend.entities.BankAccount;
import com.example.ebankingbackend.entities.Customer;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface BankAccountRepository extends JpaRepositoryImplementation<BankAccount,String> {


}
