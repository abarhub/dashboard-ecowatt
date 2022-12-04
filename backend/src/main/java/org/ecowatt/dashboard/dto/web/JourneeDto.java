package org.ecowatt.dashboard.dto.web;

import java.time.LocalDate;
import java.util.List;

public class JourneeDto {

    private LocalDate date;
    private String message;

    private StatusEnum statut;

    private List<HeureDto> heures;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<HeureDto> getHeures() {
        return heures;
    }

    public void setHeures(List<HeureDto> heures) {
        this.heures = heures;
    }

    public StatusEnum getStatut() {
        return statut;
    }

    public void setStatut(StatusEnum statut) {
        this.statut = statut;
    }
}
