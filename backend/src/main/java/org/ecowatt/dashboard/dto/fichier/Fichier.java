package org.ecowatt.dashboard.dto.fichier;

import org.ecowatt.dashboard.dto.web.DashboardDto;

import java.time.LocalDateTime;

public class Fichier {
    private DashboardDto dashboardDto;
    private String url;

    private LocalDateTime lastUpdate;

    public DashboardDto getDashboardDto() {
        return dashboardDto;
    }

    public void setDashboardDto(DashboardDto dashboardDto) {
        this.dashboardDto = dashboardDto;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
