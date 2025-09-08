package com.hard75.hard75.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hard75.hard75.R;

import java.util.ArrayList;
import java.util.List;

public class CreateTasksAdapter extends RecyclerView.Adapter<CreateTasksAdapter.VH> {

    public interface Listener {
        void onEdit(int position, String currentText);
        void onDelete(int position);
    }

    private final List<String> items = new ArrayList<>();
    private final Listener listener;

    public CreateTasksAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submit(List<String> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public List<String> data() { return new ArrayList<>(items); }

    public void addItem(String text) {
        items.add(text);
        notifyItemInserted(items.size() - 1);
    }

    public void replace(int position, String text) {
        if (position >= 0 && position < items.size()) {
            items.set(position, text);
            notifyItemChanged(position);
        }
    }

    public void remove(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override public long getItemId(int position) { return position; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_editable_task, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        String t = items.get(position);
        h.tvTitle.setText(t);
        h.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(h.getBindingAdapterPosition(), t); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(h.getBindingAdapterPosition()); });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        ImageButton btnEdit, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
