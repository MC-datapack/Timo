package github.mcdatapack.minecraft.api;

import java.util.NoSuchElementException;

public class OptionalBoolean {
    private static final OptionalBoolean EMPTY = new OptionalBoolean();

    private final boolean isPresent;
    private final boolean value;

    private OptionalBoolean() {
        this.isPresent = false;
        this.value = false;
    }

    public static OptionalBoolean empty() {
        return EMPTY;
    }

    private OptionalBoolean(boolean value) {
        this.isPresent = true;
        this.value = value;
    }

    public static OptionalBoolean of(boolean value) {
        return new OptionalBoolean(value);
    }

    public boolean getAsBoolean() {
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

        return obj instanceof OptionalBoolean other
                && (isPresent && other.isPresent
                ? Boolean.compare(value, other.value) == 0
                : isPresent == other.isPresent);
    }

    @Override
    public int hashCode() {
        return isPresent ? Boolean.hashCode(value) : 0;
    }

    @Override
    public String toString() {
        return isPresent
                ? ("OptionalBoolean[" + value + "]")
                : "OptionalBoolean.empty";
    }
}