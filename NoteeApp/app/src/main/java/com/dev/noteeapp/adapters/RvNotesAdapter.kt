package com.dev.noteeapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dev.noteeapp.R
import com.dev.noteeapp.databinding.NoteItemLayoutBinding
import com.dev.noteeapp.fragments.NoteFragmentDirections
import com.dev.noteeapp.model.Note
import com.dev.noteeapp.utils.hideKeyboard
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonVisitor
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import org.commonmark.node.SoftLineBreak

class RvNotesAdapter : ListAdapter<Note, RvNotesAdapter.NotesViewHolder>(DiffUtilCallback()) {

    inner class NotesViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        private val contentbinding =NoteItemLayoutBinding.bind(itemView)
        val title: MaterialTextView = contentbinding.noteItemTitle
        val content: TextView = contentbinding.noteItemContent
        val date: MaterialTextView = contentbinding.noteDate
        val parent: MaterialCardView = contentbinding.noteItemLayoutParent
        val markwon = Markwon.builder(itemView.context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TaskListPlugin.create(itemView.context))
            .usePlugin(object : AbstractMarkwonPlugin(){
                override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                    super.configureVisitor(builder)
                    builder.on(
                        SoftLineBreak::class.java
                    ){visitor,_ ->visitor.forceNewLine()

                    }
                }
            }).build()

}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
         return NotesViewHolder(
             LayoutInflater.from(parent.context)
                 .inflate(R.layout.note_item_layout,parent,false)
         )
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {

        getItem(position).let {note ->
            holder.apply {
                parent.transitionName = "recyclerView_${note.id}"
                title.text = note.title
                date.text = note.date
                markwon.setMarkdown(content,note.content)
                parent.setCardBackgroundColor(note.color)

                itemView.setOnClickListener {

                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "Recyclerview_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action,extras)

                }
                content.setOnClickListener {
                    val action = NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment()
                        .setNote(note)
                    val extras = FragmentNavigatorExtras(parent to "Recyclerview_${note.id}")
                    it.hideKeyboard()
                    Navigation.findNavController(it).navigate(action,extras)
                }

            }

        }

    }
}
