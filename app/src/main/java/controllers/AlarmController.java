package controllers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.utmerdesign.nefesimkalbimde.MainActivity;
import com.utmerdesign.nefesimkalbimde.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmController extends BroadcastReceiver {
    public static final int MORNING_REMINDER_TIME = 10;
    public static final int AFTERNOON_REMINDER_TIME = 15;
    public static final int EVENING_REMINDER_TIME = 20;
    boolean isViewAppeared;
    SharedPreferences preferences;
    Context appContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        appContext = context;
        preferences = context.getSharedPreferences(appContext.getString(R.string.SharedPreferencesNameStr), Context.MODE_PRIVATE);

        boolean isMeditationCompleted = readIsMeditationCompletedFromDB();
        boolean isDaysMatched = isCurrentDayMatchesWithLastModifiedDay();

        if (isDaysMatched){
            if (!isMeditationCompleted) {
                notifyUserIfViewNotAppeared();
            }
        } else {
            notifyUserIfViewNotAppeared();
        }
    }

    private void notifyUserIfViewNotAppeared() {
//        boolean isTimesAreMatched = checkAnyAlarmTimeMatched();
        if(!MediaPlayerController.isViewAppeared) {
            showNotification(appContext, "Nefesim Kalbimde", "Nefes meditasyonu zamanı!");
        }
    }

    private boolean readIsMeditationCompletedFromDB() {
        return preferences.getBoolean(appContext.getString(R.string.isMeditationCompletedStr), false);
    }

    private boolean isCurrentDayMatchesWithLastModifiedDay() {
        String lastModifiedDate = readLastModifiedDateFromDB();
        String currentDate = getCurrentDateAsString();
        if (lastModifiedDate.equals(currentDate)){
            return true;
        } else {
            return false;
        }
    }

    private String readLastModifiedDateFromDB() {
        String currentDate = getCurrentDateAsString();
        return preferences.getString(appContext.getString(R.string.lastModifiedDateOnDBStr), currentDate);
    }
    private String getCurrentDateAsString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(appContext.getString(R.string.DateFormatstr));
        return dateFormat.format(calendar.getTime());
    }

    private boolean checkAnyAlarmTimeMatched() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(appContext.getString(R.string.TimeFormatstr));
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

        int notificationId = generateUniqueNotificationID(); // You can use a unique ID for each notification
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("ActivityStartReason", "Alarm");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create a notification
        NotificationCompat.Builder bu ilder = new NotificationCompat.Builder(context, "breath_on_my_heart_alarm_channel")
                .setSmallIcon(R.mipmap.ic_launcher_round)
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

    private int generateUniqueNotificationID() {
        Calendar calendar = Calendar.getInstance();

        // Get year, month, and day as integers
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Note: Months are zero-based, so add 1
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Get hour and minute as integers
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 24-hour format
        int minute = calendar.get(Calendar.MINUTE);

        int uniq_id = year * 10000 + month * 1000 + day * 100 + hour * 10 + minute;
        return uniq_id;
    }

}