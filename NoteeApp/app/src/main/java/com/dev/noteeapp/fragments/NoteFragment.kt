package com.dev.noteeapp.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dev.noteeapp.R
import com.dev.noteeapp.activities.MainActivity
import com.dev.noteeapp.adapters.RvNotesAdapter
import com.dev.noteeapp.databinding.FragmentNoteBinding
import com.dev.noteeapp.utils.SwipeToDelete
import com.dev.noteeapp.utils.hideKeyboard
import com.dev.noteeapp.viewModel.NoteActivityViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class NoteFragment : Fragment() {

    private lateinit var noteBinding: FragmentNoteBinding
    private val noteActivityViewModel: NoteActivityViewModel by activityViewModels()
    lateinit var rvAdapter: RvNotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using FragmentNoteBinding
        noteBinding = FragmentNoteBinding.inflate(inflater, container, false)

        // Set enter transition
        var enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
        enterTransition = enterTransition

        // Set exit transition
        var exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        exitTransition = exitTransition

        return noteBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //  noteBinding = FragmentNoteBinding.bind(view)
        // Set up views and listeners
        val activity = activity as MainActivity
        val navController = Navigation.findNavController(view)

        // Hide keyboard
        requireView().hideKeyboard()

        // Delay setting status bar color to avoid flickering
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            //   activity.window.statusBarColor = Color.WHITE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.statusBarColor = Color.parseColor("#9E9D9D")
        }

        // Set up click listeners for FABs
        noteBinding.addNoteFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())
            Log.d("NoteFragment", "Add note button clicked")

        }
        noteBinding.innerFab.setOnClickListener {
            noteBinding.appBarLayout.visibility = View.INVISIBLE
            navController.navigate(NoteFragmentDirections.actionNoteFragmentToSaveOrDeleteFragment())

        }

        swipeToDelete(noteBinding.rvNote)

        observerDataChanges()
        recyclerViewDisplay()

       noteBinding.search.setOnEditorActionListener { y, actionId, _ ->
           if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                 view.clearFocus()
               requireView().hideKeyboard()
           }
           return@setOnEditorActionListener true
       }

        noteBinding.search.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                noteBinding.moData.isVisible = false
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (s.toString().isNotEmpty()){
                    val text = s.toString()
                    val query= "%$text%"
                    if (query.isNotEmpty()){
                        noteActivityViewModel.searchNote(query).observe(viewLifecycleOwner)
                        {
                                   rvAdapter.submitList(it)
                        }
                    }else{
                        observerDataChanges()
                    }
                }else{
                    observerDataChanges()
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
        noteBinding.rvNote.setOnScrollChangeListener { _, scrollX, scrollY, _, oldScrollY ->
            when{
                scrollY>oldScrollY->{
                    noteBinding.chatFabText.isVisible = false
                }
                scrollX==scrollY->{
                    noteBinding.chatFabText.isVisible = true
                }
                else->{
                    noteBinding.chatFabText.isVisible = true
                }
            }
        }

    }

    private fun swipeToDelete(rvNote: RecyclerView) {

        val swipeToDeleteCallback = object :SwipeToDelete(){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.absoluteAdapterPosition
                val note = rvAdapter.currentList[position]
                var actionBtnTapped = false
                noteActivityViewModel.deleteNote(note)
                noteBinding.search.apply {
                    hideKeyboard()
                    clearFocus()
                }
                if (noteBinding.search.text.toString().isEmpty()){
                    observerDataChanges()
                }
                val snackBar = Snackbar.make(
                    requireView(),"Note Deleted",Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>(){
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        transientBottomBar?.setAction("UNDO"){
                            noteActivityViewModel.saveNote(note)
                            actionBtnTapped = true
                            noteBinding.moData.isVisible = false
                        }
                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                    setAnchorView(R.id.add_note_fab)
                }
                snackBar.setActionTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellowOrange
                    )
                )
                snackBar.show()
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(rvNote)

    }

    private fun observerDataChanges() {
        noteActivityViewModel.getAllNotes().observe(viewLifecycleOwner){list->
              noteBinding.moData.isVisible = list.isEmpty()
            rvAdapter.submitList(list)
        }
    }

    private fun recyclerViewDisplay() {
       when(resources.configuration.orientation){
           Configuration.ORIENTATION_PORTRAIT->setUpRecyclerView(2)
           Configuration.ORIENTATION_LANDSCAPE->setUpRecyclerView(3)
       }
    }

    private fun setUpRecyclerView(spanCount: Int) {

        noteBinding.rvNote.apply {
            layoutManager = StaggeredGridLayoutManager(spanCount,StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            rvAdapter = RvNotesAdapter()
            rvAdapter.stateRestorationPolicy=
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            adapter = rvAdapter
            postponeEnterTransition(300L,TimeUnit.MILLISECONDS)
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }

        observerDataChanges()

    }
}