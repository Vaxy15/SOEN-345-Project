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

    public Task<String> reserveSeat(@NonNull String eventId, @NonNull String userId) {
        DocumentReference eventRef = db.collection("events").document(eventId);

        // One reservation per user per event.
        // This also makes duplicate booking prevention transaction-safe.
        String reservationId = eventId + "_" + userId;
        DocumentReference reservationRef = db.collection("reservations").document(reservationId);

        return db.runTransaction((Transaction.Function<String>) transaction -> {
            DocumentSnapshot eventSnap = transaction.get(eventRef);
            if (!eventSnap.exists()) {
                throw new FirebaseFirestoreException(
                    "Event not found",
                    FirebaseFirestoreException.Code.NOT_FOUND
                );
            }

            Event event = eventSnap.toObject(Event.class);
            if (event == null) {
                throw new FirebaseFirestoreException(
                    "Invalid event",
                    FirebaseFirestoreException.Code.ABORTED
                );
            }

            DocumentSnapshot existingReservation = transaction.get(reservationRef);
            if (existingReservation.exists()) {
                throw new FirebaseFirestoreException(
                    "You already reserved this event",
                    FirebaseFirestoreException.Code.ALREADY_EXISTS
                );
            }

            if (event.isCancelled()) {
                throw new FirebaseFirestoreException(
                    "This event was cancelled",
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION
                );
            }

            if (event.getAvailableSeats() <= 0) {
                throw new FirebaseFirestoreException(
                    "No seats available",
                    FirebaseFirestoreException.Code.FAILED_PRECONDITION
                );
            }

            Map<String, Object> reservation = new HashMap<>();
            reservation.put("userId", userId);
            reservation.put("eventId", eventId);
            reservation.put("eventTitle", event.getTitle());
            reservation.put("eventLocation", event.getLocation());
            reservation.put("eventDateTimeMillis", event.getDateTimeMillis());
            reservation.put("createdAt", System.currentTimeMillis());

            transaction.set(reservationRef, reservation);
            transaction.update(eventRef, "availableSeats", event.getAvailableSeats() - 1);

            return reservationRef.getId();
        });
    }

    public Task<Void> cancelReservation(@NonNull String reservationId, @NonNull String userId) {
        DocumentReference reservationRef = db.collection("reservations").document(reservationId);

        return db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot reservationSnap = transaction.get(reservationRef);
            if (!reservationSnap.exists()) {
                throw new FirebaseFirestoreException(
                    "Reservation not found",
                    FirebaseFirestoreException.Code.NOT_FOUND
                );
            }

            String reservationUserId = reservationSnap.getString("userId");
            if (!userId.equals(reservationUserId)) {
                throw new FirebaseFirestoreException(
                    "Not your reservation",
                    FirebaseFirestoreException.Code.PERMISSION_DENIED
                );
            }

            String eventId = reservationSnap.getString("eventId");
            if (eventId == null || eventId.isEmpty()) {
                throw new FirebaseFirestoreException(
                    "Invalid reservation",
                    FirebaseFirestoreException.Code.ABORTED
                );
            }

            DocumentReference eventRef = db.collection("events").document(eventId);
            DocumentSnapshot eventSnap = transaction.get(eventRef);

            if (eventSnap.exists()) {
                Event event = eventSnap.toObject(Event.class);
                int seats = event != null ? event.getAvailableSeats() : 0;
                transaction.update(eventRef, "availableSeats", seats + 1);
            }

            transaction.delete(reservationRef);
            return null;
        });
    }
}