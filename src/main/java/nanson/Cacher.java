package nanson;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Cacher<T> {
    private T data;
    private boolean valid = false;
    private final Supplier<? extends T> supplier;
    public Cacher(@NotNull Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public T getData() {
        if (valid)
            return data;
        data = supplier.get();
        return data;
    }

    public void invalidate() {
        valid = false;
    }
}
