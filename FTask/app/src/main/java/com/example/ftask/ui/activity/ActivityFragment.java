package com.example.ftask.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ftask.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityFragment extends Fragment {

    private RecyclerView recyclerBookings;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        recyclerBookings = view.findViewById(R.id.recyclerBookings);
        recyclerBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookingAdapter(bookingList, getContext(), () -> {
            fetchBookings();
        });
        recyclerBookings.setAdapter(adapter);

        fetchBookings();
        return view;
    }

    private void fetchBookings() {
        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Token không tồn tại, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();

        Request userRequest = new Request.Builder()
                .url("https://ftask.anhtudev.works/users/me")
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(userRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi lấy thông tin user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Không thể lấy thông tin user (" + response.code() + ")", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    Log.d("API_USER", jsonData);

                    JSONObject root = new JSONObject(jsonData);
                    JSONObject result = root.getJSONObject("result");
                    int customerId = result.getInt("customerId");

                    // Bước 2: Lấy bookings dựa vào customerId
                    fetchBookingsByCustomerId(client, token, customerId);

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Lỗi xử lý dữ liệu user", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fetchBookingsByCustomerId(OkHttpClient client, String token, int customerId) {
        String bookingsUrl = "https://ftask.anhtudev.works/bookings?page=1&size=1000" +
                "&fromDate=2025-10-30T23%3A59%3A59%2B07%3A00" +
                "&toDate=2127-10-30T23%3A59%3A59%2B07%3A00" +
                "&customerId=" + customerId;

        Request bookingsRequest = new Request.Builder()
                .url(bookingsUrl)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(bookingsRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi lấy bookings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Không thể tải bookings (" + response.code() + ")", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    Log.d("API_BOOKINGS", jsonData);

                    JSONObject root = new JSONObject(jsonData);
                    JSONObject result = root.getJSONObject("result");
                    JSONArray content = result.getJSONArray("content");

                    bookingList.clear();
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject b = content.getJSONObject(i);
                        int id = b.getInt("id");
                        String startAt = b.getString("startAt");
                        double totalPrice = b.getDouble("totalPrice");
                        String note = b.optString("customerNote", "");
                        String status = b.getString("status");

                        // ✅ Parse tên dịch vụ từ variant
                        String serviceName = "Không có dịch vụ";
                        if (b.has("variant") && !b.isNull("variant")) {
                            JSONObject variant = b.getJSONObject("variant");
                            serviceName = variant.optString("name", "Không có dịch vụ");
                        }

                        bookingList.add(new Booking(id, startAt, totalPrice, note, status, serviceName));
                    }

                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Lỗi xử lý dữ liệu bookings", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getUserToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("accessToken", "");
        Log.d("TOKEN_DEBUG", "Token: " + token);
        return token;
    }
}
