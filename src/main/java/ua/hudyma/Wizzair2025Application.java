package ua.hudyma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Wizzair2025Application {

	public static void main(String[] args) {
		SpringApplication.run(Wizzair2025Application.class, args);
	}

}
