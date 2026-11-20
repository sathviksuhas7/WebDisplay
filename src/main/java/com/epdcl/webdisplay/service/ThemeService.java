package com.epdcl.webdisplay.service;

import com.epdcl.webdisplay.entity.AppSetting;
import com.epdcl.webdisplay.repo.AppSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ThemeService {

    private static final String THEME_KEY = "site_theme";
    private final AppSettingRepository settings;

    public ThemeService(AppSettingRepository settings) {
        this.settings = settings;
    }

    @Transactional(readOnly = true)
    public String getTheme() {
        return settings.findById(THEME_KEY)
                .map(AppSetting::getSettingValue)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .filter(value -> value.equals("LIGHT") || value.equals("DARK"))
                .orElse("LIGHT");
    }

    @Transactional
    public String saveTheme(String requestedTheme) {
        if (requestedTheme == null) {
            throw new IllegalArgumentException("Select a theme.");
        }
        String theme = requestedTheme.trim().toUpperCase(Locale.ROOT);
        if (!theme.equals("LIGHT") && !theme.equals("DARK")) {
            throw new IllegalArgumentException("Theme must be LIGHT or DARK.");
        }

        AppSetting setting = settings.findById(THEME_KEY).orElseGet(() -> {
            AppSetting created = new AppSetting();
            created.setSettingKey(THEME_KEY);
            return created;
        });
        setting.setSettingValue(theme);
        settings.save(setting);
        return theme;
    }
}
