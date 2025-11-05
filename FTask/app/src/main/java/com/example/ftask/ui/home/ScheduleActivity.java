package com.example.ftask.ui.home;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

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
    private int selectedHour = 14, selectedMinute = 0, totalPrice = 0;

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

        totalPrice = getIntent().getIntExtra("TOTAL_PRICE",0);
        if(totalPrice>0){
            txtPrice.setText(String.format(Locale.getDefault(), "%,d VND", totalPrice));
        }

        generateNext7Days();
        setupTimePicker();

        findViewById(R.id.btnBack).setOnClickListener(v-> finish());
        findViewById(R.id.btnNext).setOnClickListener(v-> goToConfirmScreen());
    }

    private void generateNext7Days(){
        layoutDays.removeAllViews();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", new Locale("vi","VN"));
        SimpleDateFormat monthFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for(int i=0;i<7;i++){
            LinearLayout dayItem = createDayItem(dayFormat.format(calendar.getTime()), monthFormat.format(calendar.getTime()));
            if(i==0) selectDay(dayItem);
            layoutDays.addView(dayItem);
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
    }

    private LinearLayout createDayItem(String dayOfWeek, String dayOfMonth){
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(8,12,8,12);
        container.setBackgroundResource(R.drawable.bg_day_unselected);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1f);
        params.setMargins(6,0,6,0);
        container.setLayoutParams(params);

        TextView txtWeek = new TextView(this);
        txtWeek.setText(dayOfWeek.toUpperCase());
        txtWeek.setTextColor(getColor(R.color.black));
        txtWeek.setTextSize(13);
        txtWeek.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        TextView txtMonth = new TextView(this);
        txtMonth.setText(dayOfMonth);
        txtMonth.setTextColor(getColor(R.color.black));
        txtMonth.setTextSize(15);
        txtMonth.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        container.addView(txtWeek);
        container.addView(txtMonth);

        container.setOnClickListener(v-> selectDay(container));
        return container;
    }

    private void selectDay(View dayView){
        if(selectedDayView!=null) selectedDayView.setBackgroundResource(R.drawable.bg_day_unselected);
        selectedDayView = dayView;
        selectedDayView.setBackgroundResource(R.drawable.bg_day_selected);
    }

    private void setupTimePicker(){
        layoutTimePicker.setOnClickListener(v->{
            TimePickerDialog dialog = new TimePickerDialog(
                    ScheduleActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (TimePicker view,int hourOfDay,int minute)->{
                        selectedHour=hourOfDay;
                        selectedMinute=minute;
                        txtHour.setText(String.format(Locale.getDefault(),"%02d",selectedHour));
                        txtMinute.setText(String.format(Locale.getDefault(),"%02d",selectedMinute));
                    },selectedHour,selectedMinute,true);
            dialog.setTitle("Chọn giờ làm");
            dialog.show();
        });
    }

    private void goToConfirmScreen(){
        int variantId = getIntent().getIntExtra("VARIANT_ID",-1);
        int addressId = getIntent().getIntExtra("ADDRESS_ID",0);
        if(variantId==-1 || addressId==0){
            Toast.makeText(this,"Thiếu dữ liệu variantId hoặc addressId!",Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ConfirmOrderActivity.class);
        intent.putExtra("TOTAL_PRICE",totalPrice);
        intent.putExtra("VARIANT_ID",variantId);
        intent.putExtra("ADDRESS_ID",addressId);
        intent.putExtra("SHORT_ADDRESS",getIntent().getStringExtra("SHORT_ADDRESS"));
        intent.putExtra("FULL_ADDRESS",getIntent().getStringExtra("FULL_ADDRESS"));
        intent.putExtra("NOTE",edtNote.getText().toString().trim());
        intent.putExtra("SELECTED_HOUR",selectedHour);
        intent.putExtra("SELECTED_MINUTE",selectedMinute);

        // Gửi ngày theo chuẩn yyyy-MM-dd
        if(selectedDayView!=null && selectedDayView instanceof LinearLayout){
            Calendar cal = Calendar.getInstance();
            int index = layoutDays.indexOfChild(selectedDayView);
            cal.add(Calendar.DAY_OF_MONTH,index);
            String fullDate = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(cal.getTime());
            intent.putExtra("SELECTED_DATE",fullDate);
        }

        startActivity(intent);
    }
}
