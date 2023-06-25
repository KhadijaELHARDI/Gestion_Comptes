package com.example.ebankingbackend.repositories;

import com.example.ebankingbackend.entities.AccountOperation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import java.util.List;

public interface AccountOperationRepository extends JpaRepositoryImplementation<AccountOperation,Long> {
     List<AccountOperation> findByBankAccountId(String accountId);
     Page<AccountOperation> findByBankAccountIdOrderByOperationDateDesc(String accountId, Pageable pageable);





}
