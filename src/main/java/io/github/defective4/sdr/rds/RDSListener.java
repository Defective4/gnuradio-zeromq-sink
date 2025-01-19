package io.github.defective4.sdr.rds;

public interface RDSListener {
    void clockUpdated(String time);

    void flagsUpdated(RDSFlags flags);

    void programInfoUpdated(String programInfo);

    void programTypeUpdated(String programType);

    void radiotextUpdated(String radiotext);

    void stationUpdated(String station);
}
