package com.example.happybirthday

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.happybirthday.databinding.ActivityCardBinding
import com.example.happybirthday.databinding.ActivityPreviewBinding
import com.squareup.picasso.Picasso

class PreviewActivity : AppCompatActivity() {

    private lateinit var txtHb: TextView
    private lateinit var txtWish: TextView
    private lateinit var imageView: ImageView
    private lateinit var view: ConstraintLayout
    private lateinit var binding: ActivityPreviewBinding
    private val pickImage = 101
    var title = ""
    var message = ""
    var imageUri: Uri? = null
    var textColor = 0
    var bgColor = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Preview"
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        txtHb = binding.txtHb
        txtWish = binding.txtWish
        imageView = binding.imageView
        view = binding.view
        setContentView(view)
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
        view.setBackgroundColor(bgColor)
        if (message != "") txtWish.text = message
        if(imageUri != null) {
            Picasso.get().load(imageUri).into(imageView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        menu.hasVisibleItems()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_save).setVisible(false)
        menu.findItem(R.id.action_music).setVisible(false)
        menu.findItem(R.id.action_share).setVisible(false)
        menu.findItem(R.id.action_ok).setVisible(true)
        menu.findItem(R.id.action_cancel).setVisible(true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        if (id == R.id.action_ok) {
            //Go to main screen, keep the image
            val intent = Intent(this, MainActivity::class.java)
            intent.setType("text/plain")
                .putExtra(constants.title, txtHb.text.toString())
                .putExtra(constants.message, txtWish.text.toString())
                .putExtra(constants.image, imageUri)
                .putExtra(constants.textColor, textColor)
                .putExtra(constants.bgColor, bgColor)
            startActivity(intent)
            return true
        }
        if (id == R.id.action_cancel) {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            if(imageUri != null) {
                Picasso.get().load(imageUri).into(imageView)
            }
        }
    }
}