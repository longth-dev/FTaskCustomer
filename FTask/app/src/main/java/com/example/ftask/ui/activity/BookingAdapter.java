package com.example.ftask.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

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
        holder.txtDate.setText("Thời gian: " + b.getStartAt());
        holder.txtPrice.setText("Tổng: " + b.getTotalPrice() + "đ");
        holder.txtStatus.setText("Trạng thái: " + b.getStatus());
        holder.txtNote.setText("Ghi chú: " + (b.getCustomerNote() != null ? b.getCustomerNote() : "Không có"));

        // Ẩn nút Cancel nếu trạng thái là CANCELLED hoặc COMPLETED
        String status = b.getStatus().toUpperCase();
        if (status.equals("CANCELLED") || status.equals("COMPLETED")) {
            holder.btnCancel.setVisibility(View.GONE);
        } else {
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnCancel.setOnClickListener(v -> showCancelDialog(b.getId()));
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    private void showCancelDialog(int bookingId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Hủy đơn đặt");

        // Tạo EditText để nhập lý do
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
        TextView txtId, txtDate, txtPrice, txtStatus, txtNote;
        Button btnCancel;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtBookingId);
            txtDate = itemView.findViewById(R.id.txtBookingDate);
            txtPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtStatus = itemView.findViewById(R.id.txtBookingStatus);
            txtNote = itemView.findViewById(R.id.txtBookingNote);
            btnCancel = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}