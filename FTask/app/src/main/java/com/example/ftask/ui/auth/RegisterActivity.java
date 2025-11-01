package com.example.ftask.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import android.content.Intent;

import com.example.ftask.R;

public class RegisterActivity extends AppCompatActivity {

    EditText edtFullName, edtIdCard, edtPhone, edtEmail, edtReferral, edtPassword;
    Button btnRegister;
    TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtFullName = findViewById(R.id.edtFullName);
        edtIdCard = findViewById(R.id.edtIdCard);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtReferral = findViewById(R.id.edtReferral);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginLink.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void registerUser() {
        String fullName = edtFullName.getText().toString().trim();
        String idCard = edtIdCard.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String referralCode = edtReferral.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (fullName.isEmpty() || idCard.isEmpty() || phone.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("fullName", fullName);
            json.put("idCard", idCard);
            json.put("phone", phone);
            json.put("email", email);
            json.put("referralCode", referralCode);
            json.put("password", password);
            json.put("role", "customer"); // 👈 mặc định role là customer
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/auth/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show());
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + res, Toast.LENGTH_LONG).show());
                }
            }
        });
    }
}
