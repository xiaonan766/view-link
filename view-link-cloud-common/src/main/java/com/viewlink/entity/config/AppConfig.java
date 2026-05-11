package com.viewlink.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.account:}")
    private String adminAccount;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${showFFmpegLog:true}")
    private Boolean showFFmpegLog;

    @Value("${es.host.port:192.168.88.130:9200}")
    private String esHostPort;

    @Value("${es.index.video.name:view-link_video}")
    private String esIndexVideoName;

    public String getProjectFolder() {
        return projectFolder;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public Boolean getShowFFmpegLog() {
        return showFFmpegLog;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public void setShowFFmpegLog(Boolean showFFmpegLog) {
        this.showFFmpegLog = showFFmpegLog;
    }

    public String getEsHostPort() {
        return esHostPort;
    }

    public void setEsHostPort(String esHostPort) {
        this.esHostPort = esHostPort;
    }

    public String getEsIndexVideoName() {
        return esIndexVideoName;
    }

    public void setEsIndexVideoName(String esIndexVideoName) {
        this.esIndexVideoName = esIndexVideoName;
    }
}
