package com.example.ftask;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.databinding.ActivityMainBinding;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.navView, navController);

        loadUnreadCount(binding.navView);

        handleIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUnreadCount(binding.navView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            // Xử lý mở Home Fragment
            if (intent.getBooleanExtra("OPEN_HOME_FRAGMENT", false)) {
                navController.navigate(R.id.homeFragment);
            }

            // Xử lý Deep Link từ VNPay callback
            Uri data = intent.getData();
            if (data != null && "ftask".equals(data.getScheme())) {
                String host = data.getHost();

                if ("payment".equals(host)) {
                    // Chuyển đến AccountFragment và xử lý callback
                    navController.navigate(R.id.accountFragment);
                    handleVNPayCallback(data);
                }
            }
        }
    }

    private void handleVNPayCallback(Uri data) {
        String vnpOrderInfo = data.getQueryParameter("vnp_OrderInfo");
        String vnpResponseCode = data.getQueryParameter("vnp_ResponseCode");
        String vnpTransactionStatus = data.getQueryParameter("vnp_TransactionStatus");

        if (vnpOrderInfo != null && vnpResponseCode != null && vnpTransactionStatus != null) {
            confirmPayment(vnpOrderInfo, vnpResponseCode, vnpTransactionStatus);
        }
    }

    private void confirmPayment(String orderInfo, String responseCode, String transactionStatus) {
        String url = "https://ftask.anhtudev.works/payments/confirm?vnp_OrderInfo=" + orderInfo
                + "&vnp_ResponseCode=" + responseCode
                + "&vnp_TransactionStatus=" + transactionStatus;

        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("accessToken", null);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String message = response.optString("message", "Xác nhận giao dịch thành công!");
                        boolean success = response.optBoolean("success", false);

                        if ("00".equals(responseCode) && success) {
                            Toast.makeText(this, "✅ Nạp tiền thành công! " + message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "❌ Giao dịch thất bại: " + message, Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Không thể xác nhận giao dịch", Toast.LENGTH_LONG).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
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

    private void loadUnreadCount(@NonNull BottomNavigationView bottomNavigationView) {
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("accessToken", null);

        if (token == null) {
            return;
        }

        String url = "https://ftask.anhtudev.works/notifications/unread-count";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        int count = response.getInt("result");

                        BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.notificationFragment);

                        if (count > 0) {
                            badge.setVisible(true);
                            badge.setNumber(count);
                            badge.setBackgroundColor(getColor(android.R.color.holo_red_dark));
                        } else {
                            badge.clearNumber();
                            badge.setVisible(false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Không thể tải số thông báo chưa đọc", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }
}