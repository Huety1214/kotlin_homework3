package com.example.lab8

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 優化重點：
 * 1. 新增資料改呼叫 myAdapter.add()，內部用 notifyItemInserted 只刷新新的一列，
 *    取代 notifyDataSetChanged() 整個列表重繪。
 * 2. adapter 用 val 直接初始化，不需要 lateinit。
 * 3. Intent key 集中在 SecActivity 的 companion object。
 */
class MainActivity : AppCompatActivity() {

    private val myAdapter = MyAdapter()

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val data = result.data ?: return@registerForActivityResult
        myAdapter.add(
            Contact(
                name = data.getStringExtra(SecActivity.KEY_NAME).orEmpty(),
                phone = data.getStringExtra(SecActivity.KEY_PHONE).orEmpty()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity) // 預設即為垂直
            adapter = myAdapter
        }
        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            startForResult.launch(Intent(this, SecActivity::class.java))
        }
    }
}
