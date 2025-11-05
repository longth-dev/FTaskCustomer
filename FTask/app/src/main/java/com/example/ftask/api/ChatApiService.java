package com.example.ftask.api;

import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChatApiService {
    private static final String BASE_URL = "https://ftask.anhtudev.works";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private OkHttpClient client;

    public ChatApiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    public void getConversations(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/conversations")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void getMessages(String token, String conversationId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/messages/" + conversationId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void sendMessage(String token, String conversationId, String partnerId,
                            String content, String type, Callback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("conversationId", conversationId);
            json.put("partnerId", partnerId);
            json.put("content", content);
            json.put("type", type);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/chat/send")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void markAsRead(String token, String conversationId, Callback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("conversationId", conversationId);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/chat/mark-read")
                    .addHeader("Authorization", "Bearer " + token)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createConversation(String token, String partnerId, Callback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("partnerId", partnerId);

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/chat/conversations")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMessage(String token, String messageId, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/chat/messages/" + messageId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        client.newCall(request).enqueue(callback);
    }
}
