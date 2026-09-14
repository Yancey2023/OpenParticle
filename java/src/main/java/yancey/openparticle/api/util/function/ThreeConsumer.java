package yancey.openparticle.api.util.function;

import java.util.Objects;

@FunctionalInterface
public interface ThreeConsumer<A, B, C> {

    void accept(A a, B b, C c);

    default ThreeConsumer<A, B, C> andThen(ThreeConsumer<? super A, ? super B, ? super C> after) {
        Objects.requireNonNull(after);
        return (A a, B b, C c) -> {
            accept(a, b, c);
            after.accept(a, b, c);
        };
    }

}
