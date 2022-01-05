package com.example.happybirthday

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class CardActivity : AppCompatActivity() {
    private lateinit var txtHb: TextView
    private lateinit var txtWish: TextView
    private lateinit var imageView: ImageView
    private lateinit var cardView: CardView

    var mMediaPlayer: MediaPlayer? = null
    var title = ""
    var message = ""
    var imageUri: Uri? = null
    var isOn = false
    var textColor = 0
    var bgColor = 0
    var saved = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_card)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txtHb = findViewById(R.id.txtHb)
        txtWish = findViewById(R.id.txtWish)
        imageView = findViewById(R.id.imageView)
        cardView = findViewById(R.id.cardView)

        ContextCompat.getColor(this, R.color.black)

        val bundle = intent.extras
        if (bundle != null) {
            title = bundle.get(constants.title) as String
            message = bundle.get(constants.message) as String
            imageUri = bundle.get(constants.image) as Uri?
            textColor = bundle.get(constants.textColor) as Int
            bgColor = bundle.get(constants.bgColor) as Int
        }

        if (title != "") {
            txtHb.text = title
        }

        txtHb.setTextColor(textColor)
        txtWish.setTextColor(textColor)
        cardView.setCardBackgroundColor(bgColor)
        playSound()
        if (message != "") txtWish.text = message
        if (imageUri == null) {
            imageUri = Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(resources.getResourcePackageName(R.drawable.cake))
                    .appendPath(resources.getResourceTypeName(R.drawable.cake))
                    .appendPath(resources.getResourceEntryName(R.drawable.cake))
                    .build()
        }
        Picasso.get().load(imageUri).into(imageView)
        //if(imageUri != null) imageView.setImageURI()
    }

    fun playSound() {
        isOn = true
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.happybirthday)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
        } else mMediaPlayer!!.start()
    }

    override fun onStop() {
        super.onStop()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    fun pauseSound() {
        if (mMediaPlayer != null && mMediaPlayer!!.isPlaying) mMediaPlayer!!.pause()
        isOn = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.action_save) {
            checkPermissionsAndSave()
            return true
        }
        if (id == R.id.action_music) {
            if (isOn) {
                pauseSound()
            } else {
                playSound()
            }
            return true
        }
        if (id == R.id.action_share) {
            //Save image to be able to share it
            if (!saved) {
                val card = findViewById<CardView>(R.id.cardView)
                imageUri = saveImage(card, this)
            }
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_STREAM, imageUri)
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "Share image via"))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        var screenshot: Bitmap? = null
        try {
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("TAG", "Failed to capture screenshot because:" + e.message)
        }
        return screenshot
    }

    fun saveImage(itemImage: View, activity: Activity): Uri? {
        var fileName: String
        val imageFromView = getScreenShotFromView(itemImage)
        var bitmap: Bitmap

        ByteArrayOutputStream().apply {
            bitmap = Bitmap.createBitmap(imageFromView!!)
            fileName = UUID.randomUUID().toString()
        }

        val directory = File("${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/BirthdayCards/").apply {
            if (!exists())
                mkdirs()
        }
        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Getting the contentResolver
            this.contentResolver?.also { resolver ->

                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/BirthdayCards/")
                }

                // Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                val fos = imageUri?.let { resolver.openOutputStream(it) }
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos!!.flush()
                fos!!.close()
            }
        } else {
            File(directory, "$fileName.jpg").apply {
                FileOutputStream(this).apply {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, this)
                    flush()
                    close()
                }
            }.let {
                activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        ContentValues().apply {
                            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
                            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                            put(MediaStore.Images.Media.DATA, it.absolutePath)
                        }
                )
            }
        }
        Toast.makeText(activity, "Saved on BirthdayCards folder.", Toast.LENGTH_SHORT).show()
        saved = true
        return Uri.parse(directory.absolutePath)
    }

    private fun hasWriteExternalStoragePermission() = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    private fun hasReadExternalStoragePermission() = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun checkPermissionsAndSave() {
        var permissionsToRequest = mutableListOf<String>()
        if (!hasWriteExternalStoragePermission()) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!hasReadExternalStoragePermission()) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        } else {
            val card = findViewById<CardView>(R.id.cardView)
            if (!saved) {
                imageUri = saveImage(card, this)
            } else {
                Toast.makeText(this, "Already saved on BirthdayCards folder.", Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PermissionsRequest", "${permissions[i]} granted")
                    val card = findViewById<CardView>(R.id.cardView)
                    if (!saved) {
                        imageUri = saveImage(card, this)
                    }
                } else {
                    Toast.makeText(this, "No access to media files, can't save picture.", Toast.LENGTH_SHORT)
                            .show()
                    return
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}