package org.ecowatt.dashboard.dto.rte;

import java.util.StringJoiner;

public class StatutEcowatDto {

    private int pas;
    private int hvalue;

    public int getPas() {
        return pas;
    }

    public void setPas(int pas) {
        this.pas = pas;
    }

    public int getHvalue() {
        return hvalue;
    }

    public void setHvalue(int hvalue) {
        this.hvalue = hvalue;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", StatutEcowatDto.class.getSimpleName() + "[", "]")
                .add("pas=" + pas)
                .add("hvalue=" + hvalue)
                .toString();
    }
}
