package com.example.ftask.ui.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.ftask.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private Marker currentMarker;
    private LatLng selectedLatLng;
    private Button btnConfirm;
    private final OkHttpClient client = new OkHttpClient();
    private static final String TAG = "MapPicker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        btnConfirm = findViewById(R.id.btnConfirm);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng == null) {
                Toast.makeText(this, "Vui lòng chọn vị trí trên bản đồ", Toast.LENGTH_SHORT).show();
                return;
            }

            Address addr = getAddressFromLatLng(selectedLatLng.latitude, selectedLatLng.longitude);
            if (addr == null) {
                Toast.makeText(this, "Không thể lấy địa chỉ từ vị trí này", Toast.LENGTH_SHORT).show();
                return;
            }

            postAddressToServer(addr, selectedLatLng.latitude, selectedLatLng.longitude);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;

        LatLng hcm = new LatLng(10.762622, 106.660172);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hcm, 13));

        gMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            if (currentMarker != null) currentMarker.remove();

            currentMarker = gMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Vị trí đã chọn"));
        });
    }

    private Address getAddressFromLatLng(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Log.d(TAG, "Address found: " + addresses.get(0).getAddressLine(0));
                return addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void postAddressToServer(Address address, double lat, double lng) {
        try {
            String token = getToken();
            if(token.isEmpty()){
                Toast.makeText(this, "Chưa đăng nhập, token rỗng", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject json = new JSONObject();
            json.put("addressLine", address.getAddressLine(0) != null ? address.getAddressLine(0) : "Unknown");
            json.put("district", address.getSubAdminArea() != null ? address.getSubAdminArea() : "Unknown");
            json.put("city", address.getLocality() != null ? address.getLocality() : "Unknown");
            json.put("postalCode", address.getPostalCode() != null ? address.getPostalCode() : "000000");
            json.put("latitude", lat);
            json.put("longitude", lng);
            json.put("isDefault", true);

            Log.d(TAG, "Sending JSON: " + json.toString());
            Log.d(TAG, "Authorization: Bearer " + token);

            RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("https://ftask.anhtudev.works/addresses")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() ->
                            Toast.makeText(MapPickerActivity.this, "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "Response code: " + response.code() + ", body: " + responseBody);

                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(MapPickerActivity.this, "Thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(MapPickerActivity.this, "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo dữ liệu địa chỉ", Toast.LENGTH_SHORT).show();
        }
    }

    private String getToken() {
        String token = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                .getString("accessToken", "");
        Log.d(TAG, "Token: " + token);
        return token;
    }
}
