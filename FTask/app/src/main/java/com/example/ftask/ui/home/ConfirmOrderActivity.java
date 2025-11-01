package com.example.ftask.ui.home;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ftask.R;

import java.util.Locale;

public class ConfirmOrderActivity extends AppCompatActivity {

    private TextView txtJobDate, txtJobTime, txtRepeat, txtJobNote, txtTotalPrice;
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
        txtRepeat = findViewById(R.id.txtRepeat);
        txtJobNote = findViewById(R.id.txtJobNote);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnPostJob = findViewById(R.id.btnPostJob);
        layoutCash = findViewById(R.id.layoutCash);
        layoutCard = findViewById(R.id.layoutCard);

        // Nhận dữ liệu từ Intent
        int totalPrice = getIntent().getIntExtra("TOTAL_PRICE", 0);
        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        int hour = getIntent().getIntExtra("SELECTED_HOUR", 0);
        int minute = getIntent().getIntExtra("SELECTED_MINUTE", 0);
        boolean isRepeat = getIntent().getBooleanExtra("IS_REPEAT", false);
        String note = getIntent().getStringExtra("NOTE");

        // Hiển thị thông tin
        txtTotalPrice.setText(String.format(Locale.getDefault(), "%,d VND", totalPrice));
        txtJobDate.setText(selectedDate != null ? selectedDate : "Chưa chọn");
        txtJobTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        txtRepeat.setText(isRepeat ? "Có" : "Không");
        txtJobNote.setText(note != null && !note.isEmpty() ? note : "Không có");

        // Hiệu ứng chọn phương thức thanh toán
        setupPaymentSelection();

        // Nút đăng việc
        btnPostJob.setOnClickListener(v -> {
            Toast.makeText(this, "Đã đăng việc thành công!", Toast.LENGTH_SHORT).show();
            finish();
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
}
