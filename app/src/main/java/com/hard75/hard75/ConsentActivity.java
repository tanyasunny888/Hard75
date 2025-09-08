package com.hard75.hard75;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets; // <-- ВАЖНО: core.graphics.Insets

import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.util.Prefs;

import java.util.concurrent.Executors;

public class ConsentActivity extends AppCompatActivity {

    private View agreementRoot;
    private TextView tvAgreement;
    private Button btnAccept, btnDecline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Рисуем под системными барами, отступы раздаём сами
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_consent);

        agreementRoot = findViewById(R.id.root);
        tvAgreement   = findViewById(R.id.tvAgreement);
        btnAccept     = findViewById(R.id.btnAccept);
        btnDecline    = findViewById(R.id.btnDecline);

        // Безопасные отступы от выреза/баров
        ViewCompat.setOnApplyWindowInsetsListener(agreementRoot, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // верх/бока — чтобы текст не уехал под челку/статус-бар
            v.setPadding(sys.left, sys.top, sys.right, v.getPaddingBottom());

            // низ — чтобы кнопки не уползали под жестовую панель/навигацию
            int baseBottom = dp(16);
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), baseBottom + sys.bottom);

            return insets;
        });

        if (Prefs.isConsentAccepted(this)) {
            agreementRoot.setVisibility(View.GONE);
            routeToNext();
        } else {
            agreementRoot.setVisibility(View.VISIBLE);
            tvAgreement.setText(getString(R.string.user_agreement_text));

            btnAccept.setOnClickListener(v -> {
                Prefs.setConsentAccepted(this, true);
                routeToNext();
            });
            btnDecline.setOnClickListener(v -> finishAffinity());
        }
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }

    /** Проверяем активный челлендж и роутим дальше. */
    private void routeToNext() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ChallengeDao cdao = AppDatabase.get(this).challengeDao();
            ChallengeEntity active = cdao.getActive();
            runOnUiThread(() -> {
                Intent i = (active != null)
                        ? new Intent(this, ChallengeBoardActivity.class)
                        : new Intent(this, CreateChallengeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });
        });
    }
}
