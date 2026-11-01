package github.mcdatapack.minecraft.api;

public record Pair<T, X>(T first, X second) {
    public static <T, X> Pair<T, X> of(T first, X second) {
        return new Pair<>(first, second);
    }

    public record BiPair<T, X, Y>(T first, X second, Y third) {
        public static <T, X, Y> BiPair<T, X, Y> of(T first, X second, Y third) {
            return new BiPair<>(first, second, third);
        }
    }

    public record TriPair<T, X, Y, Z>(T first, X second, Y third, Z fourth) {
        public static <T, X, Y, Z> TriPair<T, X, Y, Z> of(T first, X second, Y third, Z fourth) {
            return new TriPair<>(first, second, third, fourth);
        }
    }
}