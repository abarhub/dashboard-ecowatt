package org.ecowatt.dashboard.dto.web;

import java.util.List;

public class DashboardDto {

    private List<JourneeDto> listJournees;

    public List<JourneeDto> getListJournees() {
        return listJournees;
    }

    public void setListJournees(List<JourneeDto> listJournees) {
        this.listJournees = listJournees;
    }
}
