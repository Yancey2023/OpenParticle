package yancey.openparticle.api.util.function;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ThreeFunction<A, B, C, D> {

    D apply(A a, B b, C c);

    default <E> ThreeFunction<A, B, C, E> andThen(Function<? super D, ? extends E> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> after.apply(apply(a, b, c));
    }

}
