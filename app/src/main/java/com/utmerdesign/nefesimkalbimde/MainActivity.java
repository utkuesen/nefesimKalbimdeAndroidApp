package com.utmerdesign.nefesimkalbimde;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import controllers.AlarmController;
import controllers.MediaPlayerController;


public class MainActivity extends AppCompatActivity {
    public static boolean isActivityAlive;
    MediaPlayerController mediaPlayerController;
    public SeekBar mediaPlayerSeekBar;
    public TextView currentPositionTextView;
    public TextView remainingPositionTextView;
    public ImageView mediaBackImageView;
    public ImageView mediaForwardImageView;
    public ImageView mediaStopImageView;
    public ImageView mediaPlayPauseImageView;

    public Button remindButton;
    public TextView meditationCompletedTextView;

    private boolean isMediaSeekBarBusy;
    private boolean isMediaCurrentPositionTextViewBusy;
    private boolean isMediaRemainingPositionTextViewBusy;
    private boolean isMediaPlayPauseImageViewBusy;
    private boolean isMeditationCompletedTextViewBusy;
    Intent serviceCreationIntent;
    IntentFilter intentFilter;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlayerController = ((MediaPlayerController.MyBinder)service).getService();
            startService(serviceCreationIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stopService(serviceCreationIntent);
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isActivityAlive = true;
        receiveBroadcastService();
        initializeComponents();
        setOnClickMethods();
        scheduleAlarms();
        setVisibilityOfDisplayObjects();

    }


    @Override
    protected void onStart() {
        super.onStart();
        setVisibilityOfDisplayObjects();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityAlive = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerController.class);
        intent.putExtra("command", "viewClosed");
        startService(intent);
        checkIntentExtraStatus();
    }

    private void setVisibilityOfDisplayObjects() {
        setVisibilityOfRemindButton();
        setVisibilityOfCompletedTextView();
    }

    private void setVisibilityOfCompletedTextView() {
        try {
            boolean isMeditationCompleted = readIsMeditationCompletedFromDB();
            boolean isDaysMatched = isCurrentDayMatchesWithLastModifiedDay();

            if (isDaysMatched){
                if (isMeditationCompleted) {
                    setMeditationCompletedOnScreen();
                } else {
                    setMeditationIncompletedOnScreen();
                }
            } else {
                setMeditationIncompletedOnScreen();
                saveMeditationUncompletedOnDB();
            }

        }catch (Exception e){
            System.out.println("MainActivity setVisibilityOfCompletedTextView error: " + e.getLocalizedMessage());
        }
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
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SharedPreferencesNameStr), Context.MODE_PRIVATE);
        return sharedPreferences.getString(getResources().getString(R.string.lastModifiedDateOnDBStr), currentDate);
    }

    private String getCurrentDateAsString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(getResources().getString(R.string.DateFormatstr));
        return dateFormat.format(calendar.getTime());
    }

    private void setMeditationIncompletedOnScreen() {
        try {
            if (isMeditationCompletedTextViewBusy) {
                return;
            } else {
                isMeditationCompletedTextViewBusy = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    meditationCompletedTextView.setText(R.string.meditation_incompleted_text);
                    meditationCompletedTextView.setBackgroundResource(R.drawable.complete_button_background);
                    isMeditationCompletedTextViewBusy = false;
                }
            });
        }catch (Exception e){
            System.out.println("MainActivity setMeditationCompleted error: " + e.getLocalizedMessage());
        }
    }

    private boolean readIsMeditationCompletedFromDB() {
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SharedPreferencesNameStr), Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(getResources().getString(R.string.isMeditationCompletedStr), false);
    }

    private void checkIntentExtraStatus() {
        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String activityStartReason = "";
            if (bundle != null){
                activityStartReason = bundle.getString("ActivityStartReason");
            } else return;

            if (activityStartReason.equals("Alarm")){
                intent.removeExtra("ActivityStartReason");
            }
        }catch (Exception e){
            System.out.println("MainActivity checkIntentExtraStatus error: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerController.class);
        intent.putExtra("command", "viewOpened");
        startService(intent);
    }

    private BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String command = intent.getExtras().getString("command");
            switch (command){
                case "updateMediaPlayerDisplayOnScreen":
                    updateMediaPlayerDisplay(intent);
                    break;
                case "updatePlayPauseButtonViewOnScreen":
                    updatePlayPauseButtonViewOnScreen(intent);
                    break;
                case "resetMediaPlayerDisplay":
                    resetMediaPlayerDisplay();
                    break;
                case "updateSeekBarOnScreen":
                    updateSeekBarOnScreen(intent);
                    break;
                case "setMeditationCompletedOnScreen":
                    setMeditationCompletedOnScreen();
                    saveMeditationCompletedOnDB();
                    break;
                default:
                    break;
            }
        }
    };

    private void saveMeditationCompletedOnDB() {
        String currentDate = getCurrentDateAsString();
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SharedPreferencesNameStr), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.isMeditationCompletedOnDBStr), true);
        editor.putString(getResources().getString(R.string.lastModifiedDateOnDBStr), currentDate);
        editor.commit();
    }
    private void saveMeditationUncompletedOnDB() {
        String currentDate = getCurrentDateAsString();
        SharedPreferences sharedPreferences = getSharedPreferences(getResources().getString(R.string.SharedPreferencesNameStr), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getResources().getString(R.string.isMeditationCompletedOnDBStr), false);
        editor.putString(getResources().getString(R.string.lastModifiedDateOnDBStr), currentDate);
        editor.commit();
    }

    private void setMeditationCompletedOnScreen() {
        try {
            if (isMeditationCompletedTextViewBusy) {
                return;
            } else {
                isMeditationCompletedTextViewBusy = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    meditationCompletedTextView.setText(R.string.meditation_completed_text);
                    meditationCompletedTextView.setBackgroundResource(R.drawable.completed_button_background);
                    isMeditationCompletedTextViewBusy = false;
                    remindButton.setVisibility(View.GONE);
                }
            });
        }catch (Exception e){
            System.out.println("MainActivity setMeditationCompleted error: " + e.getLocalizedMessage());
        }
    }

    private void receiveBroadcastService() {
        serviceCreationIntent = new Intent(this, MediaPlayerController.class);
        bindService(serviceCreationIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        intentFilter = new IntentFilter();
        intentFilter.addAction("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        registerReceiver(intentReceiver, intentFilter);
    }
    private void setVisibilityOfRemindButton() {
        try {
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            String activityStartReason = "";
            if (bundle != null){
                activityStartReason = bundle.getString("ActivityStartReason");
            }

            if (activityStartReason.equals("Alarm")){
                remindButton.setVisibility(View.VISIBLE);
            } else {
                remindButton.setVisibility(View.GONE);
            }
        }catch (Exception e){
            System.out.println("MainActivity setVisibilityOfRemindButton error: " + e.getLocalizedMessage());
        }
    }

    private void updateSeekBarOnScreen(Intent intent) {
        try {
            int nextPosition = intent.getExtras().getInt("nextPosition");
            int duration = intent.getExtras().getInt("duration");
            setSeekBarProgress(nextPosition, duration);
        }catch (Exception e){
            System.out.println("MainActivity updateSeekBarOnScreen error: " + e.getLocalizedMessage());
        }
    }

    private void updatePlayPauseButtonViewOnScreen(Intent intent) {
        try {
            boolean isButtonPlay = intent.getExtras().getBoolean("isButtonPlay");
            if (isButtonPlay) {
                setPlayPauseImageViewImageResource(R.drawable.play_button);
            } else {
                setPlayPauseImageViewImageResource(R.drawable.pause_button);
            }
        }catch (Exception e){
            System.out.println("MainActivity updatePlayPauseButtonViewOnScreen error: " + e.getLocalizedMessage());
        }
    }

    private void updateMediaPlayerDisplay(Intent intent) {
        try {
            int mediaDuration = intent.getExtras().getInt("mediaDuration");
            int mediaCurrentPosition = intent.getExtras().getInt("mediaCurrentPosition");
            String currentPositionStr = intent.getExtras().getString("currentPositionStr");
            String remainingPositionStr = intent.getExtras().getString("remainingPositionStr");
            boolean isPlayButtonUpdated = intent.getExtras().getBoolean("isPlayButtonUpdated");

            setSeekBarProgress(mediaCurrentPosition, mediaDuration);
            setCurrentPosition(currentPositionStr);
            setRemainingPosition(remainingPositionStr);
            if (isPlayButtonUpdated) {
                setPlayPauseImageViewImageResource(R.drawable.pause_button);
            } else {
                setPlayPauseImageViewImageResource(R.drawable.play_button);
            }
        }catch (Exception e){
            System.out.println("MainActivity updateMediaPlayerDisplay error: " + e.getLocalizedMessage());
        }
    }


    private void initializeComponents(){
        mediaPlayerSeekBar = findViewById(R.id.media_seekbar);

        currentPositionTextView = findViewById(R.id.current_position_text_view);
        remainingPositionTextView = findViewById(R.id.remaining_position_text_view);

        mediaBackImageView = findViewById(R.id.media_back_image_view);
        mediaForwardImageView = findViewById(R.id.media_forward_image_view);
        mediaStopImageView = findViewById(R.id.media_stop_image_view);
        mediaPlayPauseImageView = findViewById(R.id.media_play_pause_image_view);

        remindButton = findViewById(R.id.remind_button);
        meditationCompletedTextView = findViewById(R.id.meditation_completed_textview);
    }

    private void setOnClickMethods() {
        mediaPlayPauseImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MediaPlayerController.class);
                intent.putExtra("command", "mediaPlayPauseOnClick");
                startService(intent);
            }
        });

        mediaStopImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MediaPlayerController.class);
                intent.putExtra("command", "mediaStopOnClick");
                startService(intent);
            }
        });

        mediaForwardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MediaPlayerController.class);
                intent.putExtra("command", "mediaForwardOnClick");
                startService(intent);
            }
        });
        mediaBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MediaPlayerController.class);
                intent.putExtra("command", "mediaBackOnClick");
                startService(intent);
            }
        });

        mediaPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent intent = new Intent(seekBar.getContext(), MediaPlayerController.class);
                    intent.putExtra("command", "mediaClickOnSeekBar");
                    intent.putExtra("progress", progress);
                    startService(intent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        remindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public boolean setSeekBarProgress(int mediaCurrentPosition, int mediaDuration){
        try {
            if (isMediaSeekBarBusy) {
                return false;
            } else {
                isMediaSeekBarBusy = true;
            }
            mediaPlayerSeekBar.setMax(mediaDuration);
            mediaPlayerSeekBar.setProgress(mediaCurrentPosition);
            isMediaSeekBarBusy = false;
            return true;
        }catch (Exception e){
            System.out.println("MainActivity setSeekBarProgress error: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean setCurrentPosition(String curPosStr){
        try {
            if (isMediaCurrentPositionTextViewBusy) {
                return false;
            } else {
                isMediaCurrentPositionTextViewBusy = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentPositionTextView.setText(curPosStr);
                    isMediaCurrentPositionTextViewBusy = false;
                }
            });


            return true;
        }catch (Exception e){
            System.out.println("MainActivity setCurrentPosition error: " + e.getLocalizedMessage());
            return false;
        }
    }


    public boolean setRemainingPosition(String remPosStr){
        try {
            if (isMediaRemainingPositionTextViewBusy) {
                return false;
            } else {
                isMediaRemainingPositionTextViewBusy = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remainingPositionTextView.setText(remPosStr);
                    isMediaRemainingPositionTextViewBusy = false;
                }
            });

            return true;
        }catch (Exception e){
            System.out.println("MainActivity setRemainingPosition error: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean setPlayPauseImageViewImageResource(int resoruce_id){
        try {
            if (isMediaPlayPauseImageViewBusy) {
                return false;
            } else {
                isMediaPlayPauseImageViewBusy = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mediaPlayPauseImageView.setImageResource(resoruce_id);
                    isMediaPlayPauseImageViewBusy = false;
                }
            });

            return true;
        }catch (Exception e){
            System.out.println("MainActivity setRemainingPosition error: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean resetMediaPlayerDisplay(){
        if (isMediaSeekBarBusy ||
            isMediaCurrentPositionTextViewBusy ||
            isMediaRemainingPositionTextViewBusy ||
            isMediaPlayPauseImageViewBusy
        ) {
            return false;
        } else {
            isMediaSeekBarBusy = true;
            isMediaCurrentPositionTextViewBusy = true;
            isMediaRemainingPositionTextViewBusy = true;
            isMediaPlayPauseImageViewBusy = true;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mediaPlayerSeekBar.setProgress(0);
                currentPositionTextView.setText("00:00");
                remainingPositionTextView.setText("20:05");
                mediaPlayPauseImageView.setImageResource(R.drawable.play_button);

                isMediaSeekBarBusy = false;
                isMediaCurrentPositionTextViewBusy = false;
                isMediaRemainingPositionTextViewBusy = false;
                isMediaPlayPauseImageViewBusy = false;
            }
        });

        return true;
    }

    private void scheduleAlarms() {
        Context context = getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set up alarms for 10:00 AM, 3:00 PM, and 8:00 PM
        setAlarm(alarmManager, 10, 0);
        setAlarm(alarmManager, 15, 0);
        setAlarm(alarmManager, 20, 0);
    }

    private void setAlarm(AlarmManager alarmManager, int hour, int minute) {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, AlarmController.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                generateUniqueId(hour, minute),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Check if the time has already passed today, if yes, set it for the next day
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set the alarm
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    private int generateUniqueId(int hour, int minute) {
        // Generate a unique ID based on the hour and minute to avoid conflicts
        return hour * 100 + minute;
    }
}