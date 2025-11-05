package com.example.ftask.ui.message;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ftask.R;
import com.example.ftask.api.ChatApiService;
import com.example.ftask.models.Conversation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MessageFragment extends Fragment {

    private RecyclerView rvConversations;
    private ConversationAdapter adapter;
    private List<Conversation> conversations;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView txtEmpty;

    private ChatApiService chatApiService;
    private Handler handler = new Handler();
    private Runnable pollingRunnable;
    private static final int POLLING_INTERVAL = 5000; // 5 giây

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();

        chatApiService = new ChatApiService();

        loadConversations();
        startPolling();

        return view;
    }

    private void initViews(View view) {
        rvConversations = view.findViewById(R.id.rvConversations);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.progressBar);
        txtEmpty = view.findViewById(R.id.txtEmpty);
    }

    private void setupRecyclerView() {
        conversations = new ArrayList<>();
        adapter = new ConversationAdapter(getContext(), conversations);
        rvConversations.setLayoutManager(new LinearLayoutManager(getContext()));
        rvConversations.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(
                R.color.orange,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark
        );
        swipeRefresh.setOnRefreshListener(() -> loadConversations());
    }

    private void loadConversations() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            showError("Vui lòng đăng nhập");
            swipeRefresh.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            return;
        }

        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        chatApiService.getConversations(token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                        showError("Không thể kết nối máy chủ");
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        swipeRefresh.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful()) {
                            parseConversationsResponse(responseBody);
                        } else {
                            handleErrorResponse(response.code(), responseBody);
                        }
                    });
                }
            }
        });
    }

    private void parseConversationsResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);

            if (!jsonResponse.getBoolean("success")) {
                showError(jsonResponse.optString("message", "Có lỗi xảy ra"));
                return;
            }

            JSONObject data = jsonResponse.getJSONObject("data");
            JSONArray conversationsArray = data.getJSONArray("conversations");
            int totalUnread = data.optInt("totalUnread", 0);

            conversations.clear();
            for (int i = 0; i < conversationsArray.length(); i++) {
                JSONObject obj = conversationsArray.getJSONObject(i);

                Conversation conversation = new Conversation(
                        obj.getString("id"),
                        obj.getString("partnerId"),
                        obj.getString("partnerName"),
                        obj.optString("partnerAvatar", ""),
                        obj.getString("lastMessage"),
                        obj.getLong("lastMessageTime"),
                        obj.getInt("unreadCount"),
                        obj.optString("partnerRole", "tasker"),
                        obj.optBoolean("isOnline", false)
                );
                conversations.add(conversation);
            }

            adapter.updateConversations(conversations);
            updateEmptyState();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi xử lý dữ liệu");
        }
    }

    private void handleErrorResponse(int statusCode, String responseBody) {
        try {
            JSONObject errorJson = new JSONObject(responseBody);
            String errorMessage = errorJson.optString("message", "Có lỗi xảy ra");

            switch (statusCode) {
                case 401:
                    showError("Phiên đăng nhập hết hạn");
                    // TODO: Redirect to login
                    break;
                case 403:
                    showError("Không có quyền truy cập");
                    break;
                case 404:
                    showError("Không tìm thấy dữ liệu");
                    break;
                case 500:
                    showError("Lỗi máy chủ");
                    break;
                default:
                    showError(errorMessage);
            }
        } catch (Exception e) {
            showError("Không thể tải danh sách chat");
        }
    }

    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            rvConversations.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            rvConversations.setVisibility(View.VISIBLE);
        }
    }

    private void startPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && isVisible()) {
                    loadConversations();
                }
                handler.postDelayed(this, POLLING_INTERVAL);
            }
        };
        handler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(pollingRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(pollingRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startPolling();
    }
}