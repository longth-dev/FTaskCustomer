package com.example.ftask.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import android.content.Intent;

import com.example.ftask.R;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPhone;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPhone = findViewById(R.id.edtPhone);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {
        String phone = edtPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("phone", phone);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/auth/send-otp")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "OTP đã được gửi!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, VerifyActivity.class);
                        intent.putExtra("phone", phone);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Gửi OTP thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
