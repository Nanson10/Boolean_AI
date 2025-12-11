package nanson;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Caches data
 *
 * @param <T> is the type of the object being cached.
 */
public class Cacher<T> {
    private final Supplier<? extends T> supplier;
    private T data;
    private boolean valid = false;

    /**
     * Constructs a Cacher.
     *
     * @param supplier is the supplier that gets the cached data.
     */
    public Cacher(@NotNull Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Gets the data, automatically re-gets the data if the cache was invalidated.
     *
     * @return the data.
     */
    public T getData() {
        if (valid)
            return data;
        return data = supplier.get();
    }

    /**
     * Invalidates the cache.
     */
    public void invalidate() {
        valid = false;
    }
}
