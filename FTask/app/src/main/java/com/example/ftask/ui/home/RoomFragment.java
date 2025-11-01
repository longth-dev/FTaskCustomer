package com.example.ftask.ui.home;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.example.ftask.R;
import java.util.ArrayList;
import java.util.List;

public class RoomFragment extends Fragment {

    private String roomType;

    public RoomFragment(String roomType) {
        this.roomType = roomType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);
        ListView listView = view.findViewById(R.id.listTasks);

        List<String> tasks = new ArrayList<>();

        switch (roomType) {
            case "Phòng ngủ":
                tasks.add("⭐ Lau bụi và lau tất cả các bề mặt");
                tasks.add("⭐ Lau công tắc và tay cầm");
                tasks.add("⭐ Lau sạch giường, sắp xếp lại giường cho gọn gàng");
                tasks.add("⭐ Hút bụi và lau sàn");
                break;

            case "Phòng tắm":
                tasks.add("⭐ Làm sạch toilet");
                tasks.add("⭐ Lau chùi vòi sen, bồn tắm và bồn rửa");
                tasks.add("⭐ Làm sạch bên ngoài tủ, gương và đồ đạc");
                tasks.add("⭐ Lau công tắc và tay cầm");
                tasks.add("⭐ Sắp xếp ngăn nắp các vật dụng");
                tasks.add("⭐ Đổ rác");
                tasks.add("⭐ Quét và lau sàn");
                break;

            case "Nhà bếp":
                tasks.add("⭐ Rửa chén và xếp chén dĩa");
                tasks.add("⭐ Lau bụi và lau tất cả các bề mặt");
                tasks.add("⭐ Lau mặt ngoài của tủ bếp và thiết bị gia dụng");
                tasks.add("⭐ Lau công tắc và tay cầm");
                tasks.add("⭐ Cọ rửa bếp và mặt bàn");
                tasks.add("⭐ Làm sạch bồn rửa");
                tasks.add("⭐ Đổ rác");
                tasks.add("⭐ Quét và lau sàn");
                break;

            default:
                tasks.add("⭐ Quét bụi và lau tất cả các bề mặt");
                tasks.add("⭐ Lau công tắc và tay cầm");
                tasks.add("⭐ Đổ rác");
                tasks.add("⭐ Quét và lau sàn");
                break;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, tasks);
        listView.setAdapter(adapter);

        return view;
    }
}
