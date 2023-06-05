package com.example.chattington.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattington.R
import com.example.chattington.recycler_view.Chat
import com.example.chattington.recycler_view.ChatAdapter
import com.example.chattington.room_database.ChatHistoryDao
import com.example.chattington.room_database.ChatHistoryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    // for message UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var chatList = mutableListOf<Chat>()

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
        chatAdapter = ChatAdapter(chatList)

        // only make the clear chat button visible if there are chats
        GlobalScope.launch {
            // Perform the database operation in the IO dispatcher
            val chatHistories = withContext(Dispatchers.IO) {
                chatHistoryDao.getAllChatHistory()
            }

            // Update the chat list in the UI thread
            withContext(Dispatchers.Main) {
                if (chatHistories.isEmpty()) {
                    view.findViewById<LinearLayout>(R.id.ll_NoChats).visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.btn_ClearChats).visibility = View.GONE
                } else {
                    view.findViewById<LinearLayout>(R.id.ll_NoChats).visibility = View.GONE
                    view.findViewById<Button>(R.id.btn_ClearChats).visibility = View.VISIBLE
                }
            }
        }

        // on click listener for the clear chats button
        view.findViewById<Button>(R.id.btn_ClearChats).setOnClickListener {
            // start a thread to clear the database
            GlobalScope.launch {
                // Perform the database operation in the IO dispatcher
                withContext(Dispatchers.IO) {
                    chatHistoryDao.deleteAllChatHistory()
                }

                // Update the chat list in the UI thread
                withContext(Dispatchers.Main) {
                    chatList.clear()
                    // Notify the adapter and update the UI
                    chatAdapter.notifyDataSetChanged()
                }
            }
            // make the no chat layout visible
            view.findViewById<LinearLayout>(R.id.ll_NoChats).visibility = View.VISIBLE
            // make the clear chat button invisible
            view.findViewById<Button>(R.id.btn_ClearChats).visibility = View.GONE
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

            // Update the chat list in the UI thread
            withContext(Dispatchers.Main) {
                chatList.clear()
                for (chatHistory in chatHistories) {
                    chatList.add(Chat(chatHistory.id, chatHistory.title))
                }

                // Notify the adapter and update the UI
                chatAdapter.notifyDataSetChanged()
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
        recyclerView = view.findViewById(R.id.rv_Chats)
        // create the chat adapter
        chatAdapter = ChatAdapter(chatList)
        recyclerView.adapter = chatAdapter
        val llm = LinearLayoutManager(context)
        llm.stackFromEnd = false
        recyclerView.layoutManager = llm

        // Update the RecyclerView
        updateRecyclerView()
    }
}