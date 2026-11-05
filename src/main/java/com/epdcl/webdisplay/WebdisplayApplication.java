package com.epdcl.webdisplay;

import com.epdcl.webdisplay.dto.AccountDto;
import com.epdcl.webdisplay.entity.User;
import com.epdcl.webdisplay.service.impl.AccountService;
import com.epdcl.webdisplay.service.impl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebdisplayApplication implements CommandLineRunner {
	@Autowired
	private AccountService accountService;

	@Autowired
	private UserService userService;

	@Override
	public void run(String... args) throws Exception {
       // User dat = userService.authenticateUser("sathvik", "sathvik123");
		//AccountDto accountDto = accountService.deposit(Long.valueOf("4"), 10000);
		//System.out.println("Balance: " + accountDto.getBalance());
		//System.out.println("Name"+accountDto.getAccountHolderName());

	}

	public static void main(String[] args) {
		SpringApplication.run(WebdisplayApplication.class, args);
	}

}
