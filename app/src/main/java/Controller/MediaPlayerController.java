package Controller;

import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.MotionEvent;

import com.example.nefesimkalbimde.MainActivity;
import com.example.nefesimkalbimde.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import Model.MediaPlayerModel;

public class MediaPlayerController {
    private static final int UPDATE_MEDIA_PLAYER_SCREEN_PERIOD_MILlI_SECONDS = 500;
    private static final int MEDIA_POSITION_CHANGE_STEP_MILlI_SECONDS = 10000;
    MainActivity view;
    MediaPlayerModel mediaPlayerModel;
    private MediaPlayer mediaPlayer;
    private AudioManager mAudioManager;

    Timer mediaPlayerUpdateDisplaysTimer;

    public MediaPlayerController(MainActivity activity) {
        view = activity;
        this.mediaPlayerModel = new MediaPlayerModel();
    }

    private void startMediaPlayerTimer(){
        mediaPlayerUpdateDisplaysTimer = new Timer();
        mediaPlayerUpdateDisplaysTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMediaPlayerDisplayOnScreen();
            }
        }, 0, UPDATE_MEDIA_PLAYER_SCREEN_PERIOD_MILlI_SECONDS);
    }

    private boolean updateMediaPlayerDisplayOnScreen(){
        // Update Seekbar on screen
        int mediaDuration = mediaPlayer.getDuration();
        int mediaCurrentPosition = mediaPlayer.getCurrentPosition();
        boolean isSeekBarUpdated = view.setSeekBarProgress(mediaCurrentPosition, mediaDuration);

        // Update current position on screen
        long curDuration_sec = TimeUnit.MILLISECONDS.toSeconds(mediaCurrentPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mediaCurrentPosition));
        long curDuration_min = TimeUnit.MILLISECONDS.toMinutes(mediaCurrentPosition);
        String curDuration_sec_str = curDuration_sec > 9 ? String.valueOf(curDuration_sec) : "0" + String.valueOf(curDuration_sec);
        String curDuration_min_str = curDuration_min > 9 ? String.valueOf(curDuration_min) : "0" + String.valueOf(curDuration_min);
        String curPosStr = curDuration_min_str + ":" + curDuration_sec_str;
        boolean isCurrentPositionUpdated = view.setCurrentPosition(curPosStr);

        // Update remaining position on screen
        int remainingDuration = mediaDuration - mediaCurrentPosition;
        long remainingDuration_sec = TimeUnit.MILLISECONDS.toSeconds(remainingDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(remainingDuration));
        long remainingDuration_min = TimeUnit.MILLISECONDS.toMinutes(remainingDuration);
        String remainingDuration_sec_str = remainingDuration_sec > 9 ? String.valueOf(remainingDuration_sec) : "0" + String.valueOf(remainingDuration_sec);
        String remainingDuration_min_str = remainingDuration_min > 9 ? String.valueOf(remainingDuration_min) : "0" + String.valueOf(remainingDuration_min);
        String remPosStr = remainingDuration_min_str + ":" + remainingDuration_sec_str;
        boolean isRemainingPositionUpdated = view.setRemainingPosition(remPosStr);

        // Update start stop button image
        boolean isMediaPlaying = mediaPlayer.isPlaying();
        if (isMediaPlaying){
            boolean isPlayButtonUpdated = view.setPlayPauseImageViewImageResource(R.drawable.pause_button);
        } else {
            boolean isPlayButtonUpdated = view.setPlayPauseImageViewImageResource(R.drawable.play_button);
        }


        if (isSeekBarUpdated && isCurrentPositionUpdated && isRemainingPositionUpdated){
            return true;
        } else {
            return false;
        }
    }

    public void mediaPlayPauseOnClick(){
        if (mediaPlayerModel.isMediaListening()){
//                    Snackbar.make(view, "Stop music", Snackbar.LENGTH_LONG)
//                            .setAnchorView(R.id.imageView)
//                            .setAction("Action", null).show();
//                    releaseMediaPlayer();
            mediaPlayer.pause();
            mediaPlayerUpdateDisplaysTimer.cancel();
            mediaPlayerModel.setMediaListening(false);
            boolean isPlayButtonUpdated = view.setPlayPauseImageViewImageResource(R.drawable.play_button);
        } else {
            if (mediaPlayerModel.isMediaStarted()) {
                mediaPlayer.start();
            } else {
                startMusic();
                mediaPlayerModel.setMediaStarted(true);
            }
            startMediaPlayerTimer();
            mediaPlayerModel.setMediaListening(true);
            boolean isPlayButtonUpdated = view.setPlayPauseImageViewImageResource(R.drawable.pause_button);
        }
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
                boolean isSeekBarUpdated = view.setSeekBarProgress(nextPosition, duration);
            }
        }
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
                boolean isSeekBarUpdated = view.setSeekBarProgress(nextPosition, duration);
            }
        }
    }

    public void mediaClickOnSeekBar(int progress){

        if(mediaPlayer != null) {
            int duration = mediaPlayer.getDuration();
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.seekTo(progress);
                boolean isSeekBarUpdated = view.setSeekBarProgress(progress, duration);

            } else {
                boolean isSeekBarUpdated = view.setSeekBarProgress(0, duration);

            }
        } else {
            boolean isSeekBarUpdated = view.setSeekBarProgress(0, 100);

        }




    }

    private void startMusic() {
        mAudioManager = (AudioManager) view.getSystemService(view.getApplicationContext().AUDIO_SERVICE);

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
            mediaPlayer = MediaPlayer.create(view, R.raw.meditation);
            //start playing
            Log.d("OnCreate method", "OnCreate player created");
            mediaPlayer.start();
            //listen for completition of playing
            mediaPlayer.setOnCompletionListener(mCompletitionListener);
        }
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
            view.resetMediaPlayerDisplay();
        }
    }

}
