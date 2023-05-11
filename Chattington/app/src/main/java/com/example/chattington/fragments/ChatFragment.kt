package com.example.chattington.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.chattington.R
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chattington.recycler_view.Message
import com.example.chattington.recycler_view.MessageAdapter
import com.example.chattington.room.ChatHistory
import com.example.chattington.room.ChatHistoryDao
import com.example.chattington.room.ChatHistoryDatabase
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import androidx.test.core.app.ApplicationProvider
import com.example.chattington.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    // for message UI
    private lateinit var recyclerView: RecyclerView
    private lateinit var welcomeTextView: LinearLayout
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private var chatTitle = "Untitled Chat"
    private var messageList = mutableListOf<Message>()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var micButton: ImageView

    // for API call
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

        //get the passed chat title from the bundle
        chatTitle = arguments?.getString("chatTitle") ?: "Untitled Chat"

        // get all necessary UI elements for the chat
        recyclerView = view.findViewById(R.id.chat_recycler_view)
        welcomeTextView = view.findViewById(R.id.welcome_text)
        messageEditText = view.findViewById(R.id.message_edit_text)
        sendButton = view.findViewById(R.id.send_btn)
        // Setup recycler view
        messageAdapter = MessageAdapter(messageList)
        recyclerView.adapter = messageAdapter
        val llm = LinearLayoutManager(context)
        llm.stackFromEnd = true
        recyclerView.layoutManager = llm
        // on click listener for chat send button
        sendButton.setOnClickListener {
            val question = messageEditText.text.toString().trim()
            addToChat(question, Message.SENT_BY_ME)
            messageEditText.setText("")
            callAPI(question)
            welcomeTextView.visibility = View.GONE
        }

        // get all necessary UI elements for speech to text
        micButton = view.findViewById(R.id.mic_btn)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {
                micButton.setImageResource(R.drawable.ic_mic_active) // Assuming you have ic_mic_active drawable
                messageEditText.setText("")
                messageEditText.hint = "Listening..."
            }

            override fun onBeginningOfSpeech() {
            }

            override fun onRmsChanged(v: Float) {
            }

            override fun onBufferReceived(bytes: ByteArray) {
            }

            override fun onEndOfSpeech() {
                micButton.setImageResource(R.drawable.ic_mic_inactive) // Assuming you have ic_mic_inactive drawable
            }

            override fun onError(i: Int) {
                micButton.setImageResource(R.drawable.ic_mic_inactive) // Assuming you have ic_mic_inactive drawable
                messageEditText.setText("")
                messageEditText.hint = "Please try again..."
            }

            override fun onResults(bundle: Bundle) {
                micButton.setImageResource(R.drawable.ic_mic_inactive) // Assuming you have ic_mic_inactive drawable
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                messageEditText.setText(data?.get(0))
                messageEditText.hint = "Write here"
            }

            override fun onPartialResults(bundle: Bundle) {
                micButton.setImageResource(R.drawable.ic_mic_inactive) // Assuming you have ic_mic_inactive drawable
                val data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                messageEditText.setText(data?.get(0))
            }

            override fun onEvent(i: Int, bundle: Bundle) {
            }
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
        // OkHttp
        messageList.add(Message("Typing... ", Message.SENT_BY_BOT))
        val jsonBody = JSONObject()
        try {
            jsonBody.put("model", "text-davinci-003")
            jsonBody.put("prompt", question)
            jsonBody.put("max_tokens", 4000)
            jsonBody.put("temperature", 0)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val body = RequestBody.create(JSON, jsonBody.toString())
        val request = Request.Builder()
            .url("https://api.openai.com/v1/completions")
            .header("Authorization", BuildConfig.OPEN_API_KEY)
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    var jsonObject: JSONObject? = null
                    try {
                        jsonObject = JSONObject(response.body?.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result = jsonArray.getJSONObject(0).getString("text")
                        println(result)
                        addResponse(result.trim())

                        // get the title of the chat by getting the first 5 words of the response
                        if (chatTitle == "Untitled Chat") {
                            val words = result.split(" ")
                            chatTitle = ""
                            for (i in 0..4) {
                                chatTitle += words[i] + " "
                            }
                            chatTitle = chatTitle.trim() + "..."
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

    // ---------- FUNCTIONS FOR ROOM DATABASE STORAGE ---------------
    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        // handle fragment back stack changes here
    }
    override fun onStop() {
        super.onStop()

        // if there are items in the message list, save this chat history to the room database
        if (messageList.isNotEmpty()) {
            GlobalScope.launch(Dispatchers.IO) {
                val context = requireContext().applicationContext
                db = Room.inMemoryDatabaseBuilder(context, ChatHistoryDatabase::class.java).build()
                chatHistoryDao = db.chatHistoryDao()

                // insert this chat history to the room database
                val chatHistory = ChatHistory(title = chatTitle, messages = messageList.toList())
                chatHistoryDao.insertChatHistory(chatHistory)

                // output all saved database items
                val chatHistories = chatHistoryDao.getAllChatHistory()
                for (chatHistory in chatHistories) {
                    println(chatHistory)
                }
                // close the database
                db.close()
            }
        }

        // print that we exited the chat
        println("Exited chat")

        // remove the OnBackStackChangedListener from the FragmentManager
        childFragmentManager.removeOnBackStackChangedListener(backStackListener)

        speechRecognizer.destroy()
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