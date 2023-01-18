package no.sikt.nva;

public final class Counter {

    private int embargoCounter;

    public Counter() {
        this.embargoCounter = 0;
    }

    public int getEmbargoCounter() {
        return embargoCounter;
    }

    public void countRecordWithEmbargo() {
        embargoCounter++;
    }
}
