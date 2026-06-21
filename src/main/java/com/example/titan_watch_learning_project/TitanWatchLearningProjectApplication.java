//package com.example.titan_watch_learning_project;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.scheduling.annotation.EnableScheduling;   // ← ADD
//
//
//@SpringBootApplication
//@EnableScheduling
//public class TitanWatchLearningProjectApplication {
//
//
//
//    public static void main(String[] args) {
//        SpringApplication.run(TitanWatchLearningProjectApplication.class, args);
//    }
//
//}


package com.example.titan_watch_learning_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TitanWatchLearningProjectApplication {
    public static void main(String[] args) {

        // ════════════════════════════════════════════════════════
        // FIX — IPv4 force karo, IPv6 ki wajah se SMTP (Hostinger/
        // Cloudflare) SSL handshake timeout ho raha tha
        // ════════════════════════════════════════════════════════
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(TitanWatchLearningProjectApplication.class, args);
    }
}