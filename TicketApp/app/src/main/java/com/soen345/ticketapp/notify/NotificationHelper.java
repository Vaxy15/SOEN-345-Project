package com.soen345.ticketapp.notify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.soen345.ticketapp.R;

/**
 * NotificationHelper
 *
 * Delivers an in-app (push) confirmation notification when a ticket is reserved.
 *
 * For email/SMS confirmation in production:
 *   - Email → trigger a Firebase Cloud Function that calls SendGrid / JavaMail
 *   - SMS   → trigger a Firebase Cloud Function that calls Twilio
 *
 * Both require a server-side component (Cloud Functions) because API keys must
 * never be embedded in the Android client.
 */
public class NotificationHelper {

    private static final String CHANNEL_ID   = "ticket_confirmations";
    private static final String CHANNEL_NAME = "Ticket Confirmations";
    private static final int    NOTIF_ID     = 1001;

    /**
     * Show an in-app push notification confirming the reservation.
     * Call this immediately after a successful Firestore write.
     */
    public static void sendConfirmation(Context ctx,
                                        String eventTitle,
                                        String location,
                                        String time) {
        NotificationManager manager =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel (required on API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Booking confirmations for reserved tickets");
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Booking Confirmed: " + eventTitle)
            .setContentText(location + "  •  " + time)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Your ticket for " + eventTitle + " at " + location
                       + " on " + time + " has been confirmed."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        manager.notify(NOTIF_ID, builder.build());
    }

    /**
     * Show a push notification when an event the user booked is cancelled by admin.
     */
    public static void sendEventCancelledNotice(Context ctx, String eventTitle) {
        NotificationManager manager =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Event Cancelled")
            .setContentText("\"" + eventTitle + "\" has been cancelled.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true);

        manager.notify(NOTIF_ID + 1, builder.build());
    }
}
