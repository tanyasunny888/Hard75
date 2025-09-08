package com.hard75.hard75.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hard75.hard75.R;
import com.hard75.hard75.model.DaySticker;

import java.util.ArrayList;
import java.util.List;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import android.widget.ImageView;


public class DayBoardAdapter extends RecyclerView.Adapter<DayBoardAdapter.VH> {

    public interface Listener {
        void onDayClick(DaySticker day);      // открыть чек-лист
        void onDayLongClick(DaySticker day);  // форс-победа (если нужно)
    }

    private final List<DaySticker> items = new ArrayList<>();
    private final Listener listener;

    public DayBoardAdapter(Listener l) { this.listener = l; }
    public void submit(List<DaySticker> data) { items.clear(); items.addAll(data); notifyDataSetChanged(); }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_sticker, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        DaySticker d = items.get(pos);

        h.tvDay.setText("День " + d.dayIndex);

        final boolean isCompleted = d.completed || d.percent >= 100;

        // визуальное состояние
        if (isCompleted) {
            h.ivDone.setVisibility(View.VISIBLE);
            h.progress.setVisibility(View.GONE);

            h.bgImage.setAlpha(0.5f);
            h.contentContainer.setAlpha(0.5f);
            h.ivDone.setAlpha(1f);
        } else {
            h.ivDone.setVisibility(View.GONE);
            h.progress.setVisibility(View.VISIBLE);

            h.progress.setMax(100);
            h.progress.setProgress(d.percent);

            h.bgImage.setAlpha(1f);
            h.contentContainer.setAlpha(1f);
        }

        // ⚠️ клики НЕ отключаем — Activity решит editable по календарю
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onDayClick(d); });

    }



    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDay, tvPercent;
        CircularProgressIndicator progress;
        ImageView ivDone, bgImage;
        View contentContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            progress = itemView.findViewById(R.id.progress);
            ivDone = itemView.findViewById(R.id.ivDone);
            bgImage = itemView.findViewById(R.id.bgImage);
            contentContainer = itemView.findViewById(R.id.contentContainer);

        }
    }
}

