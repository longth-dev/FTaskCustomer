package com.example.ftask.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ftask.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private JSONArray addressList;
    private final OnAddressClickListener listener;
    private Context context;

    public interface OnAddressClickListener {
        void onAddressClick(JSONObject address);
    }

    public AddressAdapter(JSONArray addressList, OnAddressClickListener listener) {
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        JSONObject address = addressList.optJSONObject(position);
        if (address == null) return;

        holder.txtAddressLine.setText(address.optString("addressLine"));

        // Khi click chọn địa chỉ
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAddressClick(address);
        });

        // Khi bấm xoá
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xoá địa chỉ")
                    .setMessage("Bạn có chắc muốn xoá địa chỉ này?")
                    .setPositiveButton("Xoá", (dialog, which) -> deleteAddress(position, address))
                    .setNegativeButton("Huỷ", null)
                    .show();
        });
    }

    private void deleteAddress(int position, JSONObject address) {
        int id = address.optInt("id", -1);
        if (id == -1) {
            Toast.makeText(context, "Không tìm thấy ID địa chỉ", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://ftask.anhtudev.works/addresses/" + id;

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                response -> {
                    removeItem(position);
                    Toast.makeText(context, "Đã xoá địa chỉ", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(context, "Lỗi khi xoá địa chỉ", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                String token = prefs.getString("accessToken", null);
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                if (token != null) headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void removeItem(int position) {
        JSONArray newList = new JSONArray();
        for (int i = 0; i < addressList.length(); i++) {
            if (i != position) newList.put(addressList.optJSONObject(i));
        }
        addressList = newList;
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, addressList.length());
    }

    @Override
    public int getItemCount() {
        return addressList.length();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView txtAddressLine, txtCity;
        ImageView btnDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAddressLine = itemView.findViewById(R.id.txtAddressLine);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
