package guiando.billintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

@EnableIntegration
@SpringBootApplication
@IntegrationComponentScan
public class BillIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillIntegrationApplication.class, args);
	}

}
