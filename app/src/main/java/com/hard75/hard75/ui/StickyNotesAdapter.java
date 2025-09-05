package com.hard75.hard75.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hard75.hard75.R;
import com.hard75.hard75.model.StickyNote;

import java.util.ArrayList;
import java.util.List;

public class StickyNotesAdapter extends RecyclerView.Adapter<StickyNotesAdapter.VH> {

    private final List<StickyNote> items = new ArrayList<>();

    public void submit(List<StickyNote> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sticky_note, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        StickyNote n = items.get(position);
        holder.title.setText(n.getTitle());
        holder.body.setText(n.getBody());
        holder.card.setCardBackgroundColor(
                holder.itemView.getContext().getColor(n.getColorRes())
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, body;
        MaterialCardView card;
        VH(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            title = itemView.findViewById(R.id.tvTitle);
            body  = itemView.findViewById(R.id.tvBody);
        }
    }
}
