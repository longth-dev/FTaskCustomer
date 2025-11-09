package com.example.ftask.ui.account;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ftask.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction t = transactionList.get(position);

        holder.txtDescription.setText(t.getDescription());

        // Màu sắc và dấu + / - theo loại giao dịch
        if ("TOP_UP".equals(t.getType())) {
            holder.txtAmount.setTextColor(Color.parseColor("#4CAF50")); // xanh
            holder.txtAmount.setText("+" + String.format("%,.0f₫", t.getAmount()));
        } else if ("WITHDRAW".equals(t.getType())) {
            holder.txtAmount.setTextColor(Color.parseColor("#F44336")); // đỏ
            holder.txtAmount.setText("-" + String.format("%,.0f₫", t.getAmount()));
        } else {
            holder.txtAmount.setTextColor(Color.parseColor("#000000")); // đen
            holder.txtAmount.setText(String.format("%,.0f₫", t.getAmount()));
        }

        holder.txtStatus.setText(t.getStatus());

        // Hiển thị số dư trước → sau
        holder.txtBalanceBeforeAfter.setText(String.format("Số dư: %,.0f₫ → %,.0f₫",
                t.getBalanceBefore(), t.getBalanceAfter()));
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView txtDescription, txtAmount, txtStatus, txtBalanceBeforeAfter;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtBalanceBeforeAfter = itemView.findViewById(R.id.txtBalanceBeforeAfter);
        }
    }
}
