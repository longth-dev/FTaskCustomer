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

    private static final String API_URL =
            "https://ftask.anhtudev.works/customer/bookings?page=1&size=10&fromDate=2025-10-30T23%3A59%3A59%2B07%3A00&toDate=2027-10-30T23%3A59%3A59%2B07%3A00";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        recyclerBookings = view.findViewById(R.id.recyclerBookings);
        recyclerBookings.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new BookingAdapter(bookingList);
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
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Lỗi mạng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Không thể tải dữ liệu (" + response.code() + ")", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    Log.d("API_RESPONSE", jsonData);

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

                        bookingList.add(new Booking(id, startAt, totalPrice, note, status));
                    }

                    if (getActivity() != null)
                        getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Lỗi xử lý dữ liệu", Toast.LENGTH_SHORT).show());
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
