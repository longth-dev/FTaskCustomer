package com.example.ftask.ui.home;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ConfirmOrderActivity extends AppCompatActivity {

    private TextView txtJobDate, txtJobTime, txtJobNote, txtTotalPrice;
    private LinearLayout layoutCash, layoutCard;
    private TextView btnPostJob;

    private LinearLayout selectedPayment = null; // giữ layout được chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        // Ánh xạ view
        txtJobDate = findViewById(R.id.txtJobDate);
        txtJobTime = findViewById(R.id.txtJobTime);
        txtJobNote = findViewById(R.id.txtJobNote);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnPostJob = findViewById(R.id.btnPostJob);
        layoutCash = findViewById(R.id.layoutCash);
        layoutCard = findViewById(R.id.layoutCard);

        // Nhận dữ liệu từ Intent
        int totalPrice = getIntent().getIntExtra("TOTAL_PRICE", 0);
        int variantId = getIntent().getIntExtra("VARIANT_ID", 0);
        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        int hour = getIntent().getIntExtra("SELECTED_HOUR", 0);
        int minute = getIntent().getIntExtra("SELECTED_MINUTE", 0);
        String note = getIntent().getStringExtra("NOTE");

        // Hiển thị thông tin
        txtTotalPrice.setText(String.format(Locale.getDefault(), "%,d VND", totalPrice));
        txtJobDate.setText(selectedDate != null ? selectedDate : "Chưa chọn");
        txtJobTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        txtJobNote.setText(note != null && !note.isEmpty() ? note : "Không có");

        // Hiệu ứng chọn phương thức thanh toán
        setupPaymentSelection();

        // Nút đăng việc
        btnPostJob.setOnClickListener(v -> {
            if (selectedPayment == null) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán!", Toast.LENGTH_SHORT).show();
                return;
            }
            postBooking(variantId, selectedDate, hour, minute, note);
        });
    }

    private void setupPaymentSelection() {
        layoutCash.setOnClickListener(v -> selectPayment(layoutCash));
        layoutCard.setOnClickListener(v -> selectPayment(layoutCard));
    }

    private void selectPayment(LinearLayout selected) {
        // reset hai layout về mặc định
        layoutCash.setBackgroundResource(R.drawable.bg_option_unselected);
        layoutCard.setBackgroundResource(R.drawable.bg_option_unselected);

        // chọn layout
        selected.setBackgroundResource(R.drawable.bg_option_selected);
        selectedPayment = selected;
    }

    private void postBooking(int variantId, String date, int hour, int minute, String note) {
        int addressId = 1; // giả sử địa chỉ mặc định, có thể thay bằng ID thật

        // Chuyển ngày + giờ thành ISO 8601 UTC
        String startAt = convertToISO8601UTC(date, hour, minute);

        JSONObject body = new JSONObject();
        try {
            body.put("variantId", variantId);
            body.put("addressId", addressId);
            body.put("startAt", startAt);
            body.put("customerNote", note != null ? note : "");
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu đặt lịch", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://ftask.anhtudev.works/bookings",
                body,
                response -> {
                    Toast.makeText(this, "Đã đăng việc thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi đăng việc!", Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                String token = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        .getString("accessToken", null);
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        queue.add(request);
    }

    // Chuyển ngày giờ dạng "yyyy-MM-dd" + hour,minute sang ISO 8601 UTC
    private String convertToISO8601UTC(String dateStr, int hour, int minute) {
        if (dateStr == null) dateStr = "2025-11-02"; // mặc định nếu null

        SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        localFormat.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = localFormat.parse(dateStr + " " + String.format("%02d:%02d", hour, minute));
            return isoFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateStr + "T" + String.format("%02d:%02d:00.000Z", hour, minute); // fallback
        }
    }
}
