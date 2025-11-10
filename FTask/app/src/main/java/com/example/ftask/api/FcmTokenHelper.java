package com.example.ftask.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FcmTokenHelper {
    private static final String TAG = "FcmTokenHelper";
    private static final String BASE_URL = "https://ftask.anhtudev.works";
    private static final String UPDATE_INFO_ENDPOINT = "/users/update-info";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String PREFS_NAME = "MyPrefs";
    private static final String ACCESS_TOKEN_KEY = "accessToken";

    private final OkHttpClient client;
    private final Context context;

    public FcmTokenHelper(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Gửi FCM token lên server
     * @param fcmToken FCM token cần gửi
     * @param callback Callback để xử lý kết quả (có thể null)
     */
    public void sendFcmTokenToServer(String fcmToken, TokenCallback callback) {
        String accessToken = getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Log.w(TAG, "Access token is empty, cannot send FCM token to server");
            if (callback != null) {
                callback.onFailure("Access token is empty");
            }
            return;
        }

        if (fcmToken == null || fcmToken.isEmpty()) {
            Log.w(TAG, "FCM token is empty");
            if (callback != null) {
                callback.onFailure("FCM token is empty");
            }
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("fcmToken", fcmToken);
        } catch (Exception e) {
            Log.e(TAG, "Error creating JSON", e);
            if (callback != null) {
                callback.onFailure("Error creating JSON: " + e.getMessage());
            }
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + UPDATE_INFO_ENDPOINT)
                .put(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.i(TAG, "============================================");
        Log.i(TAG, "Sending FCM token to server");
        Log.i(TAG, "Token: " + fcmToken);
        Log.i(TAG, "URL: " + BASE_URL + UPDATE_INFO_ENDPOINT);
        Log.i(TAG, "============================================");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "============================================");
                Log.e(TAG, "FAILED to send FCM token to server");
                Log.e(TAG, "Error: " + e.getMessage());
                Log.e(TAG, "============================================");
                if (callback != null) {
                    callback.onFailure("Network error: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    Log.i(TAG, "============================================");
                    Log.i(TAG, "SUCCESS: FCM token sent to server successfully");
                    Log.i(TAG, "Response: " + responseBody);
                    Log.i(TAG, "Status: " + response.code());
                    Log.i(TAG, "============================================");
                    if (callback != null) {
                        callback.onSuccess();
                    }
                } else {
                    Log.e(TAG, "============================================");
                    Log.e(TAG, "FAILED to send FCM token to server");
                    Log.e(TAG, "Status: " + response.code());
                    Log.e(TAG, "Response: " + responseBody);
                    Log.e(TAG, "============================================");
                    if (callback != null) {
                        callback.onFailure("Server error: " + response.code());
                    }
                }
            }
        });
    }

    /**
     * Lấy access token từ SharedPreferences
     */
    private String getAccessToken() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(ACCESS_TOKEN_KEY, null);
    }

    /**
     * Interface để xử lý kết quả gửi token
     */
    public interface TokenCallback {
        void onSuccess();
        void onFailure(String error);
    }

    /**
     * Static method tiện lợi để gửi token
     */
    public static void sendToken(Context context, String fcmToken) {
        new FcmTokenHelper(context).sendFcmTokenToServer(fcmToken, null);
    }

    /**
     * Static method với callback
     */
    public static void sendToken(Context context, String fcmToken, TokenCallback callback) {
        new FcmTokenHelper(context).sendFcmTokenToServer(fcmToken, callback);
    }
}

