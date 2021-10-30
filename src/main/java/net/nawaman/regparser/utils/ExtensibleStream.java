package net.nawaman.regparser.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface ExtensibleStream<T> extends Stream<T> {
    
    public Stream<T> stream();
    
    @Override
    public default Iterator<T> iterator() {
        return stream().iterator();
    }
    
    @Override
    public default Spliterator<T> spliterator() {
        return stream().spliterator();
    }
    
    @Override
    public default boolean isParallel() {
        return stream().isParallel();
    }
    
    @Override
    public default Stream<T> sequential() {
        return stream().sequential();
    }
    
    @Override
    public default Stream<T> parallel() {
        return stream().parallel();
    }
    
    @Override
    public default Stream<T> unordered() {
        return stream().unordered();
    }
    
    @Override
    public default Stream<T> onClose(Runnable closeHandler) {
        return stream().onClose(closeHandler);
    }
    
    @Override
    public default void close() {
        stream().close();
    }
    
    @Override
    public default Stream<T> filter(Predicate<? super T> predicate) {
        return stream().filter(predicate);
    }
    
    @Override
    public default <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return stream().map(mapper);
    }
    
    @Override
    public default IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream().mapToInt(mapper);
    }
    
    @Override
    public default LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream().mapToLong(mapper);
    }
    
    @Override
    public default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream().mapToDouble(mapper);
    }
    
    @Override
    public default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return stream().flatMap(mapper);
    }
    
    @Override
    public default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }
    
    @Override
    public default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }
    
    @Override
    public default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }
    
    @Override
    public default Stream<T> distinct() {
        return stream().distinct();
    }
    
    @Override
    public default Stream<T> sorted() {
        return stream().sorted();
    }
    
    @Override
    public default Stream<T> sorted(Comparator<? super T> comparator) {
        return stream().sorted(comparator);
    }
    
    @Override
    public default Stream<T> peek(Consumer<? super T> action) {
        return stream().peek(action);
    }
    
    @Override
    public default Stream<T> limit(long maxSize) {
        return stream().limit(maxSize);
    }
    
    @Override
    public default Stream<T> skip(long n) {
        return stream().skip(n);
    }
    
    @Override
    public default void forEach(Consumer<? super T> action) {
        stream().forEach(action);
    }
    
    @Override
    public default void forEachOrdered(Consumer<? super T> action) {
        stream().forEachOrdered(action);
    }
    
    @Override
    public default Object[] toArray() {
        return stream().toArray();
    }
    
    @Override
    public default <A> A[] toArray(IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }
    
    @Override
    public default T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream().reduce(identity, accumulator);
    }
    
    @Override
    public default Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream().reduce(accumulator);
    }
    
    @Override
    public default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream().reduce(identity, accumulator, combiner);
    }
    
    @Override
    public default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream().collect(supplier, accumulator, combiner);
    }
    
    @Override
    public default <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }
    
    @Override
    public default Optional<T> min(Comparator<? super T> comparator) {
        return stream().min(comparator);
    }
    
    @Override
    public default Optional<T> max(Comparator<? super T> comparator) {
        return stream().max(comparator);
    }
    
    @Override
    public default long count() {
        return stream().count();
    }
    
    @Override
    public default boolean anyMatch(Predicate<? super T> predicate) {
        return stream().anyMatch(predicate);
    }
    
    @Override
    public default boolean allMatch(Predicate<? super T> predicate) {
        return stream().allMatch(predicate);
    }
    
    @Override
    public default boolean noneMatch(Predicate<? super T> predicate) {
        return stream().noneMatch(predicate);
    }
    
    @Override
    public default Optional<T> findFirst() {
        return stream().findFirst();
    }
    
    @Override
    public default Optional<T> findAny() {
        return stream().findAny();
    }
    
}
