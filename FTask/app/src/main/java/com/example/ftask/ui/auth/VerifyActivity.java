package com.example.ftask.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.ftask.MainActivity;
import com.example.ftask.R;
import com.example.ftask.api.FcmTokenHelper;
import com.example.ftask.ui.auth.CompleteProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class VerifyActivity extends AppCompatActivity {

    private EditText edtOtp;
    private Button btnVerify;
    private String phone;
    private static final String TAG = "VerifyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        edtOtp = findViewById(R.id.edtOtp);
        btnVerify = findViewById(R.id.btnVerify);
        phone = getIntent().getStringExtra("phone");

        btnVerify.setOnClickListener(v -> verifyOtp());
    }

    private void verifyOtp() {
        String otp = edtOtp.getText().toString().trim();

        if (otp.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("phone", phone);
            json.put("otp", otp);
            json.put("role", "CUSTOMER");
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/auth/verify-otp")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(VerifyActivity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    runOnUiThread(() ->
                            Toast.makeText(VerifyActivity.this, "Server trả về dữ liệu trống", Toast.LENGTH_SHORT).show());
                    return;
                }

                String res = responseBody.string();

                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject obj = new JSONObject(res);
                            JSONObject result = obj.getJSONObject("result");
                            String token = result.optString("accessToken");
                            boolean newUser = result.optBoolean("newUser", false);

                            // Lưu accessToken
                            SharedPreferences.Editor editor = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();
                            editor.putString("accessToken", token);
                            editor.apply();

                            // Lấy FCM token và gửi lên server sau khi login thành công
                            registerFCMToken();

                            // Mở Activity tiếp theo
                            Intent intent;
                            if (newUser) {
                                intent = new Intent(VerifyActivity.this, CompleteProfileActivity.class);
                            } else {
                                intent = new Intent(VerifyActivity.this, MainActivity.class);
                            }
                            // Xóa stack để user không back lại Verify
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(VerifyActivity.this, "Mã OTP không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(VerifyActivity.this, "Lỗi khi xử lý phản hồi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void registerFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Lấy token thành công
                        String fcmToken = task.getResult();
                        Log.d(TAG, "FCM Token retrieved: " + fcmToken);

                        // Gửi token lên server thông qua FcmTokenHelper
                        FcmTokenHelper.sendToken(VerifyActivity.this, fcmToken, new FcmTokenHelper.TokenCallback() {
                            @Override
                            public void onSuccess() {
                                Log.i(TAG, "FCM token sent to server successfully after login");
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e(TAG, "Failed to send FCM token to server after login: " + error);
                            }
                        });
                    }
                });
    }
}
