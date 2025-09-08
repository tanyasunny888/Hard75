package com.hard75.hard75.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hard75.hard75.R;
import com.hard75.hard75.data.db.DayTaskEntity;

import java.util.ArrayList;
import java.util.List;

public class DayTasksAdapter extends RecyclerView.Adapter<DayTasksAdapter.VH> {

    public interface OnToggle { void onToggle(DayTaskEntity task, boolean checked); }

    private final List<DayTaskEntity> items = new ArrayList<>();
    private final OnToggle onToggle;
    private boolean editable = true;

    public DayTasksAdapter(OnToggle onToggle) {
        this.onToggle = onToggle;
        setHasStableIds(true);
    }

    public void setEditable(boolean editable) { this.editable = editable; notifyDataSetChanged(); }

    public void submit(List<DayTaskEntity> data) {
        items.clear(); if (data != null) items.addAll(data); notifyDataSetChanged();
    }

    @Override public long getItemId(int position) { return items.get(position).id; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CheckBox cb = (CheckBox) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_task_check, parent, false);
        return new VH(cb);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DayTaskEntity t = items.get(pos);
        h.cb.setText(t.title);

        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(t.isDone);

        h.cb.setEnabled(editable);                     // ← read-only выключает чекбокс
        if (editable && onToggle != null) {
            h.cb.setOnCheckedChangeListener((b, checked) -> onToggle.onToggle(t, checked));
        } else {
            h.cb.setOnCheckedChangeListener(null);
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cb; VH(@NonNull CheckBox itemView) { super(itemView); cb = itemView; }
    }
}

