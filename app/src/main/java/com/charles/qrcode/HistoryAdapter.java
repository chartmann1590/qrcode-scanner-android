package com.charles.qrcode;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<ScanItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ScanItem item);
        void onCopyClick(ScanItem item);
        void onShareClick(ScanItem item);
        void onDeleteClick(ScanItem item);
    }

    public HistoryAdapter(List<ScanItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<ScanItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScanItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView contentText, formatText, timeText;
        ImageButton copyBtn, shareBtn, deleteBtn;
        ImageView typeIcon;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contentText = itemView.findViewById(R.id.historyContentText);
            formatText = itemView.findViewById(R.id.historyFormatText);
            timeText = itemView.findViewById(R.id.historyTimeText);
            copyBtn = itemView.findViewById(R.id.historyCopyBtn);
            shareBtn = itemView.findViewById(R.id.historyShareBtn);
            deleteBtn = itemView.findViewById(R.id.historyDeleteBtn);
            typeIcon = itemView.findViewById(R.id.historyTypeIcon);
        }

        public void bind(final ScanItem item, final OnItemClickListener listener) {
            contentText.setText(item.getContent());
            formatText.setText(item.getFormat());
            timeText.setText(dateFormat.format(new Date(item.getTimestamp())));

            // Set appropriate icon based on whether it looks like a URL
            String content = item.getContent().trim().toLowerCase();
            if (content.startsWith("http://") || content.startsWith("https://") || content.startsWith("www.")) {
                typeIcon.setImageResource(R.drawable.ic_link);
            } else {
                typeIcon.setImageResource(R.drawable.ic_text);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
            copyBtn.setOnClickListener(v -> {
                if (listener != null) listener.onCopyClick(item);
            });
            shareBtn.setOnClickListener(v -> {
                if (listener != null) listener.onShareClick(item);
            });
            deleteBtn.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteClick(item);
            });
        }
    }
}
