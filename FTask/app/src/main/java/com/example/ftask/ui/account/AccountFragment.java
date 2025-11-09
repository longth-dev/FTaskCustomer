package com.example.ftask.ui.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

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
    private Button btnLogout, btnTopUp;
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

        // Nút nạp tiền
        btnTopUp = view.findViewById(R.id.btnTopUp);
        btnTopUp.setOnClickListener(v -> showTopUpDialog());

        // Nút đăng xuất
        btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());

        // Gọi API lấy dữ liệu
        fetchUserInfo();
        fetchWalletInfo();
        fetchTransactions();

        // Xử lý callback từ VNPay
        handleVNPayCallback();

        return view;
    }

    // Hiển thị dialog nhập số tiền nạp
    private void showTopUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("💰 Nạp tiền vào ví");

        // Tạo EditText để nhập số tiền
        final EditText input = new EditText(requireContext());
        input.setHint("Nhập số tiền (VNĐ)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Nạp tiền", (dialog, which) -> {
            String amountStr = input.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int amount = Integer.parseInt(amountStr);
                if (amount < 10000) {
                    Toast.makeText(requireContext(), "Số tiền tối thiểu 10,000₫", Toast.LENGTH_SHORT).show();
                    return;
                }
                initiateTopUp(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Gọi API nạp tiền
    private void initiateTopUp(int amount) {
        // OPTION 1: Nếu bạn đã host HTML redirect page
//        String callbackUrl = "https://yourusername.github.io/vnpay-redirect.html";

        // OPTION 2: Nếu muốn thử Deep Link trực tiếp (có thể không hoạt động trên VNPay Sandbox)
         String callbackUrl = "ftask://payment/callback";

        String url = "https://ftask.anhtudev.works/wallets/top-up?amount=" + amount + "&callbackUrl=" + Uri.encode(callbackUrl);

        android.util.Log.d("TopUp", "URL: " + url);

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    android.util.Log.d("TopUp", "Response: " + response.toString());
                    try {
                        JSONObject result = response.getJSONObject("result");
                        String paymentUrl = result.getString("paymentUrl");

                        android.util.Log.d("TopUp", "Payment URL: " + paymentUrl);

                        // Mở trình duyệt để thanh toán VNPay
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                        startActivity(browserIntent);

                        Toast.makeText(requireContext(), "Đang chuyển đến cổng thanh toán...", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        android.util.Log.e("TopUp", "JSON Error: " + e.getMessage());
                        Toast.makeText(requireContext(), "Lỗi khi khởi tạo thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    String errorMsg = "Lỗi không xác định";
                    if (error.networkResponse != null) {
                        errorMsg = "HTTP " + error.networkResponse.statusCode;
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            android.util.Log.e("TopUp", "Error Response: " + responseBody);
                            errorMsg += ": " + responseBody;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (error.getMessage() != null) {
                        errorMsg = error.getMessage();
                    }
                    android.util.Log.e("TopUp", "Error: " + errorMsg);
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + errorMsg, Toast.LENGTH_LONG).show();
                }) {
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

    // Xử lý callback từ VNPay khi quay lại app
    private void handleVNPayCallback() {
        Intent intent = requireActivity().getIntent();
        Uri data = intent.getData();

        if (data != null && "ftask".equals(data.getScheme())) {
            String vnpOrderInfo = data.getQueryParameter("vnp_OrderInfo");
            String vnpResponseCode = data.getQueryParameter("vnp_ResponseCode");
            String vnpTransactionStatus = data.getQueryParameter("vnp_TransactionStatus");

            if (vnpOrderInfo != null && vnpResponseCode != null && vnpTransactionStatus != null) {
                confirmPayment(vnpOrderInfo, vnpResponseCode, vnpTransactionStatus);
            }
        }
    }

    // Xác nhận thanh toán với server
    private void confirmPayment(String orderInfo, String responseCode, String transactionStatus) {
        String url = "https://ftask.anhtudev.works/payments/confirm?vnp_OrderInfo=" + orderInfo
                + "&vnp_ResponseCode=" + responseCode
                + "&vnp_TransactionStatus=" + transactionStatus;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String message = response.optString("message", "Giao dịch thành công");
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                        // Refresh lại thông tin ví
                        fetchWalletInfo();
                        fetchTransactions();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Lỗi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(requireContext(), "Không thể xác nhận giao dịch", Toast.LENGTH_LONG).show();
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

    // ... (giữ nguyên các phương thức fetchUserInfo, fetchWalletInfo, fetchTransactions, logoutUser)

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

    private void logoutUser() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}