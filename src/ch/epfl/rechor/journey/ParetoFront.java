package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

import static ch.epfl.rechor.journey.PackedCriteria.GAP;

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
        long translatedArr = ((long) (arrMins - GAP)) << 39;
        long criteriaTwo = (long) changes << 32;
        long soughtTuple = translatedArr | criteriaTwo;

        for (long candidate : tableTuples) {
            if (candidate == soughtTuple) {
                return candidate;
            }
        }

        throw new NoSuchElementException("Critères introuvables dans la frontière");
    }

    public void forEach(LongConsumer action) {
        for (long tuple : tableTuples) {
            action.accept(tuple);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ParetoFront[");
        for (int i = 0; i < tableTuples.length; i++) {
            long tuple = tableTuples[i];
            if (i > 0) {
                sb.append("; ");
            }
            sb.append("(");
            // Si le tuple contient une heure de départ, on l'affiche
            if (PackedCriteria.hasDepMins(tuple)) {
                int dep = PackedCriteria.depMins(tuple);
                LocalDateTime depTime = LocalDateTime.of(2025, 2, 18, 0, 0).plusMinutes(dep);
                sb.append("dep=").append(FormatterFr.formatTime(depTime)).append(", ");
            }
            int arr = PackedCriteria.arrMins(tuple);
            LocalDateTime arrTime = LocalDateTime.of(2025, 2, 18, 0, 0).plusMinutes(arr);
            int changes = PackedCriteria.changes(tuple);
            sb.append("arr=").append(FormatterFr.formatTime(arrTime));
            sb.append(", chg=").append(changes);
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }

    public static class Builder {
        long[] tableTuples;
        int size;

        public Builder() {
            this.tableTuples = new long[1];
            this.size = 0;
        }

        public Builder(Builder that) {
            this.tableTuples = new long[that.tableTuples.length];
            System.arraycopy(that.tableTuples, 0, this.tableTuples, 0, that.size);
            this.size = that.size;

        }

        public boolean isEmpty() {
            return size == 0;
        }

        public Builder clear() {
            size = 0;
            return this;
        }


        public Builder add(long packedTuple) {

        }

        public Builder add(int arrMins, int changes, int payload) {
        }

        public Builder addAll(Builder that) {
        }

        public boolean fullyDominates(Builder that, int depMins) {
        }

        public void forEach(LongConsumer action) {

        }

        public ParetoFront build() {
        }
    }

}
