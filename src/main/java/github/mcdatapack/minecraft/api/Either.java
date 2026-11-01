package github.mcdatapack.minecraft.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import github.mcdatapack.minecraft.Items;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public record Either<A, B>(Optional<A> a, Optional<B> b) {
    public static <A, B> Either<A, B> left(A a) {
        return new Either<>(Optional.of(a), Optional.empty());
    }
    public static <A, B> Either<A, B> right(B b) {
        return new Either<>(Optional.empty(), Optional.of(b));
    }
    public static <A, B> Either<A, B> empty() {
        return new Either<>(Optional.empty(), Optional.empty());
    }

    public <T> Optional<T> mapLeft(Function<A, T> function) {
        return a.map(function);
    }
    public <T> Optional<T> mapRight(Function<B, T> function) {
        return b.map(function);
    }

    public record BiEither<A, B, C>(Optional<A> a, Optional<B> b, Optional<C> c) {
        public static <A, B, C> BiEither<A, B, C> first(A a) {
            return new BiEither<>(Optional.of(a), Optional.empty(), Optional.empty());
        }
        public static <A, B, C> BiEither<A, B, C> second(B b) {
            return new BiEither<>(Optional.empty(), Optional.of(b), Optional.empty());
        }
        public static <A, B, C> BiEither<A, B, C> third(C c) {
            return new BiEither<>(Optional.empty(), Optional.empty(), Optional.of(c));
        }
        public static <A, B, C> BiEither<A, B, C> empty() {
            return new BiEither<>(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public <T> Optional<T> mapFirst(Function<A, T> function) {
            return a.map(function);
        }
        public <T> Optional<T> mapSecond(Function<B, T> function) {
            return b.map(function);
        }
        public <T> Optional<T> mapThird(Function<C, T> function) {
            return c.map(function);
        }
    }

    public record TriEither<A, B, C, D>(Optional<A> a, Optional<B> b, Optional<C> c, Optional<D> d) {
        public static <A, B, C, D> TriEither<A, B, C, D> first(A a) {
            return new TriEither<>(Optional.of(a), Optional.empty(), Optional.empty(), Optional.empty());
        }
        public static <A, B, C, D> TriEither<A, B, C, D> second(B b) {
            return new TriEither<>(Optional.empty(), Optional.of(b), Optional.empty(), Optional.empty());
        }
        public static <A, B, C, D> TriEither<A, B, C, D> third(C c) {
            return new TriEither<>(Optional.empty(), Optional.empty(), Optional.of(c), Optional.empty());
        }
        public static <A, B, C, D> TriEither<A, B, C, D> fourth(D d) {
            return new TriEither<>(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(d));
        }
        public static <A, B, C, D> TriEither<A, B, C, D> empty() {
            return new TriEither<>(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }

        public <T> Optional<T> mapFirst(Function<A, T> function) {
            return a.map(function);
        }
        public <T> Optional<T> mapSecond(Function<B, T> function) {
            return b.map(function);
        }
        public <T> Optional<T> mapThird(Function<C, T> function) {
            return c.map(function);
        }
        public <T> Optional<T> mapFourth(Function<D, T> function) {
            return d.map(function);
        }
    }
}