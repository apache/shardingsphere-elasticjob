package io.elasticjob.lite.util;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A container object which may or may not contain a non-null value.
 * If a value is present, {@code isPresent()} will return {@code true} and
 * {@code get()} will return the value.
 */
public final class Optional<T> {
    /**
     * Common instance for {@code empty()}.
     */
    private static final Optional<?> EMPTY = new Optional<>();
    
    /**
     * If non-null, the value; if null, indicates no value is present.
     */
    private final T value;
    
    /**
     * Constructs an empty instance.
     */
    private Optional() {
        this.value = null;
    }
    
    /**
     * Constructs an instance with the value present.
     *
     * @param value the non-null value to be present
     * @throws NullPointerException if value is null
     */
    private Optional(final T value) {
        this.value = Objects.requireNonNull(value);
    }
    
    /**
     * Returns an empty {@code Optional} instance.  No value is present for this
     * Optional.
     * @param <T> Type of the non-existent value
     * @return an empty {@code Optional}
     */
    public static <T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }
    
    
    
    /**
     * Returns an {@code Optional} with the specified present non-null value.
     *
     * @param <T> the class of the value
     * @param value the value to be present, which must be non-null
     * @return an {@code Optional} with the value present
     * @throws NullPointerException if value is null
     */
    public static <T> Optional<T> of(final T value) {
        return new Optional<>(value);
    }
    
    /**
     * Returns an {@code Optional} describing the specified value, if non-null,
     * otherwise returns an empty {@code Optional}.
     *
     * @param <T> the class of the value
     * @param value the possibly-null value to describe
     * @return an {@code Optional} with a present value if the specified value] is non-null, otherwise an empty {@code Optional}.
     */
    public static <T> Optional<T> ofNullable(final T value) {
        if (value == null) {
            return empty();
        } else {
            return of(value);
        }
    }
    
    /**
     * If a value is present in this {@code Optional}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws NoSuchElementException if there is no value present
     *
     * @see Optional#isPresent()
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }
    
    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof Optional)) {
            return false;
        }
        
        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
    
    @Override
    public String toString() {
        return value != null
                ? String.format("Optional[%s]", value)
                : "Optional.empty";
    }
}
