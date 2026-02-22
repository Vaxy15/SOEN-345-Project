package com.soen345.ticketing_api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

  @PostConstruct
  public void init() {
    try {
      if (!FirebaseApp.getApps().isEmpty()) return;

      String keyPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
      GoogleCredentials credentials =
          (keyPath != null && !keyPath.isBlank())
              ? GoogleCredentials.fromStream(new FileInputStream(keyPath))
              : GoogleCredentials.getApplicationDefault();

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(credentials)
          .build();

      FirebaseApp.initializeApp(options);
    } catch (Exception e) {
      throw new RuntimeException("Failed to init Firebase Admin", e);
    }
  }
}