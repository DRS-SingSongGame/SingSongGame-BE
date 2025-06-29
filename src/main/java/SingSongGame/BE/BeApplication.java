package SingSongGame.BE;

import SingSongGame.BE.config.DotenvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BeApplication {

	public static void main(String[] args) {
		DotenvLoader.loadEnv();
		SpringApplication.run(BeApplication.class, args);
	}

}
