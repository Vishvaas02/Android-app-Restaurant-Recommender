package com.shravyakothapalli.finalproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;
import okhttp3.Response;

public class ChatGPTActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText prompt;
    List<Message> messageList;
    MessageAdapter messageAdapter;
    MediaType JSON = MediaType.get("application/json");
    OkHttpClient client = new OkHttpClient();
    Call currentCall;
    String filterPrompt = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_gptactivity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chatGPT), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            filterPrompt = extras.getString("Prompt");
        }
        recyclerView = findViewById(R.id.recycler_view);
        prompt = findViewById(R.id.prompt);
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void send(View view) throws JSONException, IOException {
        addNewMessage(prompt.getText().toString(), Message.SENT_BY_ME);
        prompt.setText("");
        callAPI();
    }

    public void addNewMessage(String message, String sentBy) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageList.add(new Message(message, sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());
            }
        });
    }

    public void callAPI() throws IOException, JSONException {
        Request request;
        final String[] content = {""};
        JSONObject jsonBody = new JSONObject();
        JSONObject temp = new JSONObject();
        temp.put("role", "user");
        temp.put("content", "Background on the me: " + filterPrompt + ". This is my question: " + prompt.getText().toString() + ". If I don't have a background, ignore it and just recommend cusinies. Reply should be within 100 words.");
        JSONArray tempArray = new JSONArray();
        tempArray.put(temp);
        jsonBody.put("model", "gpt-4o");
        jsonBody.put("max_tokens", 4000);
        jsonBody.put("temperature", 0);
        jsonBody.put("messages", tempArray);
        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        .header("Authorization", "Bearer " + BuildConfig.OPENAI_API_KEY)        currentCall = client.newCall(request);
        currentCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ContextCompat.getMainExecutor(ChatGPTActivity.this).execute(()  -> {
                    Toast.makeText(ChatGPTActivity.this, "Failed to load response. Please try again!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()) {
                    try {
                            JSONArray jsonArray = new JSONObject(response.body().string()).getJSONArray("choices");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            JSONObject messageObject = jsonObject.getJSONObject("message");
                            content[0] = messageObject.getString("content");
                        addNewMessage(content[0], Message.SENT_BY_GPT);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ContextCompat.getMainExecutor(ChatGPTActivity.this).execute(()  -> {
                            Toast.makeText(ChatGPTActivity.this, "Failed to load response. Please try again!", Toast.LENGTH_SHORT).show();
                    });
                }

            }
        });
    }

    public void goBack(View view) {
        startActivity(new Intent(ChatGPTActivity.this, MapsActivity.class));
    }
}