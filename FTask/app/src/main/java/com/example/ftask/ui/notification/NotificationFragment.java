package com.example.ftask.ui.notification;

import androidx.fragment.app.Fragment;
import androidx.annotation.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.ftask.MainActivity;
import com.example.ftask.R;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.*;
import java.util.*;

public class NotificationFragment extends Fragment {

    private ListView listView;
    private TextView tvNoNotification;
    private Button btnReadAll;
    private List<NotificationItem> list = new ArrayList<>();
    private NotificationAdapter adapter;
    private String token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        listView = view.findViewById(R.id.listNotifications);
        tvNoNotification = view.findViewById(R.id.tvNoNotification);
        btnReadAll = view.findViewById(R.id.btnReadAll);

        adapter = new NotificationAdapter(requireContext(), list);
        listView.setAdapter(adapter);

        token = requireContext().getSharedPreferences("MyPrefs", requireContext().MODE_PRIVATE)
                .getString("accessToken", null);

        if (token == null) {
            Toast.makeText(getContext(), "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return view;
        }

        loadNotifications();

        btnReadAll.setOnClickListener(v -> markAllAsRead());
        listView.setOnItemClickListener((parent, itemView, position, id) -> markAsRead(position));

        return view;
    }

    // 🔹 Load danh sách thông báo
    private void loadNotifications() {
        String url = "https://ftask.anhtudev.works/notifications";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray arr = response.getJSONArray("result");
                        list.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            list.add(new NotificationItem(
                                    obj.getInt("id"),
                                    obj.getString("title"),
                                    obj.getString("message"),
                                    obj.getString("createdAt"),
                                    obj.getBoolean("isRead")
                            ));
                        }

                        adapter.notifyDataSetChanged();
                        tvNoNotification.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

                        // Cập nhật lại badge
                        updateBadge();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Lỗi xử lý dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Không thể tải thông báo!", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    // 🔹 Đánh dấu 1 thông báo đã đọc
    private void markAsRead(int position) {
        NotificationItem item = list.get(position);
        if (item.isRead) return;

        String url = "https://ftask.anhtudev.works/notifications/" + item.id + "/read";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    item.isRead = true;
                    adapter.notifyDataSetChanged();
                    updateBadge();
                },
                error -> Toast.makeText(getContext(), "Lỗi đánh dấu đã đọc!", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    // 🔹 Đánh dấu tất cả thông báo đã đọc
    private void markAllAsRead() {
        String url = "https://ftask.anhtudev.works/notifications/read-all";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
                response -> {
                    for (NotificationItem n : list) n.isRead = true;
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();

                    // Ẩn badge ngay lập tức
                    updateBadge();
                },
                error -> Toast.makeText(getContext(), "Lỗi khi đánh dấu tất cả!", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    // 🔹 Hàm cập nhật badge trên bottom navbar
    private void updateBadge() {
        if (getActivity() == null) return;

        BottomNavigationView navView = getActivity().findViewById(R.id.nav_view);
        if (navView == null) return;

        // Đếm số thông báo chưa đọc
        long unreadCount = list.stream().filter(n -> !n.isRead).count();

        BadgeDrawable badge = navView.getOrCreateBadge(R.id.notificationFragment);
        if (unreadCount > 0) {
            badge.setVisible(true);
            badge.setNumber((int) unreadCount);
            badge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            badge.clearNumber();
            badge.setVisible(false);
        }
    }
}
