package com.example.ftask.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.ftask.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private static final String TAG = "BookingAdapter";
    private List<Booking> bookings;
    private Context context;
    private OnBookingCancelledListener cancelListener;

    public interface OnBookingCancelledListener {
        void onBookingCancelled();
    }

    public BookingAdapter(List<Booking> bookings, Context context, OnBookingCancelledListener listener) {
        this.bookings = bookings;
        this.context = context;
        this.cancelListener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking b = bookings.get(position);
        holder.txtId.setText("Mã đơn: #" + b.getId());

        // Tách ngày và giờ
        String[] dateTime = formatDateTime(b.getStartAt());
        holder.txtDate.setText(dateTime[0]); // Ngày
        holder.txtTime.setText(dateTime[1]); // Giờ

        holder.txtPrice.setText(String.format("%.0fđ", b.getTotalPrice()));

        // Set màu status
        String status = b.getStatus().toUpperCase();
        holder.txtStatus.setText(status);
        setStatusColor(holder.txtStatus, status);

        holder.txtNote.setText("Ghi chú: " + (b.getCustomerNote() != null && !b.getCustomerNote().isEmpty()
                ? b.getCustomerNote() : "Không có"));

        // Ẩn tất cả nút trước
        holder.btnPayment.setVisibility(View.GONE);
        holder.btnCancel.setVisibility(View.GONE);
        holder.btnInsufficientAgree.setVisibility(View.GONE);
        holder.btnInsufficientReject.setVisibility(View.GONE);

        // ===== XỬ LÝ TRẠNG THÁI WAITING_FOR_PAYMENT =====
        if (status.equals("WAITING_FOR_PAYMENT")) {
            holder.btnPayment.setVisibility(View.VISIBLE);
            holder.btnPayment.setOnClickListener(v -> processPayment(b.getId()));
            return; // Dừng lại, không xử lý các trường hợp khác
        }

        // Nếu đã CANCELLED hoặc COMPLETED hoặc FULLY_ACCEPTED thì ẩn tất cả nút
        if (status.equals("CANCELLED") || status.equals("COMPLETED") || status.equals("FULLY_ACCEPTED")) {
            return;
        }

        // Kiểm tra thời gian còn lại đến startAt
        long hoursRemaining = getHoursUntilStart(b.getStartAt());

        Log.d(TAG, "Booking #" + b.getId() + " - Status: " + status + " - Hours remaining: " + hoursRemaining);

        if (hoursRemaining >= 0 && hoursRemaining <= 6) {
            // Trước 6 tiếng: Hiện 2 nút Đồng ý/Từ chối thiếu người
            holder.btnInsufficientAgree.setVisibility(View.VISIBLE);
            holder.btnInsufficientReject.setVisibility(View.VISIBLE);

            holder.btnInsufficientAgree.setOnClickListener(v ->
                    showConfirmInsufficientDialog(b.getId(), true)
            );

            holder.btnInsufficientReject.setOnClickListener(v ->
                    showConfirmInsufficientDialog(b.getId(), false)
            );
        } else if (hoursRemaining > 6) {
            // Sau 6 tiếng trở lên: Chỉ hiện nút Hủy thông thường
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> showCancelDialog(b.getId()));
        }

        // 👇 Thêm click listener để hiển thị mã QR
        holder.itemView.setOnClickListener(v -> fetchQrCode(b.getId()));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    // =====================================
    // 🔹 HÀM XỬ LÝ THANH TOÁN
    // =====================================
    private void processPayment(int bookingId) {
        // API này dùng POST nhưng với query parameters, không phải JSON body
        String callbackUrl = "ftask://booking-payment/callback";
        String url = "https://ftask.anhtudev.works/payments/pay-for-booking?bookingId="
                + bookingId + "&callbackUrl=" + Uri.encode(callbackUrl);

        Log.d(TAG, "========================================");
        Log.d(TAG, "POST Payment URL: " + url);
        Log.d(TAG, "========================================");

        RequestQueue queue = Volley.newRequestQueue(context);

        // Sử dụng StringRequest thay vì JsonObjectRequest vì không có body
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d(TAG, "✓ Payment Response: " + response);

                        JSONObject jsonResponse = new JSONObject(response);
                        JSONObject result = jsonResponse.getJSONObject("result");
                        String paymentUrl = result.getString("paymentUrl");

                        // Mở trình duyệt với URL thanh toán VNPay
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                        context.startActivity(browserIntent);

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing payment response", e);
                        Toast.makeText(context, "Lỗi đọc dữ liệu thanh toán", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "========================================");
                    Log.e(TAG, "✗ Payment ERROR");

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Log.e(TAG, "Status Code: " + statusCode);

                        if (error.networkResponse.data != null) {
                            String errBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Error Body: " + errBody);
                            Toast.makeText(context, "Lỗi thanh toán: " + errBody, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Network Error: " + error.getMessage());
                        Toast.makeText(context, "Lỗi kết nối server!", Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "========================================");
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                String token = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .getString("accessToken", null);

                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                } else {
                    Log.e(TAG, "⚠ WARNING: No token found!");
                }

                return headers;
            }
        };

        queue.add(request);
    }


    /**
     * Tách ngày và giờ từ startAt
     * @param startAt Format: 2025-11-09T23:58:00
     * @return Array [ngày, giờ]
     */
    private String[] formatDateTime(String startAt) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            Date date = inputFormat.parse(startAt);

            if (date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                return new String[]{dateFormat.format(date), timeFormat.format(date)};
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + startAt, e);
        }

        return new String[]{"--/--/----", "--:--"};
    }

    /**
     * Set màu cho status badge
     */
    private void setStatusColor(TextView statusView, String status) {
        int color;
        switch (status) {
            case "PENDING":
                color = 0xFF2196F3; // Blue
                break;
            case "CONFIRMED":
                color = 0xFF4CAF50; // Green
                break;
            case "CANCELLED":
                color = 0xFFF44336; // Red
                break;
            case "COMPLETED":
                color = 0xFF9E9E9E; // Gray
                break;
            case "IN_PROGRESS":
                color = 0xFFFF9800; // Orange
                break;
            case "WAITING_FOR_PAYMENT":
                color = 0xFFFF9800; // Orange
                break;
            default:
                color = 0xFF757575; // Dark Gray
                break;
        }
        statusView.setBackgroundColor(color);
    }

    /**
     * Tính số giờ còn lại từ hiện tại đến thời gian startAt
     * @param startAt Thời gian bắt đầu (format: 2025-11-09T23:58:00)
     * @return Số giờ còn lại (âm nếu đã qua)
     */
    private long getHoursUntilStart(String startAt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            Date startDate = sdf.parse(startAt);
            Date currentDate = new Date();

            if (startDate == null) {
                Log.e(TAG, "Failed to parse date: " + startAt);
                return -1;
            }

            long diffInMillis = startDate.getTime() - currentDate.getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

            return hours;
        } catch (ParseException e) {
            Log.e(TAG, "Parse error for startAt: " + startAt, e);
            return -1;
        }
    }

    /**
     * Dialog xác nhận trước khi gửi response thiếu người
     */
    private void showConfirmInsufficientDialog(int bookingId, boolean willCancel) {
        String message = willCancel
                ? "Bạn có chắc chắn ĐỒNG Ý hủy booking này do không đủ người?"
                : "Bạn có chắc chắn TỪ CHỐI hủy booking này?";

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận")
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    respondInsufficientPartners(bookingId, willCancel);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Gửi response về API khi không đủ người
     */
    private void respondInsufficientPartners(int bookingId, boolean cancel) {
        String url = "https://ftask.anhtudev.works/bookings/" + bookingId + "/insufficient-partners-response";

        // THAY ĐỔI: Gửi string "true" hoặc "false" thay vì boolean
        JSONObject body = new JSONObject();
        try {
            body.put("cancel", cancel ? "true" : "false");  // Gửi string
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo dữ liệu!", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "========================================");
        Log.d(TAG, "POST: " + url);
        Log.d(TAG, "Body: " + body.toString());
        Log.d(TAG, "========================================");

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d(TAG, "✓ SUCCESS: " + response.toString());
                    String message = cancel
                            ? "Đã đồng ý hủy booking do thiếu người!"
                            : "Đã từ chối hủy booking!";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    if (cancelListener != null) {
                        cancelListener.onBookingCancelled();
                    }
                },
                error -> {
                    Log.e(TAG, "========================================");
                    Log.e(TAG, "✗ ERROR OCCURRED");

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Log.e(TAG, "Status Code: " + statusCode);

                        if (error.networkResponse.data != null) {
                            String errBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Error Body: " + errBody);

                            String errorMessage = "Lỗi không xác định";
                            try {
                                JSONObject errorJson = new JSONObject(errBody);
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.getString("message");
                                }
                            } catch (JSONException e) {
                                errorMessage = errBody;
                            }

                            if (statusCode == 400) {
                                errorMessage = "Không thể thực hiện: " + errorMessage +
                                        "\n\nCó thể booking này chưa được thông báo thiếu người từ hệ thống.";
                            }

                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Network Error: " + error.getMessage());
                        Toast.makeText(context, "Lỗi kết nối server!", Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "========================================");
                }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");

                String token = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .getString("accessToken", null);

                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                } else {
                    Log.e(TAG, "⚠ WARNING: No token found!");
                }

                return headers;
            }
        };

        queue.add(request);
    }

    /**
     * Dialog hủy booking thông thường (với lý do)
     */
    // =====================================
    // 🔹 HÀM LẤY QR CODE TỪ API
    // =====================================
    private void fetchQrCode(int bookingId) {
        String url = "https://ftask.anhtudev.works/bookings/" + bookingId + "/qr-code";

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject result = response.getJSONObject("result");
                        String qrToken = result.getString("qrToken");
                        showQrDialog(qrToken); // hiển thị mã QR
                    } catch (JSONException e) {
                        Toast.makeText(context, "Lỗi đọc dữ liệu QR", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errBody = new String(error.networkResponse.data);
                        Toast.makeText(context, "Lỗi: " + errBody, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Không thể lấy QR Code!", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String token = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .getString("accessToken", null);
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        queue.add(request);
    }

    private void showQrDialog(String qrToken) {
        try {
            // Tạo ảnh QR code
            QRCodeWriter writer = new QRCodeWriter();
            int size = 600;
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            var bitMatrix = writer.encode(qrToken, BarcodeFormat.QR_CODE, size, size);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            // Tạo layout dọc để chứa TextView và ImageView
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 60, 40, 40);
            layout.setGravity(Gravity.CENTER_HORIZONTAL);

            // Tạo TextView mô tả
            TextView textView = new TextView(context);
            textView.setText("Đưa mã QR này cho nhân viên của chúng tôi");
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 0, 0, 30);

            // Tạo ImageView hiển thị mã QR
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(bitmap);
            imageView.setAdjustViewBounds(true);

            // Thêm TextView + ImageView vào layout
            layout.addView(textView);
            layout.addView(imageView);

            // Hiển thị dialog
            new AlertDialog.Builder(context)
                    .setTitle("Mã QR đặt chỗ")
                    .setView(layout)
                    .setPositiveButton("Đóng", null)
                    .show();

        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo mã QR!", Toast.LENGTH_SHORT).show();
        }
    }


    // =====================================
    // 🔹 CÁC HÀM HỦY ĐƠN (CŨ)
    // =====================================
    private void showCancelDialog(int bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Hủy đơn đặt");

        final EditText input = new EditText(context);
        input.setHint("Nhập lý do hủy đơn...");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập lý do hủy!", Toast.LENGTH_SHORT).show();
            } else {
                cancelBooking(bookingId, reason);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Hủy booking với lý do
     */
    private void cancelBooking(int bookingId, String reason) {
        String url = "https://ftask.anhtudev.works/bookings/" + bookingId;

        JSONObject body = new JSONObject();
        try {
            body.put("reason", reason);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi tạo dữ liệu!", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Toast.makeText(context, "Hủy đơn thành công!", Toast.LENGTH_SHORT).show();
                    if (cancelListener != null) {
                        cancelListener.onBookingCancelled();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errBody = new String(error.networkResponse.data);
                        Toast.makeText(context, "Lỗi: " + errBody, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, "Lỗi kết nối server!", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                String token = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .getString("accessToken", null);
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };
        queue.add(request);
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtDate, txtTime, txtPrice, txtStatus, txtNote;
        Button btnPayment, btnCancel, btnInsufficientAgree, btnInsufficientReject;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtBookingId);
            txtDate = itemView.findViewById(R.id.txtBookingDate);
            txtTime = itemView.findViewById(R.id.txtBookingTime);
            txtPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtStatus = itemView.findViewById(R.id.txtBookingStatus);
            txtNote = itemView.findViewById(R.id.txtBookingNote);
            btnPayment = itemView.findViewById(R.id.btnPayment);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
            btnInsufficientAgree = itemView.findViewById(R.id.btnInsufficientAgree);
            btnInsufficientReject = itemView.findViewById(R.id.btnInsufficientReject);
        }
    }
}