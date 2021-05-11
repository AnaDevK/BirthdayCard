package com.example.happybirthday

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {

    private lateinit var imgBtn: ImageButton
    private lateinit var txtName: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        imgBtn = findViewById(R.id.imgBtn)
        txtName = findViewById(R.id.etName)
        imgBtn.setOnClickListener {
            val intent = Intent(this, CardActivity::class.java)
            intent.setType("text/plain")
                .putExtra(constants.name, txtName.text.toString())
            startActivity(intent)
        }
    }
}