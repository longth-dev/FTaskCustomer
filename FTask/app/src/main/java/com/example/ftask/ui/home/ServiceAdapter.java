package com.example.ftask.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ftask.R;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> services;
    private OnServiceClickListener listener;

    // 🔹 Constructor thêm listener
    public ServiceAdapter(List<ServiceModel> services, OnServiceClickListener listener) {
        this.services = services;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceModel service = services.get(position);
        holder.txtName.setText(service.getName());
        Glide.with(holder.itemView.getContext())
                .load(service.getImageUrl())
                .placeholder(R.drawable.cleaning)
                .into(holder.imgService);

        // 🔹 Click item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onServiceClick(service);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgService;
        TextView txtName, txtDescription;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgService = itemView.findViewById(R.id.imgService);
            txtName = itemView.findViewById(R.id.txtName);
                }
    }

    public interface OnServiceClickListener {
        void onServiceClick(ServiceModel service);
    }

    public void updateData(List<ServiceModel> newList) {
        this.services = newList;
        notifyDataSetChanged();
    }
}
