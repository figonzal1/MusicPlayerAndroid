package cl.figonzal.musicplayer

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.widget.Toast

class MusicPlayerService : Service(),
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        Toast.makeText(this, "Service created", Toast.LENGTH_LONG).show()
        mediaPlayer = MediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "Service started", Toast.LENGTH_LONG).show()

        val bundle = intent!!.extras!!
        val songList = bundle.getStringArrayList("songList") as ArrayList<String>
        val position = bundle.getInt("position")
        val uri: Uri = Uri.parse(songList[position])

        mediaPlayer.apply {
            setWakeMode(this@MusicPlayerService, PowerManager.PARTIAL_WAKE_LOCK)
            setDataSource(this@MusicPlayerService, uri)
            setOnPreparedListener(this@MusicPlayerService)
            prepareAsync()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    inner class MyServiceBinder : Binder() {
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Log.d("MEDIA_PLAYER_SERVICE", "onPrepared called")
        mediaPlayer.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e("MEDIA_PLAYER_SERVICE", "error");
        mp?.reset()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show()
        mediaPlayer.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    fun play(intent: Intent?) {

        Log.d("MEDIA_PLAYER_SERVICE", "client method called")

        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {

            Log.d("MEDIA_PLAYER_SERVICE", "reset media player")
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
        }
        val bundle = intent?.extras!!

        val songList = bundle.getStringArrayList("songList") as ArrayList<String>
        val position = bundle.getInt("position")

        val uri: Uri = Uri.parse(songList[position])

        mediaPlayer = MediaPlayer()

        mediaPlayer?.setOnErrorListener(this@MusicPlayerService)

        mediaPlayer?.apply {
            setWakeMode(this@MusicPlayerService, PowerManager.PARTIAL_WAKE_LOCK)
            setDataSource(this@MusicPlayerService, uri)
            setOnPreparedListener(this@MusicPlayerService)
            prepareAsync()
        }
    }
}