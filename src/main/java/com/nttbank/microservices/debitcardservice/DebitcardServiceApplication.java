package com.nttbank.microservices.debitcardservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@EnableReactiveFeignClients
@SpringBootApplication
public class DebitcardServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DebitcardServiceApplication.class, args);
  }

}
