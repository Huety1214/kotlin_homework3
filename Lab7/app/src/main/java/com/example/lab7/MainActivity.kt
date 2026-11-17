package com.example.lab7

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.use
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 優化重點：
 * 1. TypedArray 用 use { } 自動 recycle，不會忘記釋放資源。
 * 2. 用 List(n) { } 一次建好資料，取代 for 迴圈 + 兩個 ArrayList.add。
 * 3. 數量清單直接由水果清單 map 產生，兩者長度一定一致。
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val priceRange = 10..100
        val items: List<Item> = resources.obtainTypedArray(R.array.image_list).use { array ->
            List(array.length()) { index ->
                Item(
                    photo = array.getResourceId(index, 0),
                    name = "水果${index + 1}",
                    price = priceRange.random()
                )
            }
        }
        val counts = items.indices.map { "${it + 1}個" }

        findViewById<Spinner>(R.id.spinner).adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, counts)

        findViewById<GridView>(R.id.gridView).apply {
            numColumns = 3
            adapter = MyAdapter(this@MainActivity, items, R.layout.adapter_vertical)
        }
        findViewById<ListView>(R.id.listView).adapter =
            MyAdapter(this, items, R.layout.adapter_horizontal)
    }
}
