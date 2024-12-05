package com.example.bluetoothesp32

import androidx.recyclerview.widget.DiffUtil

class ESPDiffCallback(
    private val oldList: List<ESP>,
    private val newList: List<ESP>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].ip == newList[newItemPosition].ip
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        return oldItem.temperature == newItem.temperature &&
                oldItem.humidite == newItem.humidite &&
                oldItem.humiditeSol == newItem.humiditeSol &&
                oldItem.luminositeSuffisante == newItem.luminositeSuffisante
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return null
    }
}

