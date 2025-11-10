package com.example.ftask.ui.auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import android.content.SharedPreferences;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

import com.example.ftask.MainActivity;
import com.example.ftask.R;

public class CompleteProfileActivity extends AppCompatActivity {

    private EditText edtFullName;
    private RadioGroup rgGender;
    private Button btnSubmit;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        edtFullName = findViewById(R.id.edtFullName);
        rgGender = findViewById(R.id.rgGender);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> validateAndSendProfile());
    }

    private void validateAndSendProfile() {
        String fullName = edtFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton rb = findViewById(selectedId);
        String gender = rb.getText().toString().toUpperCase(); // MALE hoặc FEMALE

        sendProfileToServer(fullName, gender);
    }

    private void sendProfileToServer(String fullName, String gender) {
        String token = getToken();
        if (token.isEmpty()) {
            Toast.makeText(this, "Token rỗng, chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("fullName", fullName);
            json.put("gender", gender);
            json.put("fcmToken", "");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/users/update-info")
                .put(body) // PUT để tránh lỗi 405
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(CompleteProfileActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(CompleteProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CompleteProfileActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(CompleteProfileActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private String getToken() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return prefs.getString("accessToken", "");
    }
}
