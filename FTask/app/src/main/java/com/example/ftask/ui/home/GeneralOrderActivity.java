package com.example.ftask.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Hiển thị 6 gói dịch vụ dạng card cố định, nội dung được fill động từ API service-variants.
 * UI XML giữ nguyên; chỉ thay text bên trong các card và hành vi chọn.
 */
public class GeneralOrderActivity extends AppCompatActivity {

    // Các card cố định trên layout
    private MaterialCardView card60m, card80m, card100m, card150m, card200m, card400m, cardJobDetails;

    // Text tổng tiền + nút
    private TextView txtTotalPrice, btnNext;
    private TextView txtShortAddress, txtFullAddress;

    // Trạng thái lựa chọn
    private int selectedVariantId = -1;
    private String selectedVariantName = null;
    private int selectedHours = 0;
    private int selectedWorkers = 0;
    private int selectedPrice = 0;

    // Giữ lại selectedArea (ScheduleActivity có thể đang dùng) – map tạm theo variant index nếu cần
    private int selectedArea = 0;

    // Mảng các card để duyệt
    private MaterialCardView[] cards;

    // Dữ liệu variant đã fetch
    private JSONArray variantArray; // lưu lại để dùng khi click

    private static final String API_URL = "https://ftask.anhtudev.works/service-variants?page=1&size=6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_order);

        mapViews();
        setupBackButton();
        setupJobDetailsClick();
        setupNextButton();

        // Địa chỉ mặc định
        txtShortAddress.setText("Vinhomes Grand Park Long Binh");
        txtFullAddress.setText("Vinhomes Grand Park, Long Binh,...");

        fetchVariants();
    }

    private void mapViews() {
        card60m = findViewById(R.id.card60m);
        card80m = findViewById(R.id.card80m);
        card100m = findViewById(R.id.card100m);
        card150m = findViewById(R.id.card150m);
        card200m = findViewById(R.id.card200m);
        card400m = findViewById(R.id.card400m);
        cardJobDetails = findViewById(R.id.cardJobDetails);

        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnNext = findViewById(R.id.btnNext);
        txtShortAddress = findViewById(R.id.txtShortAddress);
        txtFullAddress = findViewById(R.id.txtFullAddress);

        cards = new MaterialCardView[]{
                card60m, card80m, card100m, card150m, card200m, card400m
        };
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
    }

    private void setupJobDetailsClick() {
        if (cardJobDetails != null) {
            cardJobDetails.setOnClickListener(v ->
                    Toast.makeText(this, "Chi tiết công việc", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupNextButton() {
        btnNext.setOnClickListener(v -> {
            if (selectedVariantId == -1) {
                Toast.makeText(this, "Vui lòng chọn gói dịch vụ!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(GeneralOrderActivity.this, ScheduleActivity.class);
            intent.putExtra("SERVICE_TYPE", "GENERAL_CLEANING");
            intent.putExtra("AREA", selectedArea);        // vẫn gửi cho tương thích
            intent.putExtra("WORKERS", selectedWorkers);
            intent.putExtra("HOURS", selectedHours);
            intent.putExtra("TOTAL_PRICE", selectedPrice);
            intent.putExtra("VARIANT_ID", selectedVariantId);
            intent.putExtra("VARIANT_NAME", selectedVariantName);
            startActivity(intent);
        });
    }

    private void fetchVariants() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                API_URL,
                null,
                response -> {
                    try {
                        JSONObject resultObj = response.optJSONObject("result");
                        if (resultObj != null && resultObj.has("content")) {
                            variantArray = resultObj.getJSONArray("content");
                            bindVariantsToCards(variantArray);
                        } else {
                            Toast.makeText(this, "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi xử lý dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(request);
    }

    /**
     * Gắn dữ liệu variant vào 6 card có sẵn.
     */
    private void bindVariantsToCards(JSONArray arr) {
        // Ẩn trước các card
        for (MaterialCardView c : cards) {
            if (c != null) c.setVisibility(View.GONE);
        }

        int count = Math.min(arr.length(), cards.length);
        for (int i = 0; i < count; i++) {
            MaterialCardView card = cards[i];
            if (card == null) continue;

            try {
                JSONObject obj = arr.getJSONObject(i);
                int id = obj.optInt("id", -1);
                String name = obj.optString("name", "Gói dịch vụ");
                String desc = obj.optString("description", "");
                int durationHours = obj.optInt("durationHours", 0);
                boolean isMultiPartner = obj.optBoolean("isMultiPartner", false);
                int numberOfPartners = obj.optInt("numberOfPartners", 1);
                int price = (int) Math.round(obj.optDouble("pricePerVariant", 0.0));

                // Lưu đối tượng vào tag để dùng khi click
                card.setTag(obj);

                // Lấy 2 TextView bên trong (theo đúng layout bạn gửi: LinearLayout -> 2 TextView)
                View inner = card.getChildAt(0); // LinearLayout
                if (inner instanceof View) {
                    View titleView = ((View) inner).findViewById(android.R.id.title); // không tồn tại -> fallback thủ công
                }
                // Vì không có id, ta sẽ truy cập theo thứ tự con:
                // card -> LinearLayout (index 0) -> TextView (index 0 & 1)
                if (inner instanceof android.widget.LinearLayout) {
                    android.widget.LinearLayout ll = (android.widget.LinearLayout) inner;
                    TextView tvLine1 = (TextView) ll.getChildAt(0);
                    TextView tvLine2 = (TextView) ll.getChildAt(1);

                    // Dòng 1: dùng name (thay cho “Tối đa 60m²”)
                    tvLine1.setText(name);

                    // Dòng 2: meta
                    tvLine2.setText(String.format(Locale.getDefault(),
                            "%d người / %d giờ", isMultiPartner ? numberOfPartners : 1, durationHours));
                }

                card.setVisibility(View.VISIBLE);

                // Sự kiện chọn
                card.setOnClickListener(v -> {
                    selectCard(card);
                    selectedVariantId = id;
                    selectedVariantName = name;
                    selectedHours = durationHours;
                    selectedWorkers = isMultiPartner ? numberOfPartners : 1;
                    selectedPrice = price;

                    updatePrice();
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void selectCard(MaterialCardView selected) {
        resetCardStyles();
        selected.setStrokeWidth(4);
        selected.setStrokeColor(getResources().getColor(R.color.orange));
        selected.setCardElevation(8f);
    }

    private void resetCardStyles() {
        for (MaterialCardView c : cards) {
            if (c == null) continue;
            c.setStrokeWidth(0);
            c.setCardElevation(2f);
        }
    }

    private void updatePrice() {
        if (selectedPrice <= 0 || selectedHours <= 0) {
            txtTotalPrice.setText("0đ");
        } else {
            txtTotalPrice.setText(String.format(Locale.getDefault(),
                    "%,dđ/%dh", selectedPrice, selectedHours));
        }
    }
}