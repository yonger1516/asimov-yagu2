package com.seven.asimov.it.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import com.seven.asimov.it.IntegrationTestRunnerGa;

public final class MediaUtil implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private static MediaUtil mediaUtil = null;

    private MediaUtil() {
    }

    public static MediaUtil init() {
        if (mediaUtil == null) {
            mediaUtil = new MediaUtil();
        }
        return mediaUtil;
    }

    public void play() throws Exception {
        if (mediaPlayer.isPlaying()) {
            return;
        }
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(IntegrationTestRunnerGa.getTestMedia());
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(1.0f , 1.0f);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    public void stop() throws Exception {
        mediaPlayer.reset();
        mediaPlayer.stop();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
    }
}
