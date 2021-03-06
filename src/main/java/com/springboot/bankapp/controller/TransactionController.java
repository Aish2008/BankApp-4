package com.springboot.bankapp.controller;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.bankapp.dto.Transfer;
import com.springboot.bankapp.model.Transaction;
import com.springboot.bankapp.service.TransactionService;

@RestController
public class TransactionController {

	@Autowired
	private TransactionService transactionService;
	
	@PostMapping("/transfer")
	public Transaction doTransfer(Principal principal, @RequestBody Transfer transfer) {
		String username=principal.getName(); 
		System.out.println(username);
		System.out.println(transfer);
		System.out.println("In transfer api....");

		 
		//1.1
		String fromAccountNumber = transactionService.fetchFromAccountNumber(username);
		
		//2.1 
		transactionService.updateBalance(fromAccountNumber, transfer.getAmount());
		//2.2 
		transactionService.creditAmount(transfer.getToAccountNumber(), transfer.getAmount());
		
		//3.1
		Transaction transaction = new Transaction();
		transaction.setAccountFrom(fromAccountNumber);
		transaction.setAccountTo(transfer.getToAccountNumber());
		transaction.setAmount(transfer.getAmount());
		transaction.setOperationType("TRANSFER");
		transaction.setDateOfTransaction(new Date());
		
		return transactionService.saveTransaction(transaction);
		 
		
	}
	
	@GetMapping("/statement/{startDate}/{endDate}")
	public List<Transaction> generateStatement(Principal principal, 
			@PathVariable("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate, 
			@PathVariable("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
		 
		String username=principal.getName(); 

		//Step 1
		String accountNumber = transactionService.fetchFromAccountNumber(username);
		
		//Step 2
		List<Transaction> list = transactionService.fetchTransactionsByAccountNumber(accountNumber);
		
		//Step 3
		try {
			//convert LocalDate to Date
			Date startDateToDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate.toString());
			Date endDateToDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDate.toString());
			//2022-01-10
			list = list.parallelStream()
					.filter(t-> t.getDateOfTransaction().compareTo(startDateToDate) >= 0)
					.filter(t-> t.getDateOfTransaction().compareTo(endDateToDate) <= 0)
					.collect(Collectors.toList());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return list; 
	}
}