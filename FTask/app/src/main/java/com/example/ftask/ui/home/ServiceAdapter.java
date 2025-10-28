package com.example.ftask.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ftask.R;
import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> list;

    public ServiceAdapter(List<ServiceModel> list) {
        this.list = list;
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
        ServiceModel model = list.get(position);
        holder.tvName.setText(model.getName());
        holder.imgService.setImageResource(model.getImage());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgService;
        TextView tvName;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgService = itemView.findViewById(R.id.imgService);
            tvName = itemView.findViewById(R.id.tvServiceName);
        }
    }
}
