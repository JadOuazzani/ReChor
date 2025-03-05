package ch.epfl.rechor.journey;

import java.util.function.LongConsumer;

public class ParetoFront {
    private long[] tableTuples;
    public static final ParetoFront EMPTY = null;

    private ParetoFront(long[] tableTuples) {
        this.tableTuples = tableTuples;
    }

    public int size() {
        return tableTuples.length;
    }

    public long get(int arrMins, int changes) {

    }

    public void forEach(LongConsumer action){

    }

}
