package net.nawaman.regparser.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The stream type that we will be using in this module.
 * 
 * @param <T>  the data type of the stream.
 */
public interface IStream<T> extends ExtensibleStream<T> {
	
	public static <DATA> IStream<DATA> empty() {
		return new IStream.Simple<DATA>(Stream.empty());
	}
	
	@SafeVarargs
	public static <DATA> IStream<DATA> forStream(Stream<DATA>... originals) {
		return new IStream.Simple<DATA>(Stream.of(originals).flatMap(each -> each));
	}
	
	@SafeVarargs
	public static <DATA> IStream<DATA> of(DATA... values) {
		return new IStream.Simple<DATA>(Stream.of(values));
	}
	
	public static <DATA> IStream<DATA> forCollection(Collection<DATA> values) {
		return new IStream.Simple<DATA>(values.stream());
	}
	
	public static class Simple<DATA> implements IStream<DATA> {
		
		private final Stream<DATA> original;
		
		public Simple(Stream<DATA> original) {
			this.original = original;
		}
		
		@Override
		public Stream<DATA> stream() {
			return original;
		};
		
		@Override
		public String toString() {
			return toList().toString();
		}
	}
	
	public default List<T> toList() {
		return collect(Collectors.toList());
	}
	
	public default IStream<T> concatWith(Stream<T> tail) {
		if (tail == null) {
			return this;
		}
		
		return IStream.forStream(Stream.concat(this, tail));
	}
	
}
