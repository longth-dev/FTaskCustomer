package com.example.ftask.ui.home;

import android.os.Bundle;
import android.os.Handler;
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

import java.util.Arrays;
import java.util.List;

import me.relex.circleindicator.CircleIndicator3;

public class HomeFragment extends Fragment {

    private ViewPager2 viewPager2;
    private CircleIndicator3 indicator;
    private Handler handler = new Handler();
    private Runnable slideRunnable;

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

        // Banner
        viewPager2 = view.findViewById(R.id.viewPagerBanner);
        indicator = view.findViewById(R.id.indicator);

        BannerAdapter adapter = new BannerAdapter(images);
        viewPager2.setAdapter(adapter);
        indicator.setViewPager(viewPager2);

        startAutoSlide();

        // Service RecyclerView
        RecyclerView rvServices = view.findViewById(R.id.rvServices);
        rvServices.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        List<ServiceModel> services = Arrays.asList(
                new ServiceModel(R.drawable.cleaning, "Dọn dẹp nhà"),
                new ServiceModel(R.drawable.housereal, "Tổng vệ sinh")
        );
        rvServices.setAdapter(new ServiceAdapter(services));

        return view;
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
