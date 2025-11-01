package com.example.ftask.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class JobDetailPagerAdapter extends FragmentStateAdapter {

    public JobDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new RoomFragment("Phòng ngủ");
            case 1: return new RoomFragment("Phòng tắm");
            case 2: return new RoomFragment("Nhà bếp");
            default: return new RoomFragment("Phòng khách và khu vực chung");
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}