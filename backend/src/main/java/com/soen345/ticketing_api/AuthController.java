package com.soen345.ticketing_api;

import com.google.firebase.auth.FirebaseAuth;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {

  @GetMapping("/me")
  public String me(@RequestHeader(name="Authorization", required=false) String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing Bearer token");
    }
    String token = authHeader.substring("Bearer ".length());
    try {
      return FirebaseAuth.getInstance().verifyIdToken(token).getUid();
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
    }
  }
}