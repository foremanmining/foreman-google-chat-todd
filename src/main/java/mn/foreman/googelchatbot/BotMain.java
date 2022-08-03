package mn.foreman.googelchatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Class where all bot functions are executed */
@SpringBootApplication
@EnableScheduling
public class BotMain {

    public static void main(final String[] args) {
        SpringApplication.run(BotMain.class, args);
    }
}
