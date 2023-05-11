package com.example.chattington.fragments

import android.os.Bundle
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val context = requireContext().applicationContext
        db = Room.inMemoryDatabaseBuilder(context, ChatHistoryDatabase::class.java).build()
        chatHistoryDao = db.chatHistoryDao()

        // Inflate the layout for this fragment
        return view
    }

    // when this fragment is opened, update the recycler view
    override fun onResume() {
        super.onResume()

        // print to show that the fragment is being opened
        println("HomeFragment opened")

        // get the view
        val view = requireView()

        // get chat histories from database, then convert them to conversations
        GlobalScope.launch(Dispatchers.IO) {
            val chatHistories = chatHistoryDao.getAllChatHistory()

            // get the title of each chat history and make a conversation object
            for (chatHistory in chatHistories) {
                val title = chatHistory.title
                val conversation = Conversation(title)
                conversationList.add(conversation)
            }

            withContext(Dispatchers.Main) {
                conversationAdapter.notifyDataSetChanged()
            }
        }

        // get the recycler view
        recyclerView = view.findViewById(R.id.conversation_recycler_view)
        // create the conversation adapter
        conversationAdapter = ConversationAdapter(conversationList)
        recyclerView.adapter = conversationAdapter
        val llm = LinearLayoutManager(context)
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        conversationAdapter.notifyDataSetChanged()
    }
}