package com.prush.justanotherplayer.services

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.prush.justanotherplayer.R
import com.prush.justanotherplayer.model.Track
import java.util.*

private val TAG: String = AudioPlayerService::class.java.name

class AudioPlayerService : Service() {

    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var dataSourceFactory: DefaultDataSourceFactory
    private lateinit var simpleExoPlayer: SimpleExoPlayer

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "Received ${intent?.action}")

        when (intent?.action) {
            PlaybackControls.PLAY.name -> {

                val trackPosition = intent.getIntExtra(SELECTED_TRACK_POSITION, -1)

                @Suppress("UNCHECKED_CAST")
                val tracksList: List<Track> =
                    intent.getSerializableExtra(TRACKS_LIST) as List<Track>

                Collections.rotate(tracksList, 0 - trackPosition)

                val concatenatingMediaSource = ConcatenatingMediaSource()

                for (track in tracksList) {

                    val uri: Uri = track.getPlaybackUri()
                    val mediaSource =
                        ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)

                    concatenatingMediaSource.addMediaSource(mediaSource)
                }

                simpleExoPlayer.repeatMode = Player.REPEAT_MODE_ALL
                simpleExoPlayer.prepare(concatenatingMediaSource)
                simpleExoPlayer.playWhenReady = true

                val context: Context = this

                playerNotificationManager =
                    PlayerNotificationManager.createWithNotificationChannel(context,
                        PLAYBACK_CHANNEL_ID,
                        R.string.app_name,
                        R.string.app_name,
                        1,
                        CustomMediaDescriptionAdapter(context, tracksList),
                        object : PlayerNotificationManager.NotificationListener {
                            override fun onNotificationPosted(
                                notificationId: Int,
                                notification: Notification?,
                                ongoing: Boolean
                            ) {
                                startForeground(notificationId, notification)
                            }

                            override fun onNotificationCancelled(
                                notificationId: Int,
                                dismissedByUser: Boolean
                            ) {
                                stopSelf()
                            }
                        })

                playerNotificationManager.setPlayer(simpleExoPlayer)
            }
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        val context: Context = this

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())

        dataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, getString(R.string.app_name))
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        playerNotificationManager.setPlayer(null)
        simpleExoPlayer.release()

    }

    enum class PlaybackControls {
        PLAY,
    }

}