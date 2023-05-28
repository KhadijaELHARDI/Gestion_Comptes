package com.example.ebankingbackend.services;

import com.example.ebankingbackend.dtos.*;
import com.example.ebankingbackend.entities.*;
import com.example.ebankingbackend.enums.OperationType;
import com.example.ebankingbackend.exceptions.BalanceNotSufficientException;
import com.example.ebankingbackend.exceptions.BankAccountNotFoundException;
import com.example.ebankingbackend.exceptions.CustomerNotFoundException;
import com.example.ebankingbackend.mappers.BankAccountMapperImpl;
import com.example.ebankingbackend.repositories.AccountOperationRepository;
import com.example.ebankingbackend.repositories.BankAccountRepository;
import com.example.ebankingbackend.repositories.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.domain.PageRequest.*;


@Service
@Transactional
@AllArgsConstructor
@Slf4j

public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;



    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer=dtoMapper.fromCustomerDT0(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);

        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public CurrentBankAccountDTO saveCurrenBankAccount(double intialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
            throw new CustomerNotFoundException("Customer not found");
        CurrentAccount currentAccount=new CurrentAccount();

        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setCreatedAt(new Date());
        currentAccount.setBalance(intialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);


        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double intialBalance, double interstRate, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
            throw new CustomerNotFoundException("Customer not found");
        SavingAccount savingAccount=new SavingAccount();

        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setCreatedAt(new Date());
        savingAccount.setBalance(intialBalance);
        savingAccount.setInterestRate(interstRate);
        savingAccount.setCustomer(customer);
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);


        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }


    @Override
    public List<CustomerDTO> listCustomers() {

        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS = customers.stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());
        return customerDTOS;
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("Bank Not Found"));
        if(bankAccount instanceof SavingAccount){
            SavingAccount savingAccount= (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);

        }else {
            CurrentAccount currentAccount= (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }

    }

    @Override
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("Bank Not Found"));
      if(bankAccount.getBalance()<amount)
          throw new BalanceNotSufficientException("Balance not sufficient");
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);

    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("Bank Not Found"));

        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
      debit(accountIdSource,amount,"Transfer to"+accountIDestination);
      credit(accountIDestination,amount,"Transfer from"+accountIdSource);
    }
    @Override
    public List<BankAccountDTO> bankAccountList(){
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);

            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }
    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not Found"));
        return dtoMapper.fromCustomer(customer);
    }
    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        Customer customer=dtoMapper.fromCustomerDT0(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);

        return dtoMapper.fromCustomer(savedCustomer);
    }
@Override
    public void deleteCustomer(Long customerId){
        customerRepository.deleteById(customerId);
    }
    @Override
 public List<AccountOperationDTO> accountHistory(String accountId){

     List<AccountOperation> accountOperation = accountOperationRepository.findByBankAccountId(accountId);

    return  accountOperation.stream().map(op->dtoMapper.fromAccountOperation(op)).collect((Collectors.toList()));
 }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
       BankAccount bankAccount=bankAccountRepository.findById(accountId).orElse(null);
       if(bankAccount==null) throw new BankAccountNotFoundException("Account not Found");
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId,PageRequest.of(page,size));
       AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationsDTOS = accountOperations.getContent().stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationsDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());

        return accountHistoryDTO;
    }

}
