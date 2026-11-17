package com.example.lab9_1

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 龜兔賽跑 — 使用 Kotlin Coroutines 改寫。
 *
 * 原版：兩條 Thread + Thread.sleep() + Handler/Message 把進度送回主執行緒。
 * 改寫：lifecycleScope.launch 啟動協程，delay() 暫停但不阻塞執行緒，
 *       協程預設跑在 Main，可以直接更新 SeekBar，不需要 Handler。
 *       Activity 銷毀時 lifecycleScope 會自動取消協程，不會有背景 Thread 繼續跑。
 */
class MainActivity : AppCompatActivity() {

    // 兔子有三分之二的機率會偷懶
    private val lazyChance = listOf(true, true, false)

    private lateinit var btnStart: Button
    private lateinit var sbRabbit: SeekBar
    private lateinit var sbTurtle: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        btnStart = findViewById(R.id.btnStart)
        sbRabbit = findViewById(R.id.sbRabbit)
        sbTurtle = findViewById(R.id.sbTurtle)

        btnStart.setOnClickListener { startRace() }
    }

    private fun showToast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun startRace() {
        btnStart.isEnabled = false
        sbRabbit.progress = 0
        sbTurtle.progress = 0

        lifecycleScope.launch {
            // 第一個抵達終點的選手會 complete，之後的 complete 都會被忽略
            val winner = CompletableDeferred<String>()

            // 兔子：每次跑 3 步，有 2/3 機率偷懶 0.3 秒
            val rabbit = launch {
                runRacer(sbRabbit, step = 3) { if (lazyChance.random()) delay(300) }
                winner.complete("兔子")
            }
            // 烏龜：每次跑 1 步，從不偷懶
            val turtle = launch {
                runRacer(sbTurtle, step = 1)
                winner.complete("烏龜")
            }

            val name = winner.await()
            rabbit.cancel()   // 有人抵達就停止另一位選手
            turtle.cancel()
            showToast("${name}勝利")
            btnStart.isEnabled = true
        }
    }

    /** 選手共用的跑步邏輯：每 0.1 秒前進 step 步，直到 100。 */
    private suspend fun runRacer(bar: SeekBar, step: Int, rest: suspend () -> Unit = {}) {
        var progress = 0
        while (progress < 100) {
            delay(100)
            rest()
            progress += step
            bar.progress = progress   // 協程在 Main 執行，可直接更新 UI
        }
    }
}
