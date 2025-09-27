
package com.ccms.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CreditCardManagementPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditCardManagementPortalApplication.class, args);
    }

}