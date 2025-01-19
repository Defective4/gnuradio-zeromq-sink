package io.github.defective4.sdr.rds;

public abstract class RDSAdapter implements RDSListener {
    @Override
    public void clockUpdated(String time) {}

    @Override
    public void flagsUpdated(RDSFlags flags) {}

    @Override
    public void programInfoUpdated(String programInfo) {}

    @Override
    public void programTypeUpdated(String programType) {}

    @Override
    public void radiotextUpdated(String radiotext) {}

    @Override
    public void stationUpdated(String station) {}
}
