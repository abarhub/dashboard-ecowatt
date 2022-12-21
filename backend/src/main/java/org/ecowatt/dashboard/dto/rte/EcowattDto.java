package org.ecowatt.dashboard.dto.rte;

import java.util.List;
import java.util.StringJoiner;

public class EcowattDto {

    private List<JourneeEcowattDto> signals;

    public List<JourneeEcowattDto> getSignals() {
        return signals;
    }

    public void setSignals(List<JourneeEcowattDto> signals) {
        this.signals = signals;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EcowattDto.class.getSimpleName() + "[", "]")
                .add("signals=" + signals)
                .toString();
    }
}
