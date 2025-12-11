package nanson;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Caches data
 *
 * @param <T> is the type of the object being cached.
 * @author Nanson Chen
 * @version 2.0
 */
public class Cacher<T> {
    /**
     * Supplier used to obtain the cached value when the cache is invalid.
     */
    private final Supplier<? extends T> supplier;

    /**
     * The cached data value.
     */
    private T data;

    /**
     * Whether the cached value is currently valid.
     */
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
