package org.ecowatt.dashboard.dto.web;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardDto {

    private LocalDateTime date;
    private List<JourneeDto> listJournees;

    public List<JourneeDto> getListJournees() {
        return listJournees;
    }

    public void setListJournees(List<JourneeDto> listJournees) {
        this.listJournees = listJournees;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
