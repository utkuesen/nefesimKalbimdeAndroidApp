package Model;

import com.example.nefesimkalbimde.R;

public class MediaPlayerModel {
    private boolean isMediaListening;
    private boolean isMediaStarted;

    public MediaPlayerModel() {
        this.isMediaListening = false;
        this.isMediaStarted = false;
    }

    public boolean isMediaListening() {
        return isMediaListening;
    }

    public boolean isMediaStarted() {
        return isMediaStarted;
    }

    public void setMediaListening(boolean mediaListening) {
        isMediaListening = mediaListening;
    }

    public void setMediaStarted(boolean mediaStarted) {
        isMediaStarted = mediaStarted;
    }
}
