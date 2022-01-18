package com.example.happybirthday

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class MainActivity : AppCompatActivity() {

    private lateinit var btnCreate: Button
    private lateinit var txtTitle: EditText
    private lateinit var txtMessage: EditText
    private lateinit var btnAddImage: ImageButton
    private lateinit var btnTextColor: ImageButton
    private lateinit var btnBgColor: ImageButton

    private val pickImage = 100
    private var imageUri: Uri? = null
    private var textColor: Int = 0
    private var bgColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        btnCreate = findViewById(R.id.btnCreate)
        txtTitle = findViewById(R.id.etName)
        txtMessage = findViewById(R.id.etMessage)
        btnAddImage = findViewById(R.id.btnAddImage)
        btnTextColor = findViewById(R.id.btnTextColor)
        btnBgColor = findViewById(R.id.btnBgColor)

        textColor = ContextCompat.getColor(this, R.color.black)
        bgColor = ContextCompat.getColor(this, R.color.orange)

        btnCreate.setOnClickListener {
            val intent = Intent(this, CardActivity::class.java)
            intent.setType("text/plain")
                .putExtra(constants.title, txtTitle.text.toString())
                .putExtra(constants.message, txtMessage.text.toString())
                .putExtra(constants.image, imageUri)
                .putExtra(constants.textColor, textColor)
                .putExtra(constants.bgColor, bgColor)
            startActivity(intent)
        }

        btnAddImage.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        btnTextColor.setOnClickListener {
            openColorPicker(true)
        }

        btnBgColor.setOnClickListener {
            openColorPicker(false)
        }

        //Get the values back from preview screen
        val bundle = intent.extras
        if (bundle != null) {
            txtTitle.setText(bundle.get(constants.title) as String)
            val msg = bundle.get(constants.message) as String
            if(getString(R.string.wishes) != msg) {
                txtMessage.setText(msg)
            }
            textColor = bundle.get(constants.textColor) as Int
            bgColor = bundle.get(constants.bgColor) as Int
            imageUri = bundle.get(constants.image) as Uri?
        }
    }

    private fun openColorPicker(isColorText: Boolean) {
        val colorPicker = AmbilWarnaDialog(this, textColor, object: OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                }

                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    if(isColorText) {
                        textColor = color
                    } else {
                        bgColor = color
                    }
                }
            }
        ).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            val intent = Intent(this, PreviewActivity::class.java)
            intent.setType("text/plain")
                .putExtra(constants.title, txtTitle.text.toString())
                .putExtra(constants.message, txtMessage.text.toString())
                .putExtra(constants.image, imageUri)
                .putExtra(constants.textColor, textColor)
                .putExtra(constants.bgColor, bgColor)
            startActivity(intent)
        }
    }
}