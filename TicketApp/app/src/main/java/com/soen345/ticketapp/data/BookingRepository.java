package com.soen345.ticketapp.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.soen345.ticketapp.model.Event;

import java.util.HashMap;
import java.util.Map;

public class BookingRepository {

    public interface ResultCallback<T> {
        void onSuccess(T value);

        void onError(String message);
    }

    private final FirebaseFirestore db;

    public BookingRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public BookingRepository(FirebaseFirestore db) {
        this.db = db;
    }

    public Task<String> reserveSeat(String eventId, String userId) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        return db.runTransaction((Transaction.Function<String>) transaction -> {
            DocumentSnapshot snap = transaction.get(eventRef);
            if (!snap.exists()) {
                throw new FirebaseFirestoreException("Event not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            Event e = snap.toObject(Event.class);
            if (e == null) {
                throw new FirebaseFirestoreException("Invalid event", FirebaseFirestoreException.Code.ABORTED);
            }
            if (e.getCancelled()) {
                throw new FirebaseFirestoreException("This event was cancelled", FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }
            if (e.getAvailableSeats() <= 0) {
                throw new FirebaseFirestoreException("No seats available", FirebaseFirestoreException.Code.FAILED_PRECONDITION);
            }
            DocumentReference newRef = db.collection("reservations").document();
            Map<String, Object> res = new HashMap<>();
            res.put("eventId", eventId);
            res.put("userId", userId);
            res.put("eventTitle", e.getTitle());
            res.put("createdAt", System.currentTimeMillis());
            transaction.set(newRef, res);
            transaction.update(eventRef, "availableSeats", e.getAvailableSeats() - 1);
            return newRef.getId();
        });
    }

    public Task<Void> cancelReservation(@NonNull String reservationId, @NonNull String userId) {
        DocumentReference resRef = db.collection("reservations").document(reservationId);
        return db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot resSnap = transaction.get(resRef);
            if (!resSnap.exists()) {
                throw new FirebaseFirestoreException("Reservation not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            if (!userId.equals(resSnap.getString("userId"))) {
                throw new FirebaseFirestoreException("Not your reservation", FirebaseFirestoreException.Code.PERMISSION_DENIED);
            }
            String eventId = resSnap.getString("eventId");
            if (eventId == null || eventId.isEmpty()) {
                throw new FirebaseFirestoreException("Invalid reservation", FirebaseFirestoreException.Code.ABORTED);
            }
            DocumentReference eventRef = db.collection("events").document(eventId);
            DocumentSnapshot eventSnap = transaction.get(eventRef);
            Event ev = eventSnap.toObject(Event.class);
            int seats = ev != null ? ev.getAvailableSeats() : 0;
            transaction.update(eventRef, "availableSeats", seats + 1);
            transaction.delete(resRef);
            return null;
        });
    }
}
