package org.ecowatt.dashboard.dto.rte;

import java.util.List;
import java.util.StringJoiner;

public class EcowattDto {
    private String s;

    private List<JourneeEcowattDto> signals;

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public List<JourneeEcowattDto> getSignals() {
        return signals;
    }

    public void setSignals(List<JourneeEcowattDto> signals) {
        this.signals = signals;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EcowattDto.class.getSimpleName() + "[", "]")
                .add("s='" + s + "'")
                .add("signals=" + signals)
                .toString();
    }
}
