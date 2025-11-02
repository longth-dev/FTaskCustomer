package com.example.ftask.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ftask.R;
import java.util.List;

public class VariantAdapter extends RecyclerView.Adapter<VariantAdapter.ViewHolder> {

    private List<ServiceVariant> variantList;
    private OnVariantClickListener listener;
    private int selectedPosition = -1;

    public VariantAdapter(List<ServiceVariant> variantList, OnVariantClickListener listener) {
        this.variantList = variantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_variant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ServiceVariant variant = variantList.get(position);
        holder.txtName.setText(variant.getName());
        holder.txtDescription.setText(variant.getDescription());
        holder.txtPrice.setText(variant.getPricePerVariant() + "đ");

        holder.layoutContainer.setBackgroundResource(
                selectedPosition == position
                        ? R.drawable.bg_option_selected
                        : R.drawable.bg_option_unselected
        );

        holder.itemView.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            listener.onVariantClick(variant);
        });
    }

    @Override
    public int getItemCount() {
        return variantList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutContainer;
        TextView txtName, txtDescription, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContainer = itemView.findViewById(R.id.layoutContainer);
            txtName = itemView.findViewById(R.id.txtName);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }

    public interface OnVariantClickListener {
        void onVariantClick(ServiceVariant variant);
    }
}
