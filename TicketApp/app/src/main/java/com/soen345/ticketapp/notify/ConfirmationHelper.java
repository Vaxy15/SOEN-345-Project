package com.soen345.ticketapp.notify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.soen345.ticketapp.R;
import com.soen345.ticketapp.model.Event;
import com.soen345.ticketapp.ui.EventListActivity;

import java.text.DateFormat;
import java.util.Date;

public final class ConfirmationHelper {

    public static final String CHANNEL_EMAIL = "EMAIL";
    public static final String CHANNEL_SMS = "SMS";
    public static final String CHANNEL_DEVICE = "DEVICE";
    public static final String CHANNEL_NONE = "NONE";

    private static final String NOTIFICATION_CHANNEL_ID = "reservations";

    private ConfirmationHelper() {}

    public static void deliver(Context context, String preference, Event event, String userEmail, String userPhone) {
        String body = buildBody(event);
        if (!CHANNEL_NONE.equals(preference)) {
            postDeviceNotification(context, event.getTitle(), body);
        }
        if (CHANNEL_EMAIL.equals(preference)) {
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(context, R.string.confirmation_email_missing, Toast.LENGTH_LONG).show();
                return;
            }
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("message/rfc822");
            send.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
            send.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.confirmation_email_subject, event.getTitle()));
            send.putExtra(Intent.EXTRA_TEXT, body);
            Intent chooser = Intent.createChooser(send, context.getString(R.string.confirmation_send_email));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } else if (CHANNEL_SMS.equals(preference)) {
            String digits = normalizePhone(userPhone);
            if (digits.isEmpty()) {
                Toast.makeText(context, R.string.confirmation_sms_missing, Toast.LENGTH_LONG).show();
                return;
            }
            Uri dest = Uri.fromParts("smsto", digits, null);
            Intent sms = new Intent(Intent.ACTION_SENDTO, dest);
            sms.putExtra("sms_body", body);
            sms.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(sms);
        }
    }

    private static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^+\\d]", "");
    }

    private static String buildBody(Event event) {
        return "Reservation confirmed\n"
                + event.getTitle() + "\n"
                + event.getLocation() + "\n"
                + event.getTime() + "\n"
                + "Category: " + event.getCategory();
    }

    private static void postDeviceNotification(Context context, String title, String text) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            );
            nm.createNotificationChannel(ch);
        }

        Intent open = new Intent(context, EventListActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
        PendingIntent pi = PendingIntent.getActivity(context, 0, open, flags);

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_ticket)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true);

        nm.notify((int) System.currentTimeMillis(), b.build());
    }
}
