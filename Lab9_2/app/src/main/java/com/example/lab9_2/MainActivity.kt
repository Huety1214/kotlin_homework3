package com.example.lab9_2

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow

/**
 * 體位檢測機 — 使用 Kotlin Coroutines 改寫。
 *
 * 1. Thread + runOnUiThread 改為 lifecycleScope.launch + delay()，
 *    進度更新直接寫在協程裡，不用切回主執行緒。
 * 2. 計算放在 withContext(Dispatchers.Default)，示範如何把運算移到背景再自動切回 Main。
 * 3. 輸入在按下按鈕時就先讀取並用 toDoubleOrNull() 驗證，
 *    原版是在背景 Thread 讀 EditText，且輸入 "." 之類會直接閃退。
 * 4. 計算結果包成 data class，避免三個變數到處傳。
 */
class MainActivity : AppCompatActivity() {

    private data class BodyInput(val height: Double, val weight: Double, val age: Double, val isBoy: Boolean)
    private data class BodyResult(val standWeight: Double, val bodyFat: Double, val bmi: Double)

    private lateinit var edHeight: EditText
    private lateinit var edWeight: EditText
    private lateinit var edAge: EditText
    private lateinit var tvWeightResult: TextView
    private lateinit var tvFatResult: TextView
    private lateinit var tvBmiResult: TextView
    private lateinit var tvProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var llProgress: LinearLayout
    private lateinit var btnBoy: RadioButton

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        edHeight = findViewById(R.id.edHeight)
        edWeight = findViewById(R.id.edWeight)
        edAge = findViewById(R.id.edAge)
        tvWeightResult = findViewById(R.id.tvWeightResult)
        tvFatResult = findViewById(R.id.tvFatResult)
        tvBmiResult = findViewById(R.id.tvBmiResult)
        tvProgress = findViewById(R.id.tvProgress)
        progressBar = findViewById(R.id.progressBar)
        llProgress = findViewById(R.id.llProgress)
        btnBoy = findViewById(R.id.btnBoy)

        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            val height = edHeight.text.toString().toDoubleOrNull()
            val weight = edWeight.text.toString().toDoubleOrNull()
            val age = edAge.text.toString().toDoubleOrNull()
            when {
                height == null || height <= 0 -> showToast("請輸入身高")
                weight == null || weight <= 0 -> showToast("請輸入體重")
                age == null || age <= 0 -> showToast("請輸入年齡")
                else -> startDetection(BodyInput(height, weight, age, btnBoy.isChecked))
            }
        }
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun startDetection(input: BodyInput) {
        job?.cancel() // 重複按下時，取消上一次的檢測
        showResult(null)
        progressBar.progress = 0
        tvProgress.text = "0%"
        llProgress.visibility = View.VISIBLE

        job = lifecycleScope.launch {
            // 模擬檢測：100 次 × 50ms = 5 秒
            for (progress in 1..100) {
                delay(50)
                progressBar.progress = progress
                tvProgress.text = "$progress%"
            }
            val result = withContext(Dispatchers.Default) { calculate(input) }
            llProgress.visibility = View.GONE
            showResult(result)
        }
    }

    private fun calculate(input: BodyInput): BodyResult = with(input) {
        val bmi = weight / (height / 100).pow(2)
        val (standWeight, bodyFat) = if (isBoy) {
            (height - 80) * 0.7 to 1.39 * bmi + 0.16 * age - 19.34
        } else {
            (height - 70) * 0.6 to 1.39 * bmi + 0.16 * age - 9
        }
        BodyResult(standWeight, bodyFat, bmi)
    }

    private fun showResult(r: BodyResult?) {
        fun fmt(v: Double?) = v?.let { "%.2f".format(it) } ?: "無"
        tvWeightResult.text = "標準體重\n${fmt(r?.standWeight)}"
        tvFatResult.text = "體脂肪\n${fmt(r?.bodyFat)}"
        tvBmiResult.text = "BMI\n${fmt(r?.bmi)}"
    }
}
