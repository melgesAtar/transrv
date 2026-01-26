package br.com.modware.transrv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class TransrvApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransrvApplication.class, args);
	}

}
