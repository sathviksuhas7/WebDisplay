package com.epdcl.webdisplay.repo;

import com.epdcl.webdisplay.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
