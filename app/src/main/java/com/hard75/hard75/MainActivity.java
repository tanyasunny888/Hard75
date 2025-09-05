package com.hard75.hard75;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.StickyNoteDao;
import com.hard75.hard75.data.db.StickyNoteEntity;
import com.hard75.hard75.model.StickyNote;
import com.hard75.hard75.ui.StickyNotesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private StickyNotesAdapter adapter;
    private StickyNoteDao dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dao = AppDatabase.get(this).stickyNoteDao();

        RecyclerView rv = findViewById(R.id.rvNotes);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new StickyNotesAdapter();
        rv.setAdapter(adapter);

        loadNotes();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showCreateDialog());
    }

    private void loadNotes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<StickyNoteEntity> all = dao.getAllDesc();
            List<StickyNote> ui = new ArrayList<>();
            for (StickyNoteEntity e : all) {
                ui.add(new StickyNote(e.id, e.title, e.body, e.colorRes));
            }
            runOnUiThread(() -> adapter.submit(ui));
        });
    }

    private void showCreateDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_note, null, false);
        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etBody  = view.findViewById(R.id.etBody);
        Spinner spColor  = view.findViewById(R.id.spColor);

        ArrayAdapter<CharSequence> colorsAdapter = ArrayAdapter.createFromResource(
                this, R.array.sticker_color_names, android.R.layout.simple_spinner_item);
        colorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spColor.setAdapter(colorsAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Новый стикер")
                .setView(view)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    String body  = etBody.getText().toString().trim();
                    int colorRes = pickColorRes(spColor.getSelectedItemPosition());
                    saveNote(title, body, colorRes);
                })
                .show();
    }

    private int pickColorRes(int idx) {
        switch (idx) {
            case 1: return R.color.sticker_mint;
            case 2: return R.color.sticker_lavender;
            case 3: return R.color.sticker_peach;
            case 4: return R.color.sticker_blue;
            default: return R.color.sticker_yellow;
        }
    }

    private void saveNote(String title, String body, int colorRes) {
        Executors.newSingleThreadExecutor().execute(() -> {
            StickyNoteEntity e = new StickyNoteEntity(
                    title.isEmpty() ? "Стикер" : title,
                    body,
                    colorRes,
                    System.currentTimeMillis()
            );
            dao.insert(e);
            loadNotes(); // перезагрузим список
        });
    }
}
