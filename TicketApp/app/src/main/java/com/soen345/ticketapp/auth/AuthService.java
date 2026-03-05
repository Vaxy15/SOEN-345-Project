package com.soen345.ticketapp.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    public FirebaseUser currentUser() {
        return auth.getCurrentUser();
    }

    public void signOut() {
        auth.signOut();
    }

    public FirebaseAuth getAuth() {
        return auth;
    }
}