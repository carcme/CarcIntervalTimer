/*
 * HIIT Me, a High Intensity Interval Training app for Android
 * Copyright (C) 2015 Alex Gilleran
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.carc.intervaltimer.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.SoundPool;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.ArrayRes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import me.carc.intervaltimer.R;
import me.carc.intervaltimer.model.EffortLevel;
import me.carc.intervaltimer.model.Exercise;
import me.carc.intervaltimer.settings.Preferences;


public class TextToSpeechPlayer implements SoundPlayer, OnInitListener {

	private interface UtteranceEndListener {
		void onUtteranceEnded();
	}

	private AudioManager mAudioManager;

	private final Queue<UtteranceEndListener> endListeners = new LinkedList<>();
	private final HashMap<String, String> speechParams = new HashMap<String, String>();
	private final TextToSpeech textToSpeech;

	private SoundPool mSoundPool;
	private Vibrator mVibrator;
	private int singleBeepSound, alarmSound;
	private long[] vPattern = {0, 300, 100, 300, 100, 300};
	private int[] vAmp = {0, 200, 0, 200, 0, 200};


	private boolean init = false;
	private String missedExText = null;
	private boolean playVoice, playSounds, vibrate;
	private Context context;


	public TextToSpeechPlayer(Context context, AudioManager audioManager) {
		this.mAudioManager = audioManager;
		this.context = context;

		// Audio Beeps
		AudioAttributes attributes = new AudioAttributes.Builder()
				.setUsage(AudioAttributes.USAGE_MEDIA)
				.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
				.build();

		mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(10).build();
		singleBeepSound = mSoundPool.load(context, R.raw.single_beep, 1);
		alarmSound = mSoundPool.load(context, R.raw.request_alarm, 1);

		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

		// TTL
		textToSpeech = new TextToSpeech(context, this);
		textToSpeech.setSpeechRate(1.3f);
		textToSpeech.setOnUtteranceProgressListener(utteranceListener);

		speechParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		// mandatory to listen to utterances even though we don't care about the ID.
		speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");

		playSounds = Preferences.useSounds(context);
		playVoice = Preferences.useVoices(context);
		vibrate = Preferences.useVibrate(context);
	}

	private float getVolume() {
		float actualVolume = (float) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		return actualVolume / maxVolume;
	}


	public void playAlarmSound() {
		if(playSounds) {
			float volume = getVolume();
			mSoundPool.play(alarmSound, volume, volume, 1, 0, 1f);
		}
		if (vibrate) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				mVibrator.vibrate(VibrationEffect.createWaveform(vPattern, vAmp, -1));
			} else
				mVibrator.vibrate(vPattern, -1);
		}
	}


	public void playSingleBeepSound() {
		if(playSounds) {
			float volume = getVolume();
			mSoundPool.play(singleBeepSound, volume, volume, 1, 0, 1f);
		}
		if (vibrate) {
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				mVibrator.vibrate(VibrationEffect.createWaveform(vPattern, vAmp, -1));
			} else
				mVibrator.vibrate(vPattern, -1);
		}
	}


	@Override
	public void playExerciseStart(Exercise exercise) {
		String text = "";

		if(exercise.getEffortLevel().equals(EffortLevel.HARD))
			text = getRandomString(R.array.workTTS);
		if(exercise.getEffortLevel().equals(EffortLevel.EASY))
			text = getRandomString(R.array.prepTTS);
		else if(exercise.getEffortLevel().equals(EffortLevel.REST))
			text = getRandomString(R.array.restTTS);

		if(playVoice) {
			if (init) {
				speak(text);
			} else {
				missedExText = text;
			}
		}
	}

	private void speak(String message) {
		if(playVoice)
			textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, speechParams);
	}

    @Override
    public void playWarningBeep() {
        playSingleBeepSound();
    }

    @Override
	public void playEnd() {
		if(playVoice)
			textToSpeech.speak(getRandomString(R.array.doneTTS), TextToSpeech.QUEUE_FLUSH, speechParams);

/*
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				playAlarmSound();
			}
		}, 1500);
*/
	}


	@Override
	public void cleanUp() {
		if (textToSpeech.isSpeaking()) {
			endListeners.add(new UtteranceEndListener() {
				@Override
				public void onUtteranceEnded() {
					textToSpeech.shutdown();
				}
			});
		} else {
			textToSpeech.shutdown();
		}
	}


	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			init = true;
			if (missedExText != null) {
				speak(missedExText);
			}
		} else
			playVoice = false;
	}


	private UtteranceProgressListener utteranceListener = new UtteranceProgressListener() {
		private OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
			public void onAudioFocusChange(int focusChange) {
				if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
					// Pause playback
					mAudioManager.abandonAudioFocus(afChangeListener);
				} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
					// we don't duck, just abandon focus
					mAudioManager.abandonAudioFocus(afChangeListener);
				} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
					// Resume playback
				} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
					mAudioManager.abandonAudioFocus(afChangeListener);
				}
			}
		};

		@Override
		public void onDone(String utteranceId) {
			abandon();
		}

		@Override
		public synchronized void onError(String utteranceId) {
			abandon();
		}

		private void abandon() {
			mAudioManager.abandonAudioFocus(afChangeListener);

			while (endListeners.size() > 0) {
				endListeners.poll().onUtteranceEnded();
			}
		}

		@Override
		public void onStart(String utteranceId) {
			mAudioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
		}
	};

	private String getRandomString(@ArrayRes int quotesArray) {
		String[] quotes = context.getResources().getStringArray(quotesArray);
		Random rand = new Random();
		int i = rand.nextInt(quotes.length);
		return quotes[i];
	}
}
