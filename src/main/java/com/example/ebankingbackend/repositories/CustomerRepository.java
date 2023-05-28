package com.example.ebankingbackend.repositories;

import com.example.ebankingbackend.entities.Customer;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

public interface CustomerRepository extends JpaRepositoryImplementation<Customer,Long> {

}
