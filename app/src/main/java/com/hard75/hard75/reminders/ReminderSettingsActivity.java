package com.hard75.hard75.reminders;

import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.TimePickerDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;


import com.hard75.hard75.R;
import com.hard75.hard75.util.Prefs;

import com.google.android.material.appbar.MaterialToolbar;
// import com.google.android.material.color.MaterialColors; // <- удалить, если не используете

import androidx.core.view.WindowCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;

public class ReminderSettingsActivity extends AppCompatActivity {

    private SwitchCompat swEnable;
    private TextView tvMorning, tvEvening;
    private Button btnMorning, btnEvening;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Рисуем под системными барами — инсетами управляем сами
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_reminder_settings);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        if (tb != null) {
            setSupportActionBar(tb);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            tb.setNavigationOnClickListener(v ->
                    getOnBackPressedDispatcher().onBackPressed()
            );
        } else {
            // Если тулбара нет в layout — просто не включаем up-кнопку
        }

        View root = findViewById(R.id.rootReminder);
        View scroll = findViewById(R.id.scroll);

        // Зафиксируем исходные размеры/паддинги
        final int tbPadStart = (tb != null) ? tb.getPaddingStart() : 0;
        final int tbPadTop   = (tb != null) ? tb.getPaddingTop()   : 0;
        final int tbPadEnd   = (tb != null) ? tb.getPaddingEnd()   : 0;
        final int tbPadBot   = (tb != null) ? tb.getPaddingBottom(): 0;

        final int scPadStart = (scroll != null) ? scroll.getPaddingStart() : 0;
        final int scPadTop   = (scroll != null) ? scroll.getPaddingTop()   : 0;
        final int scPadEnd   = (scroll != null) ? scroll.getPaddingEnd()   : 0;
        final int scPadBot   = (scroll != null) ? scroll.getPaddingBottom(): 0;

// ВАЖНО: сохраним базовую высоту тулбара (обычно ?attr/actionBarSize)
        final int tbBaseHeight = (tb != null) ? tb.getLayoutParams().height : 0;

        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                if (tb != null) {
                    ViewGroup.LayoutParams lp = tb.getLayoutParams();
                    lp.height = tbBaseHeight + sys.top;
                    tb.setLayoutParams(lp);

                    // компенсируем, чтобы контент не центрировался в "лишней" области сверху
                    tb.setPaddingRelative(tbPadStart, tbPadTop + sys.top, tbPadEnd, tbPadBot);
                }


                if (scroll != null) {
                    // Добавим только нижний отступ под навбар/жестовую панель
                    scroll.setPaddingRelative(scPadStart, scPadTop, scPadEnd, scPadBot + sys.bottom);
                }

                return insets;
            });
            ViewCompat.requestApplyInsets(root);
        }


        swEnable   = findViewById(R.id.swEnable);
        tvMorning  = findViewById(R.id.tvMorningTime);
        tvEvening  = findViewById(R.id.tvEveningTime);
        btnMorning = findViewById(R.id.btnMorning);
        btnEvening = findViewById(R.id.btnEvening);

        // init UI
        refreshTimes();
        boolean enabled = Prefs.isRemindersEnabled(this);
        swEnable.setChecked(enabled);
        setTimeControlsEnabled(enabled);

        swEnable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.setRemindersEnabled(this, isChecked);
            setTimeControlsEnabled(isChecked);
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 6001);
                }
                if (!ReminderPermissions.canScheduleExactAlarms(this)) {
                    ReminderPermissions.requestExactAlarm(this);
                    Toast.makeText(this, "Разрешите «Точные будильники» в системе", Toast.LENGTH_LONG).show();
                    return;
                }
                ReminderScheduler.scheduleAll(this);
                Toast.makeText(this, "Напоминания включены", Toast.LENGTH_SHORT).show();
            } else {
                ReminderScheduler.cancelAll(this);
                Toast.makeText(this, "Напоминания выключены", Toast.LENGTH_SHORT).show();
            }
        });

        btnMorning.setOnClickListener(v -> {
            int h = Prefs.getMorningHour(this), m = Prefs.getMorningMinute(this);
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Prefs.setMorningTime(this, hourOfDay, minute);
                refreshTimes();
                if (Prefs.isRemindersEnabled(this)) {
                    if (!ReminderPermissions.canScheduleExactAlarms(this)) {
                        ReminderPermissions.requestExactAlarm(this);
                        Toast.makeText(this, "Разрешите «Точные будильники»", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ReminderScheduler.rescheduleMorning(this);
                }
            }, h, m, true).show();
        });

        btnEvening.setOnClickListener(v -> {
            int h = Prefs.getEveningHour(this), m = Prefs.getEveningMinute(this);
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                Prefs.setEveningTime(this, hourOfDay, minute);
                refreshTimes();
                if (Prefs.isRemindersEnabled(this)) {
                    if (!ReminderPermissions.canScheduleExactAlarms(this)) {
                        ReminderPermissions.requestExactAlarm(this);
                        Toast.makeText(this, "Разрешите «Точные будильники»", Toast.LENGTH_LONG).show();
                        return;
                    }
                    ReminderScheduler.rescheduleEvening(this);
                }
            }, h, m, true).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void refreshTimes() {
        tvMorning.setText(timeText(Prefs.getMorningHour(this), Prefs.getMorningMinute(this)));
        tvEvening.setText(timeText(Prefs.getEveningHour(this), Prefs.getEveningMinute(this)));
    }

    private String timeText(int h, int m) {
        return (h < 10 ? "0" : "") + h + ":" + (m < 10 ? "0" : "") + m;
    }

    private void setTimeControlsEnabled(boolean enabled) {
        btnMorning.setEnabled(enabled);
        btnEvening.setEnabled(enabled);
    }
}

