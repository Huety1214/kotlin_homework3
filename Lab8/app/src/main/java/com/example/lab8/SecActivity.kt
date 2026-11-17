package com.example.lab8

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecActivity : AppCompatActivity() {

    companion object {
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phone"
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sec)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val edName = findViewById<EditText>(R.id.edName)
        val edPhone = findViewById<EditText>(R.id.edPhone)

        findViewById<Button>(R.id.btnSend).setOnClickListener {
            val name = edName.text.toString().trim()
            val phone = edPhone.text.toString().trim()
            when {
                name.isEmpty() -> showToast("請輸入姓名")
                phone.isEmpty() -> showToast("請輸入電話")
                else -> {
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(KEY_NAME, name)
                        putExtra(KEY_PHONE, phone)
                    })
                    finish()
                }
            }
        }
    }
}
