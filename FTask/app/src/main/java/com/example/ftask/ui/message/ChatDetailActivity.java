package com.example.ftask.ui.message;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import com.example.ftask.R;

import android.content.Context;
import android.content.SharedPreferences;

import android.os.Handler;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ftask.models.Message;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private List<Message> messages;
    private EditText edtMessage;
    private ImageView btnSend, btnBack, imgPartnerAvatar;
    private TextView txtPartnerName, txtPartnerStatus;

    private String conversationId;
    private String partnerId;
    private String partnerName;
    private String partnerAvatar;

    private Handler handler = new Handler();
    private Runnable pollingRunnable;
    private static final int POLLING_INTERVAL = 3000; // 3 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Get data from intent
        conversationId = getIntent().getStringExtra("CONVERSATION_ID");
        partnerId = getIntent().getStringExtra("PARTNER_ID");
        partnerName = getIntent().getStringExtra("PARTNER_NAME");
        partnerAvatar = getIntent().getStringExtra("PARTNER_AVATAR");

        initViews();
        setupHeader();
        setupRecyclerView();
        setupSendMessage();

        loadMessages();
        startPolling();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        imgPartnerAvatar = findViewById(R.id.imgPartnerAvatar);
        txtPartnerName = findViewById(R.id.txtPartnerName);
        txtPartnerStatus = findViewById(R.id.txtPartnerStatus);
    }

    private void setupHeader() {
        txtPartnerName.setText(partnerName);

        if (partnerAvatar != null && !partnerAvatar.isEmpty()) {
            Glide.with(this)
                    .load(partnerAvatar)
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .circleCrop()
                    .into(imgPartnerAvatar);
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        adapter = new MessageAdapter(this, messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);
    }

    private void setupSendMessage() {
        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (!content.isEmpty()) {
                sendMessage(content);
                edtMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/chat/messages/" + conversationId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ChatDetailActivity.this, "Không thể tải tin nhắn", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(res);
                            JSONArray data = jsonResponse.getJSONArray("data");

                            messages.clear();
                            for (int i = 0; i < data.length(); i++) {
                                JSONObject obj = data.getJSONObject(i);

                                Message message = new Message(
                                        obj.getString("id"),
                                        obj.getString("conversationId"),
                                        obj.getString("senderId"),
                                        obj.getString("senderName"),
                                        obj.optString("senderAvatar", ""),
                                        obj.getString("content"),
                                        obj.optString("type", "text"),
                                        obj.getLong("timestamp"),
                                        obj.getBoolean("isRead"),
                                        obj.getBoolean("isSentByMe")
                                );
                                messages.add(message);
                            }

                            adapter.updateMessages(messages);
                            scrollToBottom();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(ChatDetailActivity.this, "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void sendMessage(String content) {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("conversationId", conversationId);
            json.put("partnerId", partnerId);
            json.put("content", content);
            json.put("type", "text");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/chat/send")
                .addHeader("Authorization", "Bearer " + token)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(ChatDetailActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(res);
                            JSONObject data = jsonResponse.getJSONObject("data");

                            Message message = new Message(
                                    data.getString("id"),
                                    data.getString("conversationId"),
                                    data.getString("senderId"),
                                    data.getString("senderName"),
                                    data.optString("senderAvatar", ""),
                                    data.getString("content"),
                                    data.optString("type", "text"),
                                    data.getLong("timestamp"),
                                    false,
                                    true
                            );

                            adapter.addMessage(message);
                            scrollToBottom();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ChatDetailActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void startPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };
        handler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollingRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollingRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startPolling();
    }
}