package com.yoanndp.share2save

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ShareReceiverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) { finish(); return }
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val stream: Uri? = intent.getParcelableExtra(Intent.EXTRA_STREAM) ?: intent.data
                if (stream == null) { toast("Aucun fichier reçu"); finish(); return }
                saveAndFinish(stream)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (uris == null || uris.isEmpty()) { toast("Aucun fichier reçu"); finish(); return }
                for (u in uris) saveAndFinish(u, showToast=false)
                toast("${uris.size} fichiers enregistrés")
                finish()
            }
            else -> finish()
        }
    }

    private fun saveAndFinish(src: Uri, showToast: Boolean = true) {
        val mime = contentResolver.getType(src) ?: "application/octet-stream"
        val name = resolveFileName(src, mime)
        try {
            val saved = saveToDownloads(src, name, mime)
            if (showToast) toast("Enregistré: $saved")
        } catch (e: Exception) {
            toast("Échec: ${e.message}")
        } finally {
            if (showToast) finish()
        }
    }

    private fun resolveFileName(uri: Uri, mime: String): String {
        val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
        contentResolver.query(uri, projection, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (idx >= 0) {
                    val n = c.getString(idx)
                    if (!n.isNullOrEmpty()) return n
                }
            }
        }
        val ext = when {
            mime.contains("png") -> ".png"
            mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
            mime.contains("gif") -> ".gif"
            mime.contains("mp4") || mime.contains("video") -> ".mp4"
            else -> ""
        }
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "share2save_${ts}${ext}"
    }

    @Throws(IOException::class)
    private fun saveToDownloads(src: Uri, fileName: String, mime: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativePath = Environment.DIRECTORY_PICTURES + File.separator + "Share2Save"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }
            val resolver = contentResolver
            val collection = when {
                mime.startsWith("image") -> MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                mime.startsWith("video") -> MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                mime.startsWith("audio") -> MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else -> MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            }
            val uri = resolver.insert(collection, values)
                ?: throw IOException("Impossible de créer le fichier")
            resolver.openOutputStream(uri).use { out ->
                resolver.openInputStream(src).use { input ->
                    if (input == null) throw IOException("Impossible d'ouvrir le flux d'entrée")
                    input.copyTo(out!!)
                }
            }
            return "$relativePath/$fileName"
        } else {
            val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val dir = pictures
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, fileName)
            contentResolver.openInputStream(src).use { input ->
                dest.outputStream().use { out ->
                    if (input == null) throw IOException("Impossible d'ouvrir le flux d'entrée")
                    input.copyTo(out)
                }
            }
            return dest.absolutePath
        }
    }

    private fun toast(msg: String) {
        runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show() }
    }

}
