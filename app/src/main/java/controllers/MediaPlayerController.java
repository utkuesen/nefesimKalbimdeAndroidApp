package controllers;


import android.app.Service;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.utmerdesign.nefesimkalbimde.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import Model.MediaPlayerModel;

public class MediaPlayerController extends Service {
    public static boolean isViewAppeared;
    private static final int UPDATE_MEDIA_PLAYER_SCREEN_PERIOD_MILlI_SECONDS = 1000;
    private static final int MEDIA_POSITION_CHANGE_STEP_MILlI_SECONDS = 10000;
 //   MainActivity view;
    MediaPlayerModel mediaPlayerModel;
    private MediaPlayer mediaPlayer;
    private AudioManager mAudioManager;

    Timer mediaPlayerUpdateDisplaysTimer;

    private final IBinder binder = new MyBinder();

    public MediaPlayerController() {
      //  view = activity;
        this.mediaPlayerModel = new MediaPlayerModel();
        isViewAppeared = false;
    }
    public class MyBinder extends Binder {
        public MediaPlayerController getService() {
            return MediaPlayerController.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            String command = intent.getExtras().getString("command");
            switch (command){
                case "mediaPlayPauseOnClick":
                    mediaPlayPauseOnClick();
                    break;
                case "mediaStopOnClick":
                    mediaStopOnClick();
                    break;
                case "mediaForwardOnClick":
                    mediaForwardOnClick();
                    break;
                case "mediaBackOnClick":
                    mediaBackOnClick();
                    break;
                case "mediaClickOnSeekBar":
                    int progress = intent.getExtras().getInt("progress");
                    mediaClickOnSeekBar(progress);
                    break;
                case "viewClosed":
                    viewClosed();
                    break;
                case "viewOpened":
                    viewOpened();
                    break;
                default:
                    break;
            }
        } catch (Exception e){
            System.out.println("onStartCommand error: " + e.getLocalizedMessage());
        }
        return Service.START_STICKY;
    }

    private void viewClosed() {
        isViewAppeared = false;
    }
    private void viewOpened() {
        isViewAppeared = true;
        boolean isMediaListening = mediaPlayerModel.isMediaListening();
        if (isMediaListening){
            updatePlayPauseButtonViewOnScreen(false);
        } else {
            boolean isMediaStarted = mediaPlayerModel.isMediaStarted();
            if (isMediaStarted) {
                updateMediaPlayerDisplayOnScreen();
            } else {
                updatePlayPauseButtonViewOnScreen(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void startMediaPlayerTimer(){
        mediaPlayerUpdateDisplaysTimer = new Timer();
        mediaPlayerUpdateDisplaysTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isViewAppeared){
                    updateMediaPlayerDisplayOnScreen();
                }
            }
        }, 0, UPDATE_MEDIA_PLAYER_SCREEN_PERIOD_MILlI_SECONDS);
    }

    private boolean updateMediaPlayerDisplayOnScreen(){
        // Update Seekbar on screen
        int mediaDuration = mediaPlayer.getDuration();
        int mediaCurrentPosition = mediaPlayer.getCurrentPosition();
     //   boolean isSeekBarUpdated = view.setSeekBarProgress(mediaCurrentPosition, mediaDuration);

        // Update current position on screen
        long curDuration_sec = TimeUnit.MILLISECONDS.toSeconds(mediaCurrentPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaCurrentPosition));
        long curDuration_min = TimeUnit.MILLISECONDS.toMinutes(mediaCurrentPosition);
        String curDuration_sec_str = curDuration_sec > 9 ? String.valueOf(curDuration_sec) : "0" + String.valueOf(curDuration_sec);
        String curDuration_min_str = curDuration_min > 9 ? String.valueOf(curDuration_min) : "0" + String.valueOf(curDuration_min);
        String curPosStr = curDuration_min_str + ":" + curDuration_sec_str;
    //    boolean isCurrentPositionUpdated = view.setCurrentPosition(curPosStr);

        // Update remaining position on screen
        int remainingDuration = mediaDuration - mediaCurrentPosition;
        long remainingDuration_sec = TimeUnit.MILLISECONDS.toSeconds(remainingDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingDuration));
        long remainingDuration_min = TimeUnit.MILLISECONDS.toMinutes(remainingDuration);
        String remainingDuration_sec_str = remainingDuration_sec > 9 ? String.valueOf(remainingDuration_sec) : "0" + String.valueOf(remainingDuration_sec);
        String remainingDuration_min_str = remainingDuration_min > 9 ? String.valueOf(remainingDuration_min) : "0" + String.valueOf(remainingDuration_min);
        String remPosStr = remainingDuration_min_str + ":" + remainingDuration_sec_str;
     //   boolean isRemainingPositionUpdated = view.setRemainingPosition(remPosStr);
        // Update start stop button image
        boolean isPlayButtonUpdated;
        boolean isMediaPlaying = mediaPlayer.isPlaying();
        if (isMediaPlaying){
            isPlayButtonUpdated = true; // view.setPlayPauseImageViewImageResource(R.drawable.pause_button);
        } else {
            isPlayButtonUpdated = false; // view.setPlayPauseImageViewImageResource(R.drawable.play_button);
        }
        broadcastUpdateMediaPlayerDisplayOnScreen(mediaDuration, mediaCurrentPosition, curPosStr, remPosStr, isPlayButtonUpdated);

        return true;
    }

    private void broadcastUpdateMediaPlayerDisplayOnScreen(int mediaDuration, int mediaCurrentPosition, String currentPositionStr, String remainingPositionStr, boolean isPlayButtonUpdated) {
        Intent intent = new Intent("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        intent.putExtra("command", "updateMediaPlayerDisplayOnScreen");
        intent.putExtra("mediaDuration", mediaDuration);
        intent.putExtra("mediaCurrentPosition", mediaCurrentPosition);
        intent.putExtra("currentPositionStr", currentPositionStr);
        intent.putExtra("remainingPositionStr", remainingPositionStr);
        intent.putExtra("isPlayButtonUpdated", isPlayButtonUpdated);
        sendBroadcast(intent);
    }


    public void mediaPlayPauseOnClick(){
        if (mediaPlayerModel.isMediaListening()){
            mediaPlayer.pause();
            mediaPlayerUpdateDisplaysTimer.cancel();
            mediaPlayerModel.setMediaListening(false);
            if (isViewAppeared){
                updatePlayPauseButtonViewOnScreen(true);
            }
        } else {
            if (mediaPlayerModel.isMediaStarted()) {
                mediaPlayer.start();
            } else {
                startMusic();
                mediaPlayerModel.setMediaStarted(true);
            }
            startMediaPlayerTimer();
            mediaPlayerModel.setMediaListening(true);
            if (isViewAppeared){
                updatePlayPauseButtonViewOnScreen(false);
            }
        }
    }

    private void updatePlayPauseButtonViewOnScreen(boolean isButtonPlay) {
        Intent intent = new Intent("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        intent.putExtra("command", "updatePlayPauseButtonViewOnScreen");
        intent.putExtra("isButtonPlay", isButtonPlay);
        sendBroadcast(intent);
    }

    public void mediaStopOnClick(){
        if (mediaPlayerModel.isMediaStarted()){
            releaseMediaPlayer();
            mediaPlayerModel.setMediaListening(false);
            mediaPlayerModel.setMediaStarted(false);
        }
    }

    public void mediaForwardOnClick(){
        if (mediaPlayerModel.isMediaStarted()){
            if (mediaPlayerModel.isMediaStarted()){
                int currPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int nextPosition;
                if (currPosition + MEDIA_POSITION_CHANGE_STEP_MILlI_SECONDS > duration) {
                    nextPosition = duration;
                } else {
                    nextPosition = currPosition + MEDIA_POSITION_CHANGE_STEP_MILlI_SECONDS;
                }
                mediaPlayer.seekTo(nextPosition);
                if (isViewAppeared){
                    updateSeekBarOnScreen(nextPosition, duration);
                }
            }
        }
    }

    private void updateSeekBarOnScreen(int nextPosition, int duration) {
        Intent intent = new Intent("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        intent.putExtra("command", "updateSeekBarOnScreen");
        intent.putExtra("nextPosition", nextPosition);
        intent.putExtra("duration", duration);
        sendBroadcast(intent);
    }

    public void mediaBackOnClick(){
        if (mediaPlayerModel.isMediaStarted()){
            if (mediaPlayerModel.isMediaStarted()){
                int currPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();
                int nextPosition;
                if (currPosition - MEDIA_POSITION_CHANGE_STEP_MILlI_SECONDS < 0) {
                    nextPosition = 0;
                } else {
                    nextPosition = currPosition - MEDIA_POSITION_CHANGE_STEP_MILlI_SECONDS;
                }
                mediaPlayer.seekTo(nextPosition);
                if (isViewAppeared){
                    updateSeekBarOnScreen(nextPosition, duration);
                }
            }
        }
    }

    public void mediaClickOnSeekBar(int progress){
        if(mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(progress);
            }
        }
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
            mediaPlayer.setOnCompletionListener(mCompletionListener);
        }
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
            setMeditationCompletedOnScreen();
        }
    };

    private void setMeditationCompletedOnScreen() {
        Intent intent = new Intent("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        intent.putExtra("command", "setMeditationCompletedOnScreen");
        sendBroadcast(intent);
    }

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mediaPlayer.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                releaseMediaPlayer();
            }
        }
    };

    private void releaseMediaPlayer() {

        // If the media player is not null, then it may be currently playing a sound.
        if (mediaPlayer != null) {
            mediaPlayerUpdateDisplaysTimer.cancel();
            mediaPlayer.release();
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
            mediaPlayer = null;
            if (isViewAppeared){
                resetMediaPlayerDisplay();
            }
        }
    }

    private void resetMediaPlayerDisplay() {
        Intent intent = new Intent("NEFESIM_KALBIMDE_MEDIA_PLAYER_UPDATE");
        intent.putExtra("command", "resetMediaPlayerDisplay");
        sendBroadcast(intent);
    }


}
