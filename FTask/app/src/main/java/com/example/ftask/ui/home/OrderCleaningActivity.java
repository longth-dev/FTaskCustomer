package com.example.ftask.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OrderCleaningActivity extends AppCompatActivity {

    private LinearLayout layoutVariantContainer;
    private TextView txtTotalPrice;
    private int selectedVariantPrice = 0;
    private int selectedVariantId = -1;

    private int serviceCatalogId = 1; // mặc định
    private String apiUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cleaning);

        // Lấy serviceCatalogId từ Intent
        serviceCatalogId = getIntent().getIntExtra("SERVICE_ID", 1);
        apiUrl = "https://ftask.anhtudev.works/service-variants?page=1&size=10&serviceCatalogId=" + serviceCatalogId;

        mapViews();
        setupBackButton();
        setupJobDetailsSection();

        fetchServiceVariants(apiUrl);
    }

    // 🔹 Ánh xạ View và xử lý nút "Tiếp theo"
    private void mapViews() {
        layoutVariantContainer = findViewById(R.id.layoutVariantContainer);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        // Đặt địa chỉ mẫu
        TextView txtShortAddress = findViewById(R.id.txtShortAddress);
        TextView txtFullAddress = findViewById(R.id.txtFullAddress);
        if (txtShortAddress != null && txtFullAddress != null) {
            txtShortAddress.setText("Bùi Quang Là phường 12");
            txtFullAddress.setText("54/59 Bùi Quang Là, phường 12, Gò Vấp");
        }

        TextView btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> {
            if (selectedVariantId == -1) {
                Toast.makeText(this, "Vui lòng chọn gói dịch vụ!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(OrderCleaningActivity.this, ScheduleActivity.class);
            intent.putExtra("TOTAL_PRICE", selectedVariantPrice);
            intent.putExtra("VARIANT_ID", selectedVariantId); // gửi variantId
            startActivity(intent);
        });
    }

    // 🔹 Nút quay lại
    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    // 🔹 Phần “Chi tiết công việc”
    private void setupJobDetailsSection() {
        LinearLayout headerJobDetails = findViewById(R.id.headerJobDetails);
        LinearLayout jobDetailContent = findViewById(R.id.jobDetailContent);
        ImageView iconExpand = findViewById(R.id.iconExpand);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        JobDetailPagerAdapter adapter = new JobDetailPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Phòng ngủ");
                    break;
                case 1:
                    tab.setText("Phòng tắm");
                    break;
                case 2:
                    tab.setText("Nhà bếp");
                    break;
                case 3:
                    tab.setText("Phòng khách");
                    break;
            }
        }).attach();

        headerJobDetails.setOnClickListener(v -> {
            if (jobDetailContent.getVisibility() == View.GONE) {
                jobDetailContent.setVisibility(View.VISIBLE);
                iconExpand.setRotation(90);
            } else {
                jobDetailContent.setVisibility(View.GONE);
                iconExpand.setRotation(0);
            }
        });
    }

    // 🔹 Gọi API để lấy danh sách gói dịch vụ
    private void fetchServiceVariants(String apiUrl) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                response -> {
                    try {
                        JSONObject resultObj = response.optJSONObject("result");
                        if (resultObj != null && resultObj.has("content")) {
                            JSONArray content = resultObj.getJSONArray("content");
                            displayVariants(content);
                        } else if (response.has("result")) {
                            JSONArray resultArr = response.getJSONArray("result");
                            displayVariants(resultArr);
                        } else {
                            Toast.makeText(this, "Không có dữ liệu dịch vụ!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi đọc dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    String errMsg = error.getMessage();
                    if (errMsg == null && error.networkResponse != null) {
                        errMsg = "HTTP " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, "Lỗi tải dữ liệu dịch vụ: " + errMsg, Toast.LENGTH_LONG).show();
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

    // 🔹 Hiển thị danh sách gói dịch vụ (tên + mô tả + giá)
    private void displayVariants(JSONArray variants) {
        layoutVariantContainer.removeAllViews();

        for (int i = 0; i < variants.length(); i++) {
            try {
                JSONObject obj = variants.getJSONObject(i);
                int id = obj.optInt("id", -1);
                String name = obj.optString("name", "Không tên");
                String desc = obj.optString("description", "");
                int price = obj.optInt("pricePerVariant", 0);

                View variantView = getLayoutInflater().inflate(R.layout.item_variant_option, layoutVariantContainer, false);

                TextView txtName = variantView.findViewById(R.id.txtVariantName);
                TextView txtDesc = variantView.findViewById(R.id.txtVariantDesc);
                TextView txtPrice = variantView.findViewById(R.id.txtVariantPrice);
                LinearLayout layoutItem = variantView.findViewById(R.id.layoutVariantItem);

                txtName.setText(name);
                txtDesc.setText(desc);
                txtPrice.setText(price + "đ");

                layoutItem.setOnClickListener(v -> {
                    resetVariantBackground();
                    layoutItem.setBackgroundResource(R.drawable.bg_option_selected);
                    selectedVariantId = id;
                    selectedVariantPrice = price;
                    txtTotalPrice.setText(price + "đ");
                });

                layoutVariantContainer.addView(variantView);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // 🔹 Reset nền khi chọn gói khác
    private void resetVariantBackground() {
        for (int i = 0; i < layoutVariantContainer.getChildCount(); i++) {
            View child = layoutVariantContainer.getChildAt(i);
            LinearLayout layoutItem = child.findViewById(R.id.layoutVariantItem);
            layoutItem.setBackgroundResource(R.drawable.bg_option_unselected);
        }
    }
}
