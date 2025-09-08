package com.hard75.hard75.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChallengeTemplates {

    public static List<String> baseTasks(String level) {
        switch (level) {
            case "soft":
                return Arrays.asList(
                        "Вода 1.5–2 л",
                        "Лёгкая активность 20 мин",
                        "Чтение 10 мин",
                        "Сон ≥7 ч",
                        "Без алкоголя"
                );
            case "medium":
                return Arrays.asList(
                        "Тренировка 30–45 мин",
                        "Питание без фастфуда",
                        "Вода 2–2.5 л",
                        "Чтение 20 мин",
                        "Сон ≥7 ч",
                        "Прогулка 5k шагов",
                        "Ежедневный чек-ин"
                );
            default: // "hard"
                return Arrays.asList(
                        "Тренировка(ы) ≥60 мин",
                        "Строгий рацион (без быстрых углеводов)",
                        "Вода ≥2 л",
                        "Чтение 30 мин",
                        "Фото-чек",
                        "Растяжка",
                        "Медитация 10 мин",
                        "Сон ≥7.5 ч",
                        "Без алкоголя/сигарет",
                        "Холодный душ"
                );
        }
    }

    public static List<String> filterDisabled(List<String> base, boolean[] enabled) {
        if (enabled == null || enabled.length != base.size()) return base;
        List<String> out = new ArrayList<>();
        for (int i = 0; i < base.size(); i++) if (enabled[i]) out.add(base.get(i));
        return out;
    }
}
