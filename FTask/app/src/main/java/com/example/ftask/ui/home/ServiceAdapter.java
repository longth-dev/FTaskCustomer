package com.example.ftask.ui.home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
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

        // Click Item
        holder.itemView.setOnClickListener(v -> {

            // Animation scale nhỏ rồi to lên nhẹ
            Animation scaleAnim = new ScaleAnimation(
                    0.95f, 1.0f,
                    0.95f, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            scaleAnim.setDuration(150);
            v.startAnimation(scaleAnim);

            // Mở trang đặt dịch vụ
            Intent intent = new Intent(v.getContext(), OrderCleaningActivity.class);
            intent.putExtra("serviceName", model.getName());
            v.getContext().startActivity(intent);
        });
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
