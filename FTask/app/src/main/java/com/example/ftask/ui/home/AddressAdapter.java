package com.example.ftask.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ftask.R;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private JSONArray addressList;
    private OnAddressClickListener listener;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        JSONObject address = addressList.optJSONObject(position);
        holder.txtAddressLine.setText(address.optString("addressLine"));
        holder.txtCity.setText(address.optString("city"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAddressClick(address);
        });
    }

    @Override
    public int getItemCount() {
        return addressList.length();
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView txtAddressLine, txtCity;
        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAddressLine = itemView.findViewById(R.id.txtAddressLine);
            txtCity = itemView.findViewById(R.id.txtCity);
        }
    }
}