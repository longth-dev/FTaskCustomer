package com.example.ftask.ui.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ftask.R;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings;

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
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
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView txtId, txtDate, txtPrice, txtStatus, txtNote;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            txtId = itemView.findViewById(R.id.txtBookingId);
            txtDate = itemView.findViewById(R.id.txtBookingDate);
            txtPrice = itemView.findViewById(R.id.txtBookingPrice);
            txtStatus = itemView.findViewById(R.id.txtBookingStatus);
            txtNote = itemView.findViewById(R.id.txtBookingNote);
        }
    }
}