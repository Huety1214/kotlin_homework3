package com.example.lab8

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * 優化重點：
 * 1. 資料改由 Adapter 自己管理（add / remove），外部不用碰 ArrayList。
 * 2. 刪除時用 bindingAdapterPosition + notifyItemRemoved，只更新被刪的那一列並有動畫，
 *    也避免同名同電話的聯絡人被 remove(item) 刪錯筆。
 * 3. 點擊監聽在 onCreateViewHolder 設定一次，不在每次 bind 時重建 lambda。
 */
class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    private val data = mutableListOf<Contact>()

    fun add(contact: Contact) {
        data.add(contact)
        notifyItemInserted(data.lastIndex)
    }

    private fun removeAt(position: Int) {
        if (position !in data.indices) return
        data.removeAt(position)
        notifyItemRemoved(position)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvPhone: TextView = v.findViewById(R.id.tvPhone)
        val imgDelete: ImageView = v.findViewById(R.id.imgDelete)

        fun bind(item: Contact) {
            tvName.text = item.name
            tvPhone.text = item.phone
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_row, parent, false)
        return ViewHolder(v).apply {
            imgDelete.setOnClickListener { removeAt(bindingAdapterPosition) }
        }
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(data[position])
}
