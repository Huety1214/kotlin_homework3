# Kotlin Homework3 報告（Lab7 購物清單、Lab8 通訊錄、Lab9 龜兔賽跑與體位檢測機）

> 繳交檔名：`113360122_林彥廷.pdf`

## 一、讀書會
| 項目 | 內容 |
|---|---|
| 組員 | （填寫） |
| 討論時間 | （填寫） |
| 地點 / Google Meet | （填寫） |
| 照片 / 截圖 | （貼上） |

## 二、GitHub
- 程式連結：`https://github.com/Huety1214/kotlin_homework3`
- GitHub 截圖：（貼上）

## 三、Lab 內容
- **Lab7 購物清單**：Spinner 顯示購買數量，GridView 與 ListView 用自訂的 `MyAdapter` 顯示水果圖片、名稱與亂數價格。
- **Lab8 通訊錄**：RecyclerView 顯示聯絡人，按「新增」開 SecActivity 輸入姓名電話後回傳，點垃圾桶圖示刪除。
- **Lab9_1 龜兔賽跑**：兩位選手同時跑，SeekBar 顯示進度，先到 100 的勝利。**以 Coroutines 實作。**
- **Lab9_2 體位檢測機**：進度條模擬 5 秒檢測後算出標準體重、體脂肪、BMI。**以 Coroutines 實作。**

## 四、優化程式碼心得

**Lab7**
- 原本 `getView()` 每次都 `findViewById`，加入 **ViewHolder**（存在 `view.tag`），重複利用的 View 不用再找元件，捲動更順。
- `TypedArray` 改用 `use { }`，結束時自動 `recycle()`。
- 用 `List(n) { }` 建立水果清單，數量清單由 `items.indices.map { }` 產生，兩份資料長度一定一致。

**Lab8**
- `notifyDataSetChanged()` 會重繪整個列表，改成 `notifyItemInserted()` / `notifyItemRemoved()` 只更新變動的那一列，還有新增/刪除動畫。
- 原本用 `data.remove(item)` 刪除，若有兩筆同名同電話的聯絡人（data class 的 equals 會判定相同），可能刪錯筆。改用 `bindingAdapterPosition` 依位置刪除。
- 點擊監聽在 `onCreateViewHolder` 只設定一次，不在每次 `onBindViewHolder` 重建 lambda。
- 資料由 Adapter 自己管理（`add()`），MainActivity 不需要 `lateinit` 和外部 ArrayList。

**Lab9_1（Coroutines）**
- 兩條 Thread + `Thread.sleep()` + `Handler/Message` 改成 `lifecycleScope.launch` + `delay()`。協程預設在主執行緒，直接更新 SeekBar，**Handler 整段刪除**。
- 原版兔子與烏龜共用 `progressRabbit/progressTurtle` 變數，兩條 Thread 同時讀寫有競爭條件；協程都在主執行緒輪流執行，沒有這個問題。
- 用 `CompletableDeferred` 等第一個抵達的選手，抵達後 `cancel()` 另一位；兩位選手共用 `runRacer()` 函式，只差在步數與是否偷懶。
- Activity 關閉時 `lifecycleScope` 自動取消協程，原版的 Thread 會繼續跑。

```kotlin
lifecycleScope.launch {
    val winner = CompletableDeferred<String>()
    val rabbit = launch { runRacer(sbRabbit, step = 3) { if (lazyChance.random()) delay(300) }; winner.complete("兔子") }
    val turtle = launch { runRacer(sbTurtle, step = 1); winner.complete("烏龜") }
    val name = winner.await()
    rabbit.cancel(); turtle.cancel()
    showToast("${name}勝利")
}
```

**Lab9_2（Coroutines）**
- `Thread` + `runOnUiThread` 改成 `lifecycleScope.launch` + `delay(50)`，進度更新直接寫在迴圈中。
- 計算用 `withContext(Dispatchers.Default) { }` 放到背景，結束後自動回到主執行緒顯示結果。
- 原版在背景 Thread 讀取 `EditText`，而且輸入「.」會因 `toDouble()` 例外而閃退；改成按下按鈕時用 `toDoubleOrNull()` 先驗證。
- 重複按下按鈕時 `job?.cancel()` 取消上一次的檢測，不會有兩個進度同時跑。

## 五、Java Thread 與 Kotlin Coroutines 的差異

| 比較 | Java Thread | Kotlin Coroutines |
|---|---|---|
| 本質 | 作業系統執行緒，每條約佔 1MB 堆疊 | 輕量的可暫停計算，在少數執行緒上排程，可開上萬個 |
| 等待 | `Thread.sleep()` **阻塞**整條執行緒 | `delay()` **暫停**協程，執行緒可去做別的事 |
| 更新 UI | 背景執行緒不能動 UI，要 `Handler` / `runOnUiThread` | 在 `Dispatchers.Main` 直接更新；需要背景時 `withContext` 切換 |
| 取消 | `interrupt()`，需自行處理 `InterruptedException` | `job.cancel()`；`delay()` 等暫停點自動檢查取消 |
| 生命週期 | 不受 Activity 管理，關掉畫面仍會繼續跑，可能記憶體洩漏 | `lifecycleScope` 隨 Activity 銷毀自動取消（結構化並行） |
| 例外 | 例外留在該執行緒，不易回傳 | 例外沿著父子關係往上傳，可用 try/catch 處理 |
| 寫法 | 回呼、巢狀 | 循序寫法，看起來像同步程式 |
| 共享資料 | 多執行緒同時存取需要 `synchronized` | 同一個 Main dispatcher 上的協程輪流執行，不會同時改變數 |

Java 語法
```java
new Thread(() -> {
    try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
    progress += 3;
    Message msg = new Message();
    msg.what = 1;
    handler.sendMessage(msg);   // 要透過 Handler 回主執行緒
}).start();
```
Kotlin 語法
```kotlin
lifecycleScope.launch {
    delay(100)                  // 不阻塞執行緒
    progress += 3
    sbRabbit.progress = progress  // 直接更新 UI
}
```

## 六、心得
（依自己的感想修改）這次最大的收穫是 Coroutines。原本龜兔賽跑要 Thread、Handler、Message 三種東西配合，改寫之後只剩 `launch` 和 `delay`，程式由上往下讀就是比賽流程。也發現原版的 Thread 在關掉 App 後還會繼續跑，用 `lifecycleScope` 就自動解決了。RecyclerView 方面，學到 `notifyItemInserted/Removed` 比 `notifyDataSetChanged` 更有效率。
