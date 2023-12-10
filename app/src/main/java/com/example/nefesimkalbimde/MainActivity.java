package com.example.nefesimkalbimde;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.nefesimkalbimde.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import Controller.MediaPlayerController;

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
    private boolean isMediaRemaininPositionTextViewBusy;
    private boolean isMediaPlayPauseImageViewBusy;



    @Override
    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_main);

        mediaPlayerController = new MediaPlayerController(this);
        initializeComponents();
        setOnClickMethods();

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
            public void onClick(View view) {
                mediaPlayerController.mediaPlayPauseOnClick();

            }
        });

        mediaStopImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerController.mediaStopOnClick();
            }
        });

        mediaForwardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerController.mediaForwardOnClick();
            }
        });
        mediaBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerController.mediaBackOnClick();
            }
        });

        mediaPlayerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayerController.mediaClickOnSeekBar(progress);
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
            if (isMediaRemaininPositionTextViewBusy) {
                return false;
            } else {
                isMediaRemaininPositionTextViewBusy = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remainingPositionTextView.setText(remPosStr);
                    isMediaRemaininPositionTextViewBusy = false;
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
            isMediaRemaininPositionTextViewBusy ||
            isMediaPlayPauseImageViewBusy
        ) {
            return false;
        } else {
            isMediaSeekBarBusy = true;
            isMediaCurrentPositionTextViewBusy = true;
            isMediaRemaininPositionTextViewBusy = true;
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
                isMediaRemaininPositionTextViewBusy = false;
                isMediaPlayPauseImageViewBusy = false;
            }
        });

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }


}