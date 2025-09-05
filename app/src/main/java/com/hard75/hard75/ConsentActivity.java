package com.hard75.hard75;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hard75.hard75.util.Prefs;

public class ConsentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Prefs.isConsentAccepted(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);

        TextView tv = findViewById(R.id.tvAgreement);
        tv.setText(getAgreementText()); // грузим текст

        Button btnAccept = findViewById(R.id.btnAccept);
        Button btnDecline = findViewById(R.id.btnDecline);

        btnAccept.setOnClickListener(v -> {
            Prefs.setConsentAccepted(this, true);
            startActivity(new Intent(this, CreateChallengeActivity.class));
            finish();
        });


        btnDecline.setOnClickListener(v -> {
            finishAffinity(); // закрываем приложение
        });
    }

    private String getAgreementText() {
        return getString(R.string.user_agreement_text);
    }
}
