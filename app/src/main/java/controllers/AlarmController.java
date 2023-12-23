package controllers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.nefesimkalbimde.MainActivity;
import com.example.nefesimkalbimde.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmController extends BroadcastReceiver {
    public static final int MORNING_REMINDER_TIME = 10;
    public static final int AFTERNOON_REMINDER_TIME = 15;
    public static final int EVENING_REMINDER_TIME = 20;
    boolean isViewAppeared;
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isTimesAreMatched = checkAnyAlarmTimeMatched();

        if(isTimesAreMatched && !MediaPlayerController.isViewAppeared) {
            showNotification(context, "Nefesim Kalbimde", "Nefes meditasyonu zamanı!");
        }
    }

    private boolean checkAnyAlarmTimeMatched() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String currentTime = sdf.format(calendar.getTime());

        if (currentTime.equals(String.valueOf(MORNING_REMINDER_TIME) + ":00")){
            return true;
        }
        if (currentTime.equals(String.valueOf(AFTERNOON_REMINDER_TIME) + ":00")){
            return true;
        }
        if (currentTime.equals(String.valueOf(EVENING_REMINDER_TIME) + ":00")){
            return true;
        }
        return false;
    }

    private void showNotification(Context context, String title, String content) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (required for Android Oreo and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "breath_on_my_heart_alarm_channel";
            String channelName = "breath_on_my_heart";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        // Set the sound for the notification
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int notificationId = 1; // You can use a unique ID for each notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("ActivityStartReason", "Alarm");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "breath_on_my_heart_alarm_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Show the notification

        notificationManager.notify(notificationId, builder.build());
    }



}