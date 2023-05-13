/*
 * Copyright (C) 2022 Eureka Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eurekateam.samsungextras.speaker

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Switch
import androidx.preference.PreferenceFragmentCompat
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.OnMainSwitchChangeListener
import com.eurekateam.samsungextras.R
import java.io.IOException

class ClearSpeakerFragment : PreferenceFragmentCompat(), OnMainSwitchChangeListener {
    private lateinit var mHandler: Handler
    private lateinit var mMediaPlayer: MediaPlayer
    private lateinit var mClearSpeakerPref: MainSwitchPreference
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.clear_speaker_settings)
        mClearSpeakerPref = findPreference(PREF_CLEAR_SPEAKER)!!
        mClearSpeakerPref.addOnSwitchChangeListener(this)
        mHandler = Handler(Looper.getMainLooper())
    }

    override fun onSwitchChanged(switchView: Switch, isChecked: Boolean) {
        if (isChecked) {
            if (startPlaying()) {
                mHandler.removeCallbacksAndMessages(null)
                mHandler.postDelayed({ stopPlaying() }, 30000)
            }
        }
    }

    override fun onStop() {
        stopPlaying()
        super.onStop()
    }

    /**
     * Start playing speaker-clearing audio
     * @return true on success, else false
     */
    private fun startPlaying(): Boolean {
        mMediaPlayer = MediaPlayer()
        requireActivity().volumeControlStream = AudioManager.STREAM_MUSIC
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer.isLooping = true
        try {
            resources.openRawResourceFd(R.raw.clear_speaker_sound).use { file ->
                mMediaPlayer.setDataSource(
                    file.fileDescriptor,
                    file.startOffset,
                    file.length
                )
            }
            mClearSpeakerPref.isEnabled = false
            mMediaPlayer.setVolume(1.0f, 1.0f)
            mMediaPlayer.prepare()
            mMediaPlayer.start()
        } catch (ioe: IOException) {
            Log.e(TAG, "Failed to play speaker clean sound!", ioe)
            return false
        }
        return true
    }

    /**
     * Stops and invalidates
     */
    private fun stopPlaying() {
        if (::mMediaPlayer.isInitialized) {
            mMediaPlayer.stop()
            mMediaPlayer.reset()
            mMediaPlayer.release()
        }
        mClearSpeakerPref.isEnabled = true
        mClearSpeakerPref.isChecked = false
    }

    companion object {
        private val TAG = ClearSpeakerFragment::class.java.simpleName
        private const val PREF_CLEAR_SPEAKER = "clear_speaker_pref"
    }
}
