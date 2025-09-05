package com.hard75.hard75;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hard75.hard75.data.db.AppDatabase;
import com.hard75.hard75.data.db.ChallengeDao;
import com.hard75.hard75.data.db.ChallengeEntity;
import com.hard75.hard75.util.Prefs;

import java.util.concurrent.Executors;

/**
 * LAUNCHER-экран.
 * Маршрутизирует:
 * - если согласия нет — показывает текст и кнопки Принять/Не принимаю
 * - если согласие есть — проверяет, есть ли активный челлендж, и открывает:
 *      ChallengeBoardActivity (если есть) или CreateChallengeActivity (если нет)
 */
public class ConsentActivity extends AppCompatActivity {

    private View agreementRoot;   // корневой layout с текстом соглашения
    private TextView tvAgreement; // сам текст
    private Button btnAccept, btnDecline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        agreementRoot = findViewById(R.id.root);           // добавьте android:id="@+id/root" в корневой layout activity_consent.xml
        tvAgreement   = findViewById(R.id.tvAgreement);
        btnAccept     = findViewById(R.id.btnAccept);
        btnDecline    = findViewById(R.id.btnDecline);

        // если согласие уже принято — сразу роутим, экран соглашения не показываем
        if (Prefs.isConsentAccepted(this)) {
            agreementRoot.setVisibility(View.GONE);
            routeToNext(); // проверяем БД и открываем нужный экран
        } else {
            // показываем текст и кнопки
            agreementRoot.setVisibility(View.VISIBLE);
            tvAgreement.setText(getString(R.string.user_agreement_text));

            btnAccept.setOnClickListener(v -> {
                Prefs.setConsentAccepted(this, true);
                routeToNext(); // после принятия — сразу маршрутизация
            });
            btnDecline.setOnClickListener(v -> finishAffinity());
        }
    }

    /** Проверяем — есть ли активный челлендж. Открываем Board или Create. */
    private void routeToNext() {
        Executors.newSingleThreadExecutor().execute(() -> {
            ChallengeDao cdao = AppDatabase.get(this).challengeDao();
            ChallengeEntity active = cdao.getActive();

            runOnUiThread(() -> {
                Intent i = (active != null)
                        ? new Intent(this, ChallengeBoardActivity.class)
                        : new Intent(this, CreateChallengeActivity.class);
                // чтобы по "Назад" не возвращаться на ConsentActivity
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });
        });
    }
}
