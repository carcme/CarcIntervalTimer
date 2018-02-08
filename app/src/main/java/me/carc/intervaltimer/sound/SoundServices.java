package me.carc.intervaltimer.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

import me.carc.intervaltimer.R;


/**
 * Created by bamptonm on 05/02/2018.
 */

public class SoundServices {

    private SoundPool mSoundPool;
    private AudioManager mAudioManager;
    private Vibrator mVibrator;

    private int singleBeepSound, alarmSound;
    private long[] vPattern = {0, 300, 100, 300, 100, 300};
    private int[] vAmp = {0, 200, 0, 200, 0, 200};


    private TextToSpeech mTextToSpeech;
    private final HashMap<String, String> speechParams = new HashMap<String, String>();


    public SoundServices(Context applicationContext) {
        mAudioManager = (AudioManager)applicationContext.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(10).build();
        singleBeepSound = mSoundPool.load(applicationContext, R.raw.single_beep, 1);
        alarmSound = mSoundPool.load(applicationContext, R.raw.request_alarm, 1);

        mVibrator = (Vibrator) applicationContext.getSystemService(Context.VIBRATOR_SERVICE);

        initTextToSpeech(applicationContext);
    }

    private void initTextToSpeech(Context context) {
        speechParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
        speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");

        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    mTextToSpeech.setLanguage(Locale.getDefault());
                }
            }
        });
    }

    public void playVoice(String msg) {
        mTextToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, speechParams);
    }


    private float getVolume() {
        float actualVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return actualVolume / maxVolume;
    }


    public void playAlarmSound(boolean mute, boolean vibrate) {
        float volume = getVolume();
        if (!mute)
            mSoundPool.play(alarmSound, volume, volume, 1, 0, 1f);

        if (vibrate) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mVibrator.vibrate(VibrationEffect.createWaveform(vPattern, vAmp, -1));
            } else
                mVibrator.vibrate(vPattern, -1);
        }
    }



    public void playSingleBeepSound(boolean mute, boolean vibrate) {
        float volume = getVolume();
        if (!mute)
            mSoundPool.play(singleBeepSound, volume, volume, 1, 0, 1f);

        if (vibrate) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mVibrator.vibrate(VibrationEffect.createWaveform(vPattern, vAmp, -1));
            } else
                mVibrator.vibrate(vPattern, -1);
        }
    }
}
