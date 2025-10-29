package com.example.ftask.ui.home;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ftask.R;

public class OrderCleaningActivity extends AppCompatActivity {

    private LinearLayout item2h, item3h, item4h, layoutPremium, itemCooking, itemIroning;
    private Switch switchPremium, switchPet, switchChooseTasker, switchFavorite;
    private TextView txtTotalPrice;

    private int selectedHours = 0;
    private int extraHours = 0;

    private final int price2h = 120000;
    private final int price3h = 180000;
    private final int price4h = 240000;
    private final int premiumPrice = 50000;
    private final int extraHourPrice = 60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_cleaning);

        mapViews();
        setupBackButton();
        setupHourSelection();
        setupPremiumSelection();
        setupAddOnSelection();
        setupOptionsSelection();

        updatePrice();
    }

    private void mapViews() {
        item2h = findViewById(R.id.item2h);
        item3h = findViewById(R.id.item3h);
        item4h = findViewById(R.id.item4h);

        layoutPremium = findViewById(R.id.layoutPremium);
        switchPremium = findViewById(R.id.switchPremium);

        itemCooking = findViewById(R.id.itemCooking);
        itemIroning = findViewById(R.id.itemIroning);

        switchPet = findViewById(R.id.switchPet);
        switchChooseTasker = findViewById(R.id.switchChooseTasker);
        switchFavorite = findViewById(R.id.switchFavorite);

        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        TextView txtShortAddress = findViewById(R.id.txtShortAddress);
        TextView txtFullAddress = findViewById(R.id.txtFullAddress);

        txtShortAddress.setText("Bùi Quang Là phường 12");
        txtFullAddress.setText("54/59 Bùi Quang Là, phường 12, Gò Vấp");
    }

    private void setupBackButton() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupHourSelection() {
        item2h.setOnClickListener(v -> selectHour(2, item2h));
        item3h.setOnClickListener(v -> selectHour(3, item3h));
        item4h.setOnClickListener(v -> selectHour(4, item4h));
    }

    private void selectHour(int hour, LinearLayout selectedView) {
        selectedHours = hour;
        item2h.setBackgroundResource(R.drawable.bg_option_unselected);
        item3h.setBackgroundResource(R.drawable.bg_option_unselected);
        item4h.setBackgroundResource(R.drawable.bg_option_unselected);

        selectedView.setBackgroundResource(R.drawable.bg_option_selected);
        updatePrice();
    }

    private void setupPremiumSelection() {
        layoutPremium.setOnClickListener(v -> {
            switchPremium.toggle();
        });

        switchPremium.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutPremium.setBackgroundResource(
                    isChecked ? R.drawable.bg_option_selected : R.drawable.bg_option_unselected
            );
            updatePrice();
        });
    }

    private void setupAddOnSelection() {
        itemCooking.setOnClickListener(v -> toggleAddOn(itemCooking));
        itemIroning.setOnClickListener(v -> toggleAddOn(itemIroning));
    }

    private void toggleAddOn(LinearLayout item) {
        boolean selected = item.getBackground().getConstantState() ==
                getDrawable(R.drawable.bg_option_selected).getConstantState();

        if (selected) {
            item.setBackgroundResource(R.drawable.bg_option_unselected);
            extraHours -= 1;
        } else {
            item.setBackgroundResource(R.drawable.bg_option_selected);
            extraHours += 1;
        }
        updatePrice();
    }

    private void setupOptionsSelection() {
        switchPet.setOnCheckedChangeListener((buttonView, isChecked) -> updatePrice());
        switchChooseTasker.setOnCheckedChangeListener((buttonView, isChecked) -> updatePrice());
        switchFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> updatePrice());
    }

    private void updatePrice() {
        int total = 0;

        if (selectedHours == 2) total = price2h;
        if (selectedHours == 3) total = price3h;
        if (selectedHours == 4) total = price4h;

        if (switchPremium.isChecked()) total += premiumPrice;
        if (extraHours > 0) total += extraHours * extraHourPrice;

        txtTotalPrice.setText(total + "đ");
    }
}
