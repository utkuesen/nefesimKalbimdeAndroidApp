package com.example.nefesimkalbimde;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.IBinder;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;

import controllers.MediaPlayerController;


public class MainActivity extends AppCompatActivity {
    MediaPlayerController mediaPlayerController;
    public SeekBar mediaPlayerSeekBar;
    public TextView currentPositionTextView;
    public TextView remainingPositionTextView;
    public ImageView mediaBackImageView;
    public ImageView mediaForwardImageView;
    public ImageView mediaStopImageView;
    public ImageView mediaPlayPauseImageView;

    public Button remindButton;
    public Button meditationCompletedButton;
    public int length;

    Timer mediaPlayerUpdateDisplaysTimer;
    private boolean isMediaSeekBarBusy;
    private boolean isMediaCurrentPositionTextViewBusy;
    private boolean isMediaRemainingPositionTextViewBusy;
    private boolean isMediaPlayPauseImageViewBusy;
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
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_main);

        //mediaPlayerController = new MediaPlayerController(this);
        serviceCreationIntent = new Intent(this, MediaPlayerController.class);
        bindService(serviceCreationIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        intentFilter = new IntentFilter();
        intentFilter.addAction("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        registerReceiver(intentReceiver, intentFilter);
        initializeComponents();
        setOnClickMethods();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(getApplicationContext(), MediaPlayerController.class);
        intent.putExtra("command", "viewClosed");
        startService(intent);
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
                case "mediaStopOnClick":
                    break;
                case "mediaForwardOnClick":
                    break;
                case "mediaBackOnClick":
                    break;
                case "mediaClickOnSeekBar":
                    break;
                case "viewClosed":
                    break;
                case "viewOpened":
                    break;
                default:
                    break;
            }
        }
    };

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("MainActivity setSeekBarProgress error: ");

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
        meditationCompletedButton = findViewById(R.id.meditation_completed_button);
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
//                    mediaPlayerController.mediaClickOnSeekBar(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
}