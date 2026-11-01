package github.mcdatapack.minecraft.api;

import java.util.NoSuchElementException;

public class OptionalFloat {
    private static final OptionalFloat EMPTY = new OptionalFloat();

    private final boolean isPresent;
    private final float value;

    private OptionalFloat() {
        this.isPresent = false;
        this.value = Float.NaN;
    }

    public static OptionalFloat empty() {
        return EMPTY;
    }

    private OptionalFloat(float value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalFloat of(float value) {
        return new OptionalFloat(value);
    }

    public float getAsFloat() {
        if (!isPresent) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public boolean isEmpty() {
        return !isPresent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof OptionalFloat other
                && (isPresent && other.isPresent
                ? Double.compare(value, other.value) == 0
                : isPresent == other.isPresent);
    }

    @Override
    public int hashCode() {
        return isPresent ? Float.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? ("OptionalFloat[" + value + "]")
                : "OptionalFloat.empty";
    }
}