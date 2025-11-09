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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;
import com.example.ftask.ui.auth.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class AccountFragment extends Fragment {

    private TextView txtFullName, txtEmail, txtPhone, txtRole;
    private TextView txtBalance, txtTotalEarned, txtTotalWithdrawn;
    private ImageView imgAvatar;
    private Button btnLogout;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactionList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Ánh xạ view thông tin người dùng
        imgAvatar = view.findViewById(R.id.imgAvatar);
        txtFullName = view.findViewById(R.id.txtFullName);
        txtEmail = view.findViewById(R.id.txtEmail);
        txtPhone = view.findViewById(R.id.txtPhone);
        txtRole = view.findViewById(R.id.txtRole);

        // Ánh xạ view thông tin ví
        txtBalance = view.findViewById(R.id.txtBalance);
        txtTotalEarned = view.findViewById(R.id.txtTotalEarned);
        txtTotalWithdrawn = view.findViewById(R.id.txtTotalWithdrawn);

        // Ánh xạ RecyclerView lịch sử giao dịch
        rvTransactions = view.findViewById(R.id.rvTransactions);
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(transactionAdapter);

        // Nút đăng xuất
        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());

        // Gọi API lấy dữ liệu
        fetchUserInfo();
        fetchWalletInfo();
        fetchTransactions();

        return view;
    }

    // Lấy thông tin người dùng
    private void fetchUserInfo() {
        String url = "https://ftask.anhtudev.works/users/me";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");
                        txtFullName.setText(result.optString("fullName", "Chưa có tên"));
                        txtEmail.setText(result.optString("email", "Chưa cập nhật email"));
                        txtPhone.setText(result.optString("phone", "N/A"));
                        txtRole.setText(result.optString("role", "Khách hàng"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Không thể tải thông tin tài khoản", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = prefs.getString("accessToken", null);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    // Lấy thông tin ví người dùng
    private void fetchWalletInfo() {
        String url = "https://ftask.anhtudev.works/users/wallet";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");
                        double balance = result.optDouble("balance", 0);
                        double totalEarned = result.optDouble("totalEarned", 0);
                        double totalWithdrawn = result.optDouble("totalWithdrawn", 0);

                        txtBalance.setText(String.format("%,.0f₫", balance));
                        txtTotalEarned.setText("Tổng kiếm được: " + String.format("%,.0f₫", totalEarned));
                        txtTotalWithdrawn.setText("Tổng đã rút: " + String.format("%,.0f₫", totalWithdrawn));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Lỗi xử lý dữ liệu ví", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Không thể tải thông tin ví", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = prefs.getString("accessToken", null);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    // Lấy lịch sử giao dịch
    private void fetchTransactions() {
        String url = "https://ftask.anhtudev.works/users/transactions?page=1&size=20";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");
                        JSONArray content = result.getJSONArray("content");
                        transactionList.clear();
                        for (int i = 0; i < content.length(); i++) {
                            JSONObject obj = content.getJSONObject(i);
                            Transaction tx = new Transaction(
                                    obj.getInt("id"),
                                    obj.optString("type"),
                                    obj.optDouble("amount"),
                                    obj.optDouble("balanceBefore"),
                                    obj.optDouble("balanceAfter"),
                                    obj.optString("description"),
                                    obj.optString("status")
                            );
                            transactionList.add(tx);
                        }
                        transactionAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Lỗi tải lịch sử giao dịch", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Không thể tải lịch sử giao dịch", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = prefs.getString("accessToken", null);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    // Đăng xuất người dùng
    private void logoutUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
