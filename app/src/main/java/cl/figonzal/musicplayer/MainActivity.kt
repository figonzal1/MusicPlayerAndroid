package cl.figonzal.musicplayer

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    private lateinit var requestMultiplePermissions: ActivityResultLauncher<Array<String>>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var listView: ListView
    private val songList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.song_list)

        //checkearPermiso()
        checkearPermisos()
    }

    private fun checkearPermisos() {
        requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {

                    if (it.key.equals("android.permission.READ_EXTERNAL_STORAGE") && it.value) {
                        getMusicInfos()
                    }
                }
            }

        requestMultiplePermissions.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    /*
    private fun checkearPermiso() {

        //CALLBACK
        requestPermissionLauncher =
                registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // Permission is granted. Continue the action or workflow in your
                        // app.
                        //displayAudioSongsName()
                        getMusicInfos()
                    } else {
                        checkearPermisos()
                    }
                }
        //HACER LLAMADA
        requestPermissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    */

    /**
     * REFERENCIA (SHARED MEDIA FILES):
     * https://developer.android.com/training/data-storage/shared/media?hl=es-419#kotlin
     */
    fun getMusicInfos() {

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME
        )
        val selection = null
        val selectionArgs = null
        val sortOrder = null

        val query = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (cursor.moveToNext()) {
                // Use an ID column from the projection to get
                // a URI representing the media item itself.

                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )


                songList.add(contentUri.toString())
            }
        }

        query.run { }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songList)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->

            val songName = songList[position]

            val intent = Intent(this, PlaySongActivity::class.java)
            intent.putExtra("song_name", songName)
            intent.putExtra("position", position)
            intent.putExtra("songList", songList)
            startActivity(intent)
        }
    }
}