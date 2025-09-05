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

public class DayBoardAdapter extends RecyclerView.Adapter<DayBoardAdapter.VH> {

    public interface Listener {
        void onDayClick(DaySticker day);      // открыть чек-лист дня
        void onDayLongClick(DaySticker day);  // пометить «победа»
    }

    private final List<DaySticker> items = new ArrayList<>();
    private final Listener listener;

    public DayBoardAdapter(Listener listener) { this.listener = listener; }

    public void submit(List<DaySticker> data) {
        items.clear(); items.addAll(data); notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_day_sticker, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DaySticker d = items.get(pos);
        h.tvDayTitle.setText("День " + d.dayIndex);
        h.tvWin.setText("День " + d.dayIndex + " — победа");

        if (d.completed) {
            h.stateActive.setVisibility(View.GONE);
            h.stateDone.setVisibility(View.VISIBLE);
            ((MaterialCardView) h.itemView).setCardBackgroundColor(
                    h.itemView.getContext().getColor(R.color.sticker_mint)); // мягкий цвет победы
        } else {
            h.stateActive.setVisibility(View.VISIBLE);
            h.stateDone.setVisibility(View.GONE);
            ((MaterialCardView) h.itemView).setCardBackgroundColor(
                    h.itemView.getContext().getColor(R.color.sticker_yellow));
        }

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onDayClick(d); });
        h.itemView.setOnLongClickListener(v -> { if (listener != null) listener.onDayLongClick(d); return true; });
    }

    @Override public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDayTitle, tvWin;
        LinearLayout stateActive, stateDone;
        VH(@NonNull View itemView) {
            super(itemView);
            tvDayTitle = itemView.findViewById(R.id.tvDayTitle);
            tvWin = itemView.findViewById(R.id.tvWin);
            stateActive = itemView.findViewById(R.id.stateActive);
            stateDone = itemView.findViewById(R.id.stateDone);
        }
    }
}
