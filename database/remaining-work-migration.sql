-- WebDisplay remaining-work migration
-- Run this once against webdisplay_db before starting the patched application.

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key VARCHAR(100) NOT NULL,
    setting_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB;

INSERT INTO app_settings (setting_key, setting_value)
VALUES ('site_theme', 'LIGHT')
ON DUPLICATE KEY UPDATE setting_value = setting_value;
