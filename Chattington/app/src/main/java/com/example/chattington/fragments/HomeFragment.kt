package com.example.chattington.fragments

import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chattington.R
import com.example.chattington.recycler_view.Conversation
import com.example.chattington.recycler_view.ConversationAdapter
import com.example.chattington.recycler_view.Message
import com.example.chattington.recycler_view.MessageAdapter
import com.example.chattington.room.ChatHistoryDao
import com.example.chattington.room.ChatHistoryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    // for message UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var conversationAdapter: ConversationAdapter
    private var conversationList = mutableListOf<Conversation>()

    // for Room database storage
    private lateinit var db: ChatHistoryDatabase
    private lateinit var chatHistoryDao: ChatHistoryDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // get the view
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // initialize the room database
        db = ChatHistoryDatabase.getInstance(requireContext().applicationContext)
        chatHistoryDao = db.chatHistoryDao()
        conversationAdapter = ConversationAdapter(conversationList)

        // only make the clear conversations button visible if there are conversations
        GlobalScope.launch {
            // Perform the database operation in the IO dispatcher
            val chatHistories = withContext(Dispatchers.IO) {
                chatHistoryDao.getAllChatHistory()
            }

            // Update the conversation list in the UI thread
            withContext(Dispatchers.Main) {
                if (chatHistories.isEmpty()) {
                    view.findViewById<LinearLayout>(R.id.no_conversations_layout).visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.btn_Clear).visibility = View.GONE
                } else {
                    view.findViewById<LinearLayout>(R.id.no_conversations_layout).visibility = View.GONE
                    view.findViewById<Button>(R.id.btn_Clear).visibility = View.VISIBLE
                }
            }
        }

        // on click listener for the clear conversations button
        view.findViewById<Button>(R.id.btn_Clear).setOnClickListener {
            // start a thread to clear the database
            GlobalScope.launch {
                // Perform the database operation in the IO dispatcher
                withContext(Dispatchers.IO) {
                    chatHistoryDao.deleteAllChatHistory()
                }

                // Update the conversation list in the UI thread
                withContext(Dispatchers.Main) {
                    conversationList.clear()

                    // Notify the adapter and update the UI
                    conversationAdapter.notifyDataSetChanged()
                }
            }
            // make the no conversations layout visible
            view.findViewById<LinearLayout>(R.id.no_conversations_layout).visibility = View.VISIBLE
            // make the clear conversations button invisible
            view.findViewById<Button>(R.id.btn_Clear).visibility = View.GONE
        }

        // Inflate the layout for this fragment
        return view
    }

    // Declare the function to update the RecyclerView
    private fun updateRecyclerView() {
        GlobalScope.launch {
            // Perform the database operation in the IO dispatcher
            val chatHistories = withContext(Dispatchers.IO) {
                chatHistoryDao.getAllChatHistory()
            }

            println("chatHistories: $chatHistories")

            // Update the conversation list in the UI thread
            withContext(Dispatchers.Main) {
                conversationList.clear()
                for (chatHistory in chatHistories) {
                    conversationList.add(Conversation(chatHistory.id, chatHistory.title))
                }

                // Notify the adapter and update the UI
                conversationAdapter.notifyDataSetChanged()
            }
        }
    }

    // when this fragment is opened, update the recycler view
    override fun onResume() {
        super.onResume()

        // print to show that the fragment is being opened
        println("HomeFragment opened")

        // get the view
        val view = requireView()

        // get the recycler view
        recyclerView = view.findViewById(R.id.conversation_recycler_view)
        // create the conversation adapter
        conversationAdapter = ConversationAdapter(conversationList)
        recyclerView.adapter = conversationAdapter
        val llm = LinearLayoutManager(context)
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        // Update the RecyclerView
        updateRecyclerView()
    }

}