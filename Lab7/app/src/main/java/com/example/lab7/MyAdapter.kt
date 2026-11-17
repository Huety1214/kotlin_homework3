package com.example.lab7

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

/**
 * 優化重點：加入 ViewHolder（存在 view.tag），
 * 重複利用 convertView 時不必每次 findViewById，捲動更順。
 */
class MyAdapter(
    context: Context,
    data: List<Item>,
    private val layout: Int
) : ArrayAdapter<Item>(context, layout, data) {

    private class ViewHolder(view: View) {
        val imgPhoto: ImageView = view.findViewById(R.id.imgPhoto)
        val tvMsg: TextView = view.findViewById(R.id.tvMsg)
    }

    private val isVertical = layout == R.layout.adapter_vertical

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: LayoutInflater.from(parent.context).inflate(layout, parent, false)
                .also { it.tag = ViewHolder(it) }
        val holder = view.tag as ViewHolder
        val item = getItem(position) ?: return view

        holder.imgPhoto.setImageResource(item.photo)
        holder.tvMsg.text = if (isVertical) item.name else "${item.name}: ${item.price}元"
        return view
    }
}
