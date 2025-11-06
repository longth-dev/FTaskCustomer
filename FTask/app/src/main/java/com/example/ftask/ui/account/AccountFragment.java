package com.example.ftask.ui.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;
import com.example.ftask.ui.auth.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private TextView txtFullName, txtEmail, txtPhone, txtRole;
    private ImageView imgAvatar;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Ánh xạ view
        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtRole = view.findViewById(R.id.txtRole);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Gọi API lấy thông tin user
        fetchUserInfo();

        // Xử lý nút đăng xuất
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void fetchUserInfo() {
        String url = "https://ftask.anhtudev.works/users/me";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");

                        // Lấy dữ liệu từ JSON
                        String fullName = result.optString("fullName", "Chưa có tên");
                        String phone = result.optString("phone", "N/A");
                        String role = result.optString("role", "Khách hàng");
                        String email = result.optString("email", "Chưa cập nhật email");

                        // Hiển thị thông tin lên UI
                        txtFullName.setText(fullName);
                        txtEmail.setText(email);
                        txtPhone.setText(phone);
                        txtRole.setText(role);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Không thể tải thông tin tài khoản", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = prefs.getString("accessToken", null);

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        queue.add(request);
    }


    private void logoutUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
