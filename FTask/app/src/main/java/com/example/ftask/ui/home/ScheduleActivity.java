package com.example.ftask.ui.home;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ftask.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private LinearLayout layoutDays, layoutTimePicker;
    private View selectedDayView;
    private TextView txtHour, txtMinute, txtPrice;
    private EditText edtNote;
    private Switch switchRepeat;
    private int selectedHour = 14;
    private int selectedMinute = 0;
    private int totalPrice = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        layoutDays = findViewById(R.id.layoutDays);
        layoutTimePicker = findViewById(R.id.layoutTimePicker);
        txtHour = findViewById(R.id.txtHour);
        txtMinute = findViewById(R.id.txtMinute);
        txtPrice = findViewById(R.id.txtPrice);
        edtNote = findViewById(R.id.edtNote);
        switchRepeat = findViewById(R.id.switchRepeat);

        totalPrice = getIntent().getIntExtra("TOTAL_PRICE", 0);
        if (totalPrice > 0) {
            txtPrice.setText(String.format(Locale.getDefault(), "%,d VND", totalPrice));
        }

        generateNext7Days();
        setupTimePicker();

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(v -> goToConfirmScreen());
    }

    private void generateNext7Days() {
        layoutDays.removeAllViews();
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
        SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            String dayOfWeek = dayOfWeekFormat.format(calendar.getTime());
            String dayOfMonth = dayOfMonthFormat.format(calendar.getTime());

            LinearLayout dayItem = createDayItem(dayOfWeek, dayOfMonth);

            if (i == 0) selectDay(dayItem);

            layoutDays.addView(dayItem);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private LinearLayout createDayItem(String dayOfWeek, String dayOfMonth) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(8, 12, 8, 12);
        container.setBackgroundResource(R.drawable.bg_day_unselected);

        // Giới hạn kích thước đồng đều để không bị tràn hoặc xuống dòng
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(6, 0, 6, 0);
        container.setLayoutParams(params);

        TextView txtDayOfWeek = new TextView(this);
        txtDayOfWeek.setText(dayOfWeek.toUpperCase());
        txtDayOfWeek.setTextColor(getColor(R.color.black));
        txtDayOfWeek.setTextSize(13);
        txtDayOfWeek.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtDayOfWeek.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtDayOfMonth = new TextView(this);
        txtDayOfMonth.setText(dayOfMonth);
        txtDayOfMonth.setTextColor(getColor(R.color.black));
        txtDayOfMonth.setTextSize(15);
        txtDayOfMonth.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        LinearLayout.LayoutParams monthParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        monthParams.setMargins(0, 2, 0, 0);
        txtDayOfMonth.setLayoutParams(monthParams);

        container.addView(txtDayOfWeek);
        container.addView(txtDayOfMonth);

        container.setOnClickListener(v -> selectDay(container));
        return container;
    }


    private void selectDay(View dayView) {
        if (selectedDayView != null) {
            selectedDayView.setBackgroundResource(R.drawable.bg_day_unselected);
        }

        selectedDayView = dayView;
        selectedDayView.setBackgroundResource(R.drawable.bg_day_selected);
    }

    private void setupTimePicker() {
        layoutTimePicker.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    ScheduleActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (TimePicker view, int hourOfDay, int minute) -> {
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        updateTimeDisplay();
                    },
                    selectedHour,
                    selectedMinute,
                    true
            );
            dialog.setTitle("Chọn giờ làm");
            dialog.show();
        });
    }

    private void updateTimeDisplay() {
        txtHour.setText(String.format(Locale.getDefault(), "%02d", selectedHour));
        txtMinute.setText(String.format(Locale.getDefault(), "%02d", selectedMinute));
    }

    private void goToConfirmScreen() {
        Intent intent = new Intent(this, ConfirmOrderActivity.class);
        intent.putExtra("TOTAL_PRICE", totalPrice);
        intent.putExtra("SELECTED_HOUR", selectedHour);
        intent.putExtra("SELECTED_MINUTE", selectedMinute);

        // Lấy ngày được chọn
        if (selectedDayView != null && selectedDayView instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) selectedDayView;
            TextView dayOfWeek = (TextView) layout.getChildAt(0);
            TextView dayOfMonth = (TextView) layout.getChildAt(1);

            String fullDate = dayOfWeek.getText().toString() + ", " + dayOfMonth.getText().toString();
            intent.putExtra("SELECTED_DATE", fullDate);
        }


        // Lặp lại hàng tuần
        intent.putExtra("IS_REPEAT", switchRepeat.isChecked());

        // Ghi chú
        intent.putExtra("NOTE", edtNote.getText().toString().trim());

        startActivity(intent);
    }
}
