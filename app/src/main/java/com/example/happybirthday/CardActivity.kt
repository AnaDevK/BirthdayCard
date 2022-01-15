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
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import java.io.File
import androidx.activity.result.contract.ActivityResultContracts
import com.example.happybirthday.databinding.ActivityCardBinding
import java.io.IOException

class CardActivity : AppCompatActivity() {
    private lateinit var txtHb: TextView
    private lateinit var txtWish: TextView
    private lateinit var imageView: ImageView
    private lateinit var cardView: CardView
    private lateinit var binding: ActivityCardBinding

    var mMediaPlayer: MediaPlayer? = null
    var title = ""
    var message = ""
    var imageUri: Uri? = null
    var isOn = false
    var textColor = 0
    var bgColor = 0
    var saved = false

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val card = findViewById<CardView>(R.id.cardView)
                if (!saved) {
                    imageUri = saveImage(card, this)
                } else {
                    Toast.makeText(
                        this,
                        "Already saved on BirthdayCards folder.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied :(")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardBinding.inflate(layoutInflater)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        txtHb = binding.txtHb
        txtWish = binding.txtWish
        imageView = binding.imageView
        cardView = binding.cardView
        setContentView(cardView)
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
        //playSound()
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
            checkPermissionsAndSave()
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(Intent.EXTRA_STREAM, imageUri)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share image via"))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        var screenshot: Bitmap? = null
        try {
            screenshot =
                Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("TAG", "Failed to capture screenshot because:" + e.message)
        }
        return screenshot
    }

    fun saveImage(itemImage: View, activity: Activity): Uri? {
        val fileName: String
        val bitmap = getScreenShotFromView(itemImage)

        val timeStamp: String = System.currentTimeMillis().toString()
        fileName = "BdCard$timeStamp"

        //Create a folder to save birthdays cards
        val directory =
            File("${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}/BirthdayCards/").apply {
                if (!exists())
                    mkdirs()
            }

        if (savePhotoToExternalStorage(fileName, bitmap!!, directory)) {
            Toast.makeText(activity, "Saved on BirthdayCards folder.", Toast.LENGTH_SHORT).show()
            saved = true
            return Uri.parse(directory.absolutePath + "/$fileName.jpg")
        } else {
            Toast.makeText(activity, "Error saving the image.", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    private fun savePhotoToExternalStorage(name: String, bmp: Bitmap, path: File): Boolean {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/BirthdayCards/"
                )
            } else {
                val file = File(path, "$name.jpg")
                put(
                    MediaStore.Images.Media.DATA, file.absolutePath
                )
            }
        }
        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch (e: IOException) {
            Log.e("TAG", "Failed to save image because:" + e.message)
            false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun checkPermissionsAndSave() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                val card = findViewById<CardView>(R.id.cardView)
                if (!saved) {
                    imageUri = saveImage(card, this)
                } else {
                    Toast.makeText(
                        this,
                        "Already saved on BirthdayCards folder.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                cardView.showSnackbar(
                    cardView,
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
}

fun View.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this)
        }.show()
    } else {
        snackbar.show()
    }
}