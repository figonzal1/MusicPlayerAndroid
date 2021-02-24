package cl.figonzal.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlaySongActivity : AppCompatActivity() {

    private lateinit var mediaPlayerService2: MediaPlayerService2
    private var serviceBound = false

    private lateinit var buttonPause: Button
    private lateinit var buttonPlay: Button
    private lateinit var tvSongName: TextView

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: MediaPlayerService2.LocalBinder = service as MediaPlayerService2.LocalBinder
            mediaPlayerService2 = binder.getService()
            serviceBound = true
            Toast.makeText(this@PlaySongActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_song)

        buttonPause = findViewById(R.id.btn_pause)
        buttonPlay = findViewById(R.id.btn_play)

        val bundle = intent?.extras!!
        val songList = bundle.getStringArrayList("songList") as ArrayList<String>
        val position = bundle.getInt("position")
        val uri = songList[position]

        playAudio(uri)

        tvSongName = findViewById(R.id.tv_song_name)
        tvSongName.text = intent.extras?.getString("song_name")

    }

    private fun playAudio(uri: String) {

        if (!serviceBound) {
            val intent = Intent(this, MediaPlayerService2::class.java)
            intent.putExtra("media", uri)
            startService(intent)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } else {
            //Service is active
            //Send media with broadcast
        }
    }


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", serviceBound)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("ServiceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            //service is active
            mediaPlayerService2.stopSelf()
        }
    }
}