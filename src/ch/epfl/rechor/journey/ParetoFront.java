package ch.epfl.rechor.journey;

import ch.epfl.rechor.FormatterFr;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.function.LongConsumer;

import static ch.epfl.rechor.journey.PackedCriteria.*;

public class ParetoFront {
    private long[] tableTuples;
    public static final ParetoFront EMPTY = new ParetoFront(new long[0]);


    private ParetoFront(long[] tableTuples) {
        this.tableTuples = tableTuples;
    }

    public int size() {
        return tableTuples.length;
    }

    public long get(int arrMins, int changes) {

        for (long criteria : tableTuples) {
            if (arrMins(criteria) == arrMins && changes(criteria) == changes) {
                return criteria;
            }
        }
        throw new NoSuchElementException("Critères introuvables dans la frontière");
    }

    public void forEach(LongConsumer action) {
        for (long criteria : tableTuples) {
            action.accept(criteria);
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
        private final static long MASK = ~0xFFFFFFFFL;
        private final static int initialCapacity = 2;

        public Builder() {
            this.tableTuples = new long[initialCapacity];
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

        int compact(long[] array, int size, long newTuple) {
            int dst = 0;
            for (int src = 0; src < size; src += 1) {
                if (PackedCriteria.dominatesOrIsEqual(newTuple, array[src])) continue;
                if (dst != src) array[dst] = array[src];
                dst += 1;
            }
            return dst;
        }

        public Builder add(long packedTuple) {
            for (int i = 0; i < this.size; i++) {
                if (PackedCriteria.dominatesOrIsEqual(tableTuples[i], packedTuple)) {
                    return this;
                }
            }

            int insertionIndex = 0;
            long cmpNew = packedTuple & MASK;

            while (insertionIndex < this.size) {
                long cmpExisting = (tableTuples[insertionIndex] & MASK);
                if (cmpExisting > cmpNew) {
                    break;
                }
                insertionIndex++;
            }

            this.size = compact(tableTuples, size, packedTuple);

            if (size == tableTuples.length) {
                int newCapacity = (int) (tableTuples.length * 1.5);
                if (newCapacity == tableTuples.length) {
                    newCapacity++; // pour éviter de stagner en cas de taille 0 ou 1 (cas limite)
                }
                long[] arrayResized = new long[newCapacity];
                System.arraycopy(tableTuples, 0, arrayResized, 0, this.size);
                tableTuples = arrayResized;
            }

            System.arraycopy(tableTuples, insertionIndex, tableTuples, insertionIndex + 1, this.size - insertionIndex);
            tableTuples[insertionIndex] = packedTuple;
            size++;

            return this;
        }


        public Builder add(int arrMins, int changes, int payload) {
            long tuple = pack(arrMins, changes, payload);
            add(tuple);
            return this;
        }

        public Builder addAll(Builder that) {
            for (int i = 0; i < that.size; i++) {
                this.add(that.tableTuples[i]);
            }
            return this;
        }

        public boolean fullyDominates(Builder that, int depMins) {
            for (int i = 0; i < that.size; i++) {
                long tWithDep = withDepMins(that.tableTuples[i], depMins);
                boolean isDominated = false;
                for (int j = 0; j < this.size; j++) {
                    if (dominatesOrIsEqual(tableTuples[j], tWithDep)) {
                        isDominated = true;
                        break;
                    }

                }

                if (!isDominated) {
                    return false;
                }
            }
            return true;
        }

        public void forEach(LongConsumer action) {
            for (long criteria : tableTuples) {
                action.accept(criteria);
            }
        }

        public ParetoFront build() {
            long[] newArray = new long[this.size];
            System.arraycopy(this.tableTuples, 0, newArray, 0, this.size);
            return new ParetoFront(newArray);
        }

    }

}

