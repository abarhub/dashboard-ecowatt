package org.ecowatt.dashboard.dto.rte;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.StringJoiner;

public class JourneeEcowattDto {
    private String GenerationFichier;
    private String jour;

    private int dvalue;

    private String message;

    private List<StatutEcowatDto> values;

    @JsonProperty("GenerationFichier")
    public String getGenerationFichier() {
        return GenerationFichier;
    }

    public void setGenerationFichier(String generationFichier) {
        GenerationFichier = generationFichier;
    }

    public String getJour() {
        return jour;
    }

    public void setJour(String jour) {
        this.jour = jour;
    }

    public int getDvalue() {
        return dvalue;
    }

    public void setDvalue(int dvalue) {
        this.dvalue = dvalue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<StatutEcowatDto> getValues() {
        return values;
    }

    public void setValues(List<StatutEcowatDto> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JourneeEcowattDto.class.getSimpleName() + "[", "]")
                .add("GenerationFichier='" + GenerationFichier + "'")
                .add("jour='" + jour + "'")
                .add("dvalue=" + dvalue)
                .add("message='" + message + "'")
                .add("values=" + values)
                .toString();
    }
}
