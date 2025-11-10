package com.example.ftask;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.api.FcmTokenHelper;
import com.example.ftask.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;
    private ActivityResultLauncher<String> requestPermissionLauncher;

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

        // Setup FCM
        setupPermissionLauncher();
        requestNotificationPermission();
        getFCMToken();
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
                else if ("booking-payment".equals(host)) {
                    // Callback thanh toán booking - Chuyển đến ActivityFragment
                    navController.navigate(R.id.activityFragment);
                    handleBookingPaymentCallback(data);
                }
            }
        }
    }

    private void handleVNPayCallback(Uri data) {
        String vnpOrderInfo = data.getQueryParameter("vnp_OrderInfo");
        String vnpResponseCode = data.getQueryParameter("vnp_ResponseCode");
        String vnpTransactionStatus = data.getQueryParameter("vnp_TransactionStatus");

        if (vnpOrderInfo != null && vnpResponseCode != null && vnpTransactionStatus != null) {
            confirmWalletPayment(vnpOrderInfo, vnpResponseCode, vnpTransactionStatus);
        }
    }
    private void handleBookingPaymentCallback(Uri data) {
        String vnpOrderInfo = data.getQueryParameter("vnp_OrderInfo");
        String vnpResponseCode = data.getQueryParameter("vnp_ResponseCode");
        String vnpTransactionStatus = data.getQueryParameter("vnp_TransactionStatus");

        Log.d(TAG, "========================================");
        Log.d(TAG, "Booking Payment Callback");
        Log.d(TAG, "Order Info: " + vnpOrderInfo);
        Log.d(TAG, "Response Code: " + vnpResponseCode);
        Log.d(TAG, "Transaction Status: " + vnpTransactionStatus);
        Log.d(TAG, "========================================");

        // Gọi API confirm để xác nhận thanh toán
        if (vnpOrderInfo != null && vnpResponseCode != null && vnpTransactionStatus != null) {
            confirmBookingPayment(vnpOrderInfo, vnpResponseCode, vnpTransactionStatus);
        } else {
            Toast.makeText(this, "Thiếu thông tin callback từ VNPay!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Gọi API confirm cho thanh toán booking
     */
    private void confirmBookingPayment(String orderInfo, String responseCode, String transactionStatus) {
        String url = "https://ftask.anhtudev.works/payments/confirm?vnp_OrderInfo=" + orderInfo
                + "&vnp_ResponseCode=" + responseCode
                + "&vnp_TransactionStatus=" + transactionStatus;

        Log.d(TAG, "========================================");
        Log.d(TAG, "Confirm Booking Payment URL: " + url);
        Log.d(TAG, "========================================");

        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("accessToken", null);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        Log.d(TAG, "✓ Confirm Response: " + response.toString());

                        String message = response.optString("message", "Xác nhận thanh toán thành công!");
                        int code = response.optInt("code", 0);

                        // Kiểm tra kết quả từ VNPay (00 = thành công)
                        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                            Toast.makeText(this, "✅ Thanh toán booking thành công!\n" + message, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "❌ Thanh toán booking thất bại!\n" + message, Toast.LENGTH_LONG).show();
                        }

                        // Reload lại danh sách booking
                        navController.navigate(R.id.activityFragment);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error parsing confirm response", e);
                        Toast.makeText(this, "Lỗi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "========================================");
                    Log.e(TAG, "✗ Confirm Error");

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errBody = new String(error.networkResponse.data);
                        Log.e(TAG, "Error Body: " + errBody);
                    }

                    error.printStackTrace();
                    Toast.makeText(this, "Không thể xác nhận thanh toán!", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "========================================");
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

    /**
     * Gọi API confirm cho nạp tiền ví
     */
    private void confirmWalletPayment(String orderInfo, String responseCode, String transactionStatus) {
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

                        // Chỉ kiểm tra responseCode từ VNPay (00 = thành công)
                        if ("00".equals(responseCode)) {
                            Toast.makeText(this, "✅ Nạp tiền thành công! " + message, Toast.LENGTH_LONG).show();
                            navController.navigate(R.id.accountFragment);
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

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Notification permission granted");
                        getFCMToken();
                    } else {
                        Log.w(TAG, "Notification permission denied");
                    }
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }
    }

    private void getFCMToken() {
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
                        
                        // Log token ra console (Logcat)
                        Log.d(TAG, "FCM Token: " + fcmToken);
                        Log.i(TAG, "============================================");
                        Log.i(TAG, "FCM TOKEN (copy token này để test từ BE):");
                        Log.i(TAG, fcmToken);
                        Log.i(TAG, "============================================");

                        // Gửi token lên server thông qua FcmTokenHelper
                        // Chỉ gửi nếu đã có accessToken (đã đăng nhập)
                        String accessToken = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                .getString("accessToken", null);
                        if (accessToken != null && !accessToken.isEmpty()) {
                            FcmTokenHelper.sendToken(MainActivity.this, fcmToken, new FcmTokenHelper.TokenCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.i(TAG, "FCM token sent to server successfully");
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.e(TAG, "Failed to send FCM token to server: " + error);
                                }
                            });
                        } else {
                            Log.d(TAG, "Access token not available, FCM token will be sent after login");
                        }
                    }
                });
    }
}
