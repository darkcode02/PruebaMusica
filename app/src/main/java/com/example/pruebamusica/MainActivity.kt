package com.example.pruebamusica

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import android.content.res.AssetManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    val fd by lazy{
        //assets.openFd("la sirena.mp3")
        assets.openFd(cancionActual)
    }

    val mp by lazy {
        val m = MediaPlayer()
        m.setDataSource(
            fd.fileDescriptor,
            fd.startOffset,
            fd.length
        )
        fd.close()
        m.prepare()
        m
    }

    val controllers by lazy{
        listOf(R.id.btnAnt, R.id.btnStop, R.id.btnPlay, R.id.btnSig, R.id.btnAgregar, R.id.btnEliminar).map{ findViewById<MaterialButton>(it) }
    }

    object ci {
        val ant  = 0
        val stop = 1
        val play = 2
        val sig  = 3
        val agregar = 4
        val eliminar = 5
    }

    val Cancion by lazy {
        findViewById<TextView>(R.id.NombCancion)
    }

    var canciones = mutableListOf<String>()

    var cancionActualIndex = 0
        set(value) {
            var v = if (value == -1) {
                canciones.size - 1
            } else {
                value % canciones.size
            }
            field = v
            cancionActual = canciones[v]
        }

    lateinit var cancionActual: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controllers[ci.play].setOnClickListener { playClicked() }
        controllers[ci.stop].setOnClickListener { stopClicked() }
        controllers[ci.ant].setOnClickListener { antClick() }
        controllers[ci.sig].setOnClickListener { sigClick() }
        controllers[ci.eliminar].setOnClickListener { eliminarCancion() }
        controllers[ci.agregar].setOnClickListener { agregarCancion() }

        val nombArchivos = assets.list("")?.toList() ?: listOf()
        canciones = nombArchivos.filter { it.contains(".mp3") }.toMutableList()

        cancionActual = canciones[cancionActualIndex]
        Cancion.text = cancionActual
    }

    fun playClicked() {
        if (!mp.isPlaying) {
            mp.start()
            controllers[ci.play].setIconResource(R.drawable.baseline_pause_48)
            Cancion.visibility = View.VISIBLE
        } else {
            mp.pause()
            controllers[ci.play].setIconResource(R.drawable.baseline_play_arrow_48)
        }
    }

    fun stopClicked() {
        if (mp.isPlaying) {
            mp.pause()
            controllers[ci.play].setIconResource(R.drawable.baseline_play_arrow_48)
            Cancion.visibility = View.INVISIBLE
        }
        mp.seekTo(0)
    }

    fun sigClick() {
        cancionActualIndex++
        refrescarmusica()
    }

    fun antClick() {
        cancionActualIndex--
        refrescarmusica()
    }

    fun refrescarmusica() {
        mp.reset()
        val fd = assets.openFd(cancionActual)
        mp.setDataSource(
            fd.fileDescriptor,
            fd.startOffset,
            fd.length
        )
        mp.prepare()
        playClicked()
        Cancion.text = cancionActual
    }


    fun eliminarCancion() {
        val musicFolderPath = "C://Users//admin//Desktop//music_android" // Ruta de la carpeta de música
        val musicFolder = File(musicFolderPath)
        if (musicFolder.isDirectory) {
            val files = musicFolder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile && file.name == cancionActual) {
                        val deleted = file.delete()
                        if (deleted) {
                            // Actualizar la lista de canciones después de eliminar la canción
                            canciones.remove(cancionActual)
                            cancionActualIndex = 0
                            refrescarmusica()
                            Cancion.text = cancionActual
                        } else {
                            // No se pudo eliminar el archivo
                            // Manejar el error aquí
                        }
                        break
                    }
                }
            }
        }
    }

    fun agregarCancion() {
        val musicFolderPath = "C://Users//admin//Desktop//music_android" // Ruta de la carpeta de música
        val musicFolder = File(musicFolderPath)
        if (musicFolder.isDirectory) {
            val files = musicFolder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isFile && file.name.endsWith(".mp3")) {
                        try {
                            val inputStream = FileInputStream(file)
                            val outputStream = assets.openFd(file.name).createOutputStream()
                            inputStream.copyTo(outputStream)
                            inputStream.close()
                            outputStream.close()

                            canciones.add(file.name)
                        } catch (e: IOException) {
                            // Manejar el error al copiar el archivo
                        }
                    }
                }
            }
        }
    }

}
