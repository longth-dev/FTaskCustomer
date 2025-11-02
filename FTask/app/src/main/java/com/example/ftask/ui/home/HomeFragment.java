package com.example.ftask.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ftask.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager2;
    private CircleIndicator3 indicator;
    private Handler handler = new Handler();
    private Runnable slideRunnable;

    private RecyclerView rvServices;
    private ServiceAdapter serviceAdapter;

    private List<Integer> images = Arrays.asList(
            R.drawable.viecnha,
            R.drawable.viecnha1,
            R.drawable.viecnha2
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 🖼️ Banner
        viewPager2 = view.findViewById(R.id.viewPagerBanner);
        indicator = view.findViewById(R.id.indicator);

        BannerAdapter adapter = new BannerAdapter(images);
        viewPager2.setAdapter(adapter);
        indicator.setViewPager(viewPager2);
        startAutoSlide();

        // 🔧 Service RecyclerView
        rvServices = view.findViewById(R.id.rvServices);
        rvServices.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        serviceAdapter = new ServiceAdapter(new ArrayList<>(), service -> {
            Intent intent = new Intent(getContext(), OrderCleaningActivity.class);
            intent.putExtra("SERVICE_ID", service.getId());
            intent.putExtra("SERVICE_NAME", service.getName());
            startActivity(intent);
        });


        rvServices.setAdapter(serviceAdapter);
        loadServices();

        return view;
    }
    private void loadServices() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://ftask.anhtudev.works/service-catalogs")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Không thể kết nối máy chủ", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String res = response.body().string();

                if (response.isSuccessful()) {
                    try {
                        JSONObject obj = new JSONObject(res);
                        JSONArray arr = obj.getJSONArray("result");

                        List<ServiceModel> list = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject s = arr.getJSONObject(i);
                            list.add(new ServiceModel(
                                    s.getInt("id"),
                                    s.getString("name"),
                                    s.getString("description"),
                                    s.getString("imageUrl")
                            ));
                        }

                        if (getActivity() != null)
                            getActivity().runOnUiThread(() -> serviceAdapter.updateData(list));

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Lỗi xử lý dữ liệu dịch vụ", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Tải danh sách dịch vụ thất bại", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void startAutoSlide() {
        handler.removeCallbacksAndMessages(null);
        slideRunnable = () -> {
            if (viewPager2 == null || images.isEmpty()) return;

            int nextPage = viewPager2.getCurrentItem() + 1;
            viewPager2.setCurrentItem(nextPage % images.size(), true);

            handler.postDelayed(slideRunnable, 3000);
        };
        handler.postDelayed(slideRunnable, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoSlide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}
