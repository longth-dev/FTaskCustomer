package com.example.ftask.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ftask.R;
import com.google.android.material.card.MaterialCardView;

public class GeneralOrderActivity extends AppCompatActivity {

    private MaterialCardView card60m, card80m, card100m, card150m, card200m, card400m;
    private MaterialCardView cardJobDetails;
    private TextView txtTotalPrice, btnNext;
    private TextView txtShortAddress, txtFullAddress;

    private int selectedArea = 0;
    private int selectedWorkers = 0;
    private int selectedHours = 0;
    private int selectedPrice = 0;

    // Giá cho từng gói (VNĐ)
    private final int price60m = 660000;   // 2 người / 3h
    private final int price80m = 880000;   // 2 người / 4h
    private final int price100m = 990000;  // 3 người / 3h
    private final int price150m = 1320000; // 3 người / 4h
    private final int price200m = 1760000; // 4 người / 4h
    private final int price400m = 3520000; // 4 người / 8h

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_order);

        mapViews();
        setupBackButton();
        setupAreaSelection();
        setupJobDetailsClick();
        setupNextButton();

        // Set địa chỉ mặc định
        txtShortAddress.setText("Vinhomes Grand Park Long Binh");
        txtFullAddress.setText("Vinhomes Grand Park, Long Binh,...");
    }

    private void mapViews() {
        // Cards
        card60m = findViewById(R.id.card60m);
        card80m = findViewById(R.id.card80m);
        card100m = findViewById(R.id.card100m);
        card150m = findViewById(R.id.card150m);
        card200m = findViewById(R.id.card200m);
        card400m = findViewById(R.id.card400m);

        cardJobDetails = findViewById(R.id.cardJobDetails);

        // Text views
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnNext = findViewById(R.id.btnNext);

        txtShortAddress = findViewById(R.id.txtShortAddress);
        txtFullAddress = findViewById(R.id.txtFullAddress);
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupAreaSelection() {
        card60m.setOnClickListener(v -> selectArea(60, 2, 3, price60m, card60m));
        card80m.setOnClickListener(v -> selectArea(80, 2, 4, price80m, card80m));
        card100m.setOnClickListener(v -> selectArea(100, 3, 3, price100m, card100m));
        card150m.setOnClickListener(v -> selectArea(150, 3, 4, price150m, card150m));
        card200m.setOnClickListener(v -> selectArea(200, 4, 4, price200m, card200m));
        card400m.setOnClickListener(v -> selectArea(400, 4, 8, price400m, card400m));
    }

    private void selectArea(int area, int workers, int hours, int price, MaterialCardView selectedCard) {
        selectedArea = area;
        selectedWorkers = workers;
        selectedHours = hours;
        selectedPrice = price;

        // Reset tất cả các card về trạng thái unselected
        resetAllCards();

        // Set card được chọn với border màu cam
        selectedCard.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        selectedCard.setCardElevation(8f);
        selectedCard.setStrokeWidth(4);
        selectedCard.setStrokeColor(getResources().getColor(R.color.orange));

        // Cập nhật giá
        updatePrice();
    }

    private void resetAllCards() {
        MaterialCardView[] cards = {card60m, card80m, card100m, card150m, card200m, card400m};
        for (MaterialCardView card : cards) {
            card.setCardBackgroundColor(getResources().getColor(android.R.color.white));
            card.setCardElevation(2f);
            card.setStrokeWidth(0);
        }
    }

    private void updatePrice() {
        String priceText = String.format("%,d VNĐ/%dh", selectedPrice, selectedHours);
        txtTotalPrice.setText(priceText);
    }

    private void setupJobDetailsClick() {
        cardJobDetails.setOnClickListener(v -> {
            // TODO: Mở dialog hoặc activity hiển thị chi tiết công việc
            Toast.makeText(this, "Chi tiết công việc", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            if (selectedArea == 0) {
                Toast.makeText(this,
                        "Vui lòng chọn diện tích cần dọn dẹp",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(GeneralOrderActivity.this, ScheduleActivity.class);
            intent.putExtra("SERVICE_TYPE", "GENERAL_CLEANING");
            intent.putExtra("AREA", selectedArea);
            intent.putExtra("WORKERS", selectedWorkers);
            intent.putExtra("HOURS", selectedHours);
            intent.putExtra("TOTAL_PRICE", selectedPrice);
            startActivity(intent);
        });
    }
}