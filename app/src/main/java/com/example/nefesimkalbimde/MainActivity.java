package com.example.nefesimkalbimde;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

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
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private boolean isMediaListining = false;
    private boolean isMediastarted = false;

    private MediaPlayer mediaPlayer;
    private AudioManager mAudioManager;
    private int length;

    Timer mediaPlayerUpdateDisplaysTimer;


    @Override
    protected void onStart() {
        super.onStart();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMediaListining){
//                    Snackbar.make(view, "Stop music", Snackbar.LENGTH_LONG)
//                            .setAnchorView(R.id.imageView)
//                            .setAction("Action", null).show();
//                    releaseMediaPlayer();
                    mediaPlayer.pause();
                    mediaPlayerUpdateDisplaysTimer.cancel();
                    isMediaListining = false;
                    binding.playPauseButton.setImageResource(R.drawable.play_button);
                } else {
                    if (isMediastarted) {
                        mediaPlayer.start();
                    } else {
                        startMusic();
                        isMediastarted = true;
                    }
                    startMediaPlayerTimer();
                    isMediaListining = true;
                    binding.playPauseButton.setImageResource(R.drawable.pause_button);
                }

            }
        });

        binding.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMediastarted){
                    releaseMediaPlayer();
                    isMediaListining = false;
                    isMediastarted = false;
                    binding.playPauseButton.setImageResource(R.drawable.play_button);
                }
            }
        });

        binding.forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMediastarted){
                    int currPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    int nextPosition = currPosition + 10000 > duration ? duration : currPosition + 10000;
                    mediaPlayer.seekTo(nextPosition);

                    binding.seekBar.setMax(duration);
                    binding.seekBar.setProgress(nextPosition);
                }
            }
        });
        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMediastarted){
                    int currPosition = mediaPlayer.getCurrentPosition();
                    int duration = mediaPlayer.getDuration();
                    int nextPosition = currPosition - 10000 < 0 ? 0 : currPosition - 10000;
                    mediaPlayer.seekTo(nextPosition);

                    binding.seekBar.setMax(duration);
                    binding.seekBar.setProgress(nextPosition);
                }
            }
        });

        binding.seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return true;
            }
        });

    }
    private void startMusic() {
        mAudioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);

    /* Request audio focus so in order to play the audio file. The app needs to play a
        audio file, so we will request audio focus for unknown duration
       with AUDIOFOCUS_GAIN*/
        int result = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //for API >= 26
            result = mAudioManager.requestAudioFocus((new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build());
        } else {
            result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //create player
            mediaPlayer = MediaPlayer.create(this, R.raw.meditation);
            //start playing
            Log.d("OnCreate method", "OnCreate player created");
            mediaPlayer.start();
            //listen for completition of playing
            mediaPlayer.setOnCompletionListener(mCompletitionListener);
        }
    }
    private void startMediaPlayerTimer(){
        mediaPlayerUpdateDisplaysTimer = new Timer();
        mediaPlayerUpdateDisplaysTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                int mediaDuration = mediaPlayer.getDuration();
                int mediaCurrentPosition = mediaPlayer.getCurrentPosition();

                binding.seekBar.setMax(mediaDuration);
                binding.seekBar.setProgress(mediaCurrentPosition);

                long curDuration_sec = TimeUnit.MILLISECONDS.toSeconds(mediaCurrentPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaCurrentPosition));
                long curDuration_min = TimeUnit.MILLISECONDS.toMinutes(mediaCurrentPosition);
                String curDuration_sec_str = curDuration_sec > 9 ? String.valueOf(curDuration_sec) : "0" + String.valueOf(curDuration_sec);
                String curDuration_min_str = curDuration_min > 9 ? String.valueOf(curDuration_min) : "0" + String.valueOf(curDuration_min);
                String curPosStr = curDuration_min_str + ":" + curDuration_sec_str;
                binding.currentPosition.setText(curPosStr);

                int remainingDuration = mediaDuration - mediaCurrentPosition;
                long remainingDuration_sec = TimeUnit.MILLISECONDS.toSeconds(remainingDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingDuration));
                long remainingDuration_min = TimeUnit.MILLISECONDS.toMinutes(remainingDuration);
                String remainingDuration_sec_str = remainingDuration_sec > 9 ? String.valueOf(remainingDuration_sec) : "0" + String.valueOf(remainingDuration_sec);
                String remainingDuration_min_str = remainingDuration_min > 9 ? String.valueOf(remainingDuration_min) : "0" + String.valueOf(remainingDuration_min);
                String remPosStr = remainingDuration_min_str + ":" + remainingDuration_sec_str;

                binding.remainingPosition.setText(remPosStr);

            }
        }, 0, 1000);
    }
    private void resetMediaPlayerDisplay(){
        binding.seekBar.setProgress(0);
        binding.currentPosition.setText("00:00");
        binding.remainingPosition.setText("20:05");
    }
    private void releaseMediaPlayer() {

        // If the media player is not null, then it may be currently playing a sound.
        if (mediaPlayer != null) {
            mediaPlayerUpdateDisplaysTimer.cancel();
            mediaPlayer.release();
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            mediaPlayer = null;
            resetMediaPlayerDisplay();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    private MediaPlayer.OnCompletionListener mCompletitionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mediaPlayer.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                length = mediaPlayer.getCurrentPosition();
                Log.d("loss focus", "loss of length: " + length);
                releaseMediaPlayer();
            }
        }
    };


}