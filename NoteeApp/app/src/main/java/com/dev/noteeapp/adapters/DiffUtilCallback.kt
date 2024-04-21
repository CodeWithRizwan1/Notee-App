package com.dev.noteeapp.adapters

import androidx.recyclerview.widget.DiffUtil
import com.dev.noteeapp.model.Note

class DiffUtilCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        // Implement logic to check if two items represent the same object
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        // Implement logic to check if two items have the same content
        return oldItem.id == newItem.id
    }
}
