package com.example.chattington.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.chattington.R
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chattington.recycler_view.Message
import com.example.chattington.recycler_view.MessageAdapter
import com.example.chattington.room_database.ChatHistory
import com.example.chattington.room_database.ChatHistoryDao
import com.example.chattington.room_database.ChatHistoryDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import com.example.chattington.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.lang.Integer.min
import java.util.*

class ChatFragment : Fragment() {
    // chat UI components
    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeTextView: LinearLayout
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var micButton: ImageView
    private lateinit var headerText: TextView

    // chat information
    private var chatId: Long = -1L
    private var chatTitle = "New Chat"
    private var messageList = mutableListOf<Message>()

    // for OpenAI API calls
    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val client = OkHttpClient()

    // for Room database storage
    private lateinit var db: ChatHistoryDatabase
    private lateinit var chatHistoryDao: ChatHistoryDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // get the view
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // initialize the room database
        val context = requireContext().applicationContext
        db = ChatHistoryDatabase.getInstance(requireContext().applicationContext)
        chatHistoryDao = db.chatHistoryDao()

        headerText = view.findViewById(R.id.tv_ChatDescription)
        // get the passed chat id from the bundle
        chatId = arguments?.getLong("chat_id") ?: -1L

        // get all necessary UI elements for the chat
        recyclerView = view.findViewById(R.id.rv_Messages)
        welcomeTextView = view.findViewById(R.id.ll_NewChatMessage)
        messageEditText = view.findViewById(R.id.et_MessageInput)
        sendButton = view.findViewById(R.id.ib_SendBtn)
        // Setup recycler view
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val llm = LinearLayoutManager(context)
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm

        // get the chat title and message list from the database using the chat id only if it is not 0
        if (chatId != -1L) {
            GlobalScope.launch(Dispatchers.IO) {
                welcomeTextView.visibility = View.GONE

                // get the chat history containing this id
                val chatHistory = chatHistoryDao.getChatHistory(chatId)
                // get the chat title
                chatTitle = chatHistory.title

                val updatedMessageList = mutableListOf<Message>()

                // add each message to the chat
                updatedMessageList.addAll(chatHistory.messages)

                withContext(Dispatchers.Main) {
                    // set the title of the chat on the header
                    headerText.text = chatTitle
                    // update the messageList with the new messages
                    messageList.clear()
                    messageList.addAll(updatedMessageList)
                    // notify the adapter that the data has changed
                    messageAdapter.notifyDataSetChanged()
                }
            }
        }
        else {
            // set the title of the "new chat" on the header
            headerText.text = chatTitle
        }

        // on click listener for chat send button
        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim()
            callAPI(question)
            messageEditText.setText("")
            welcomeTextView.visibility = View.GONE
        }


        // get all necessary UI elements for speech to text
        micButton = view.findViewById(R.id.iv_MicBtn)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {
                micButton.setImageResource(R.drawable.ic_mic_active)
                messageEditText.setText("")
                messageEditText.hint = "Listening..."
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(v: Float) {}
            override fun onBufferReceived(bytes: ByteArray) {}
            override fun onEndOfSpeech() {
                micButton.setImageResource(R.drawable.ic_mic_inactive)
            }
            override fun onError(i: Int) {
                micButton.setImageResource(R.drawable.ic_mic_inactive)
                messageEditText.setText("")
                messageEditText.hint = "Please try again..."
            }
            override fun onResults(bundle: Bundle) {
                micButton.setImageResource(R.drawable.ic_mic_inactive)
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                messageEditText.setText(data?.get(0))
                messageEditText.hint = "Write here"
            }
            override fun onPartialResults(bundle: Bundle) {
                micButton.setImageResource(R.drawable.ic_mic_inactive) // Assuming you have ic_mic_inactive drawable
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                messageEditText.setText(data?.get(0))
            }
            override fun onEvent(i: Int, bundle: Bundle) {}
        })

        micButton.setOnClickListener {
            // check if permission is granted
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                // check if device is running Android 6.0+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    micButton.setImageResource(R.drawable.ic_mic_active) // Assuming you have ic_mic_active drawable
                    speechRecognizer.startListening(speechRecognizerIntent)
                    messageEditText.setText("")
                    messageEditText.hint = "Listening..."
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), 1)

                // if permission is granted, start listening
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    // check if device is running Android 6.0+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        micButton.setImageResource(R.drawable.ic_mic_active) // Assuming you have ic_mic_active drawable
                        speechRecognizer.startListening(speechRecognizerIntent)
                        messageEditText.setText("")
                        messageEditText.hint = "Listening..."
                    }
                }
            }
        }

        return view
    }

    // ---------- FUNCTIONS FOR CHAT UI FUNCTIONALITIES -------------
    private fun addToChat(message: String, sentBy: String) {
        requireActivity().runOnUiThread {
            messageList.add(Message(message, sentBy))
            messageAdapter.notifyDataSetChanged()
            recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
        }
    }
    private fun addResponse(response: String) {
        messageList.removeAt(messageList.size - 1)
        addToChat(response, Message.SENT_BY_BOT)
    }

    // ---------- FUNCTION FOR API CALL -----------------------------
    private fun callAPI(question: String) {
        // add the question to the chat
        addToChat(question, Message.SENT_BY_ME)
        addToChat("Typing... ", Message.SENT_BY_BOT)

        // create a JSON object to hold the request body
        val jsonBody = JSONObject()
        try {
            // create a JSON array to hold the processed messages
            val messagesJsonArray = JSONArray()

            // process the message list to match the format required by the API
            for (i in messageList.indices) {
                // create a JSON object for each message
                val messageJsonObject = JSONObject()
                // check if msg sent by user or bot and set role accordingly
                if (messageList[i].sentBy == Message.SENT_BY_ME) {
                    messageJsonObject.put("role", "user")
                    messageJsonObject.put("content", messageList[i].message)
                } else {
                    messageJsonObject.put("role", "system")
                    messageJsonObject.put("content", messageList[i].message)
                }
                // add the message to the messagesJsonArray
                messagesJsonArray.put(messageJsonObject)
            }

            jsonBody.put("model", "gpt-3.5-turbo")
            jsonBody.put("max_tokens", 4000)
            jsonBody.put("temperature", 0)
            jsonBody.put("messages", messagesJsonArray)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val body = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", BuildConfig.OPEN_API_KEY)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            // API response
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    var responseJsonObj: JSONObject? = null
                    try {
                        responseJsonObj = JSONObject(response.body?.string())
                        val choicesJsonArray = responseJsonObj.getJSONArray("choices")
                        val choicesJsonObj = choicesJsonArray.getJSONObject(0)
                        val result = choicesJsonObj.getJSONObject("message").getString("content")

                        println(result)
                        addResponse(result.trim())

                        // get max 20 characters from the response to use as the chat title
                        if (chatTitle == "New Chat") {
                            chatTitle = result.substring(0, min(20, result.length))
                            chatTitle = chatTitle.trim()
                            // get rid of line breaks and put everything in one line
                            chatTitle = chatTitle.replace("\n", "")
                            chatTitle += "..."
                            // update the header of the chat
                            headerText.text = chatTitle
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body?.string())
                }
            }
        })
    }

    // ---------- FUNCTION FOR WHEN THE FRAGMENT IS RESUMED -------------
    override fun onResume() {
        super.onResume()

        // Add the OnBackStackChangedListener to the FragmentManager
        childFragmentManager.addOnBackStackChangedListener(backStackListener)

        // get the chatId from the arguments
        chatId = arguments?.getLong("chat_id") ?: -1L

        // update the information of the chat
        if (chatId != -1L) {
            GlobalScope.launch(Dispatchers.IO) {
                val chatHistory = chatHistoryDao.getChatHistory(chatId)
                chatTitle = chatHistory.title
                messageList.addAll(chatHistory.messages)
                withContext(Dispatchers.Main) {
                    headerText.text = chatTitle
                    messageAdapter.notifyDataSetChanged()
                    recyclerView.smoothScrollToPosition(messageAdapter.itemCount)
                }
            }
        }
        else {
            // reset the chat title
            chatTitle = "New Chat"
            headerText.text = chatTitle
            messageList.clear()
            messageAdapter.notifyDataSetChanged()
        }
    }

    // ---------- FUNCTIONS FOR ROOM DATABASE STORAGE ---------------
    private val backStackListener = FragmentManager.OnBackStackChangedListener {
    }
    override fun onPause() {
        super.onPause()

        // if there is a response and messageList is not empty, save the chat history to the database
        if (messageList.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                // insert or update the chat history in the room database
                if (chatId == -1L) {
                    val chatHistory = ChatHistory(title = chatTitle, messages = messageList.toList())
                    chatHistoryDao.insertChatHistory(chatHistory)
                } else {
                    val chatHistory = ChatHistory(id = chatId, title = chatTitle, messages = messageList.toList())
                    chatHistoryDao.updateChatHistory(chatHistory)
                }
            }
        }

        // reset the id of the chat
        chatId = -1L

        // Remove the OnBackStackChangedListener from the FragmentManager
        childFragmentManager.removeOnBackStackChangedListener(backStackListener)
        speechRecognizer.destroy()

        // clear the message list
        messageList.clear()
    }

    // ---------- FUNCTIONS FOR PERMISSIONS -------------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        if (requestCode == RecordAudioRequestCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
    companion object {
        const val RecordAudioRequestCode = 1
    }
}