package com.example.ftask.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.MainActivity;
import com.example.ftask.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ConfirmOrderActivity extends AppCompatActivity {

    private TextView txtJobDate, txtJobTime, txtJobNote, txtTotalPrice;
    private TextView txtShortAddress, txtFullAddress;
    private LinearLayout layoutCash, layoutCard;
    private TextView btnPostJob;

    private LinearLayout selectedPayment = null;
    private int addressId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        txtJobDate = findViewById(R.id.txtJobDate);
        txtJobTime = findViewById(R.id.txtJobTime);
        txtJobNote = findViewById(R.id.txtJobNote);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnPostJob = findViewById(R.id.btnPostJob);
        layoutCash = findViewById(R.id.layoutCash);
        layoutCard = findViewById(R.id.layoutCard);
        txtShortAddress = findViewById(R.id.txtShortAddress);
        txtFullAddress = findViewById(R.id.txtFullAddress);

        int totalPrice = getIntent().getIntExtra("TOTAL_PRICE",0);
        int variantId = getIntent().getIntExtra("VARIANT_ID",0);
        String selectedDate = getIntent().getStringExtra("SELECTED_DATE");
        int hour = getIntent().getIntExtra("SELECTED_HOUR",0);
        int minute = getIntent().getIntExtra("SELECTED_MINUTE",0);
        String note = getIntent().getStringExtra("NOTE");
        txtShortAddress.setText(getIntent().getStringExtra("SHORT_ADDRESS"));
        txtFullAddress.setText(getIntent().getStringExtra("FULL_ADDRESS"));
        addressId = getIntent().getIntExtra("ADDRESS_ID",0);

        txtTotalPrice.setText(String.format(Locale.getDefault(), "%,d VND", totalPrice));
        txtJobDate.setText(selectedDate!=null?selectedDate:"Chưa chọn");
        txtJobTime.setText(String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
        txtJobNote.setText(note!=null && !note.isEmpty()?note:"Không có");

        setupPaymentSelection();

        btnPostJob.setOnClickListener(v->{
            if(selectedPayment==null){
                Toast.makeText(this,"Vui lòng chọn phương thức thanh toán!",Toast.LENGTH_SHORT).show();
                return;
            }
            if(variantId==0 || addressId==0){
                Toast.makeText(this,"Thiếu dữ liệu variantId hoặc addressId!",Toast.LENGTH_SHORT).show();
                return;
            }
            postBooking(variantId, selectedDate, hour, minute, note);
        });
    }

    private void setupPaymentSelection(){
        layoutCash.setOnClickListener(v-> selectPayment(layoutCash));
        layoutCard.setOnClickListener(v-> selectPayment(layoutCard));
    }

    private void selectPayment(LinearLayout selected){
        layoutCash.setBackgroundResource(R.drawable.bg_option_unselected);
        layoutCard.setBackgroundResource(R.drawable.bg_option_unselected);
        selected.setBackgroundResource(R.drawable.bg_option_selected);
        selectedPayment = selected;
    }

    private void postBooking(int variantId, String date, int hour, int minute, String note){
        String startAt = convertToISO8601UTC(date,hour,minute);

        JSONObject body = new JSONObject();
        try{
            body.put("variantId",variantId);
            body.put("addressId",addressId);
            body.put("startAt",startAt);
            body.put("customerNote",note!=null?note:"");
            String method = selectedPayment==layoutCash?"CASH":"WALLET";
            body.put("method",method);
        }catch(JSONException e){
            e.printStackTrace();
            Toast.makeText(this,"Lỗi tạo dữ liệu đặt lịch",Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("BOOKING_BODY",body.toString());

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                "https://ftask.anhtudev.works/bookings", body,
                response -> {
                    Log.d("BOOKING_RESPONSE",response.toString());
                    Toast.makeText(this,"Đặt lịch thành công!",Toast.LENGTH_SHORT).show();
                    // Trở về HomeActivity
                    Intent intent = new Intent(ConfirmOrderActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.putExtra("OPEN_HOME_FRAGMENT", true);
                    startActivity(intent);
                    finish();


                },
                error -> {
                    if(error.networkResponse!=null && error.networkResponse.data!=null){
                        String errBody = new String(error.networkResponse.data);
                        Log.e("BOOKING_ERROR","Status Code: "+error.networkResponse.statusCode);
                        Log.e("BOOKING_ERROR","Response: "+errBody);
                        Toast.makeText(this,"Lỗi "+error.networkResponse.statusCode+": "+errBody,Toast.LENGTH_LONG).show();
                    }else{
                        Log.e("BOOKING_ERROR","Unknown error",error);
                        Toast.makeText(this,"Lỗi kết nối server!",Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError{
                Map<String,String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");
                String token = getSharedPreferences("MyPrefs",MODE_PRIVATE).getString("accessToken",null);
                if(token!=null) headers.put("Authorization","Bearer "+token);
                return headers;
            }
        };
        queue.add(request);
    }

    private String convertToISO8601UTC(String dateStr,int hour,int minute){
        if(dateStr==null || dateStr.isEmpty()){
            dateStr = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());
        }
        SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault());
        localFormat.setTimeZone(TimeZone.getDefault());

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try{
            Date date = localFormat.parse(dateStr+" "+String.format(Locale.getDefault(),"%02d:%02d",hour,minute));
            return isoFormat.format(date);
        }catch(Exception e){
            e.printStackTrace();
            return dateStr+"T"+String.format(Locale.getDefault(),"%02d:%02d:00.000Z",hour,minute);
        }
    }
}
