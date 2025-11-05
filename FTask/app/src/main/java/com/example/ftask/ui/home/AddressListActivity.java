package com.example.ftask.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddressListActivity extends AppCompatActivity {

    private RecyclerView recyclerAddresses;
    private Button btnAddAddress;
    private ImageView btnBack;

    private static final int REQUEST_ADD_ADDRESS = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        recyclerAddresses = findViewById(R.id.recyclerAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnBack = findViewById(R.id.btnBack);

        recyclerAddresses.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        btnAddAddress.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapPickerActivity.class);
            startActivityForResult(intent, REQUEST_ADD_ADDRESS);
        });

        loadAddresses();
    }

    private void loadAddresses() {
        String url = "https://ftask.anhtudev.works/customer/addresses";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    JSONArray addresses = response.optJSONArray("result");
                    if (addresses != null && addresses.length() > 0) {
                        setupRecycler(addresses);
                    } else {
                        Toast.makeText(this, "Danh sách địa chỉ trống", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Không thể tải danh sách địa chỉ", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String token = prefs.getString("accessToken", null);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        queue.add(request);
    }

    private void setupRecycler(JSONArray addresses) {
        AddressAdapter adapter = new AddressAdapter(addresses, address -> {
            Intent result = new Intent();
            result.putExtra("addressLine", address.optString("addressLine"));
            result.putExtra("city", address.optString("city"));
            result.putExtra("district", address.optString("district"));
            result.putExtra("province", address.optString("province"));
            result.putExtra("addressId", address.optInt("id", 0));
            setResult(Activity.RESULT_OK, result);
            finish();
        });
        recyclerAddresses.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ADDRESS && resultCode == RESULT_OK) {
            // Khi thêm địa chỉ mới từ MapPickerActivity xong, reload danh sách
            loadAddresses();
        }
    }
}
