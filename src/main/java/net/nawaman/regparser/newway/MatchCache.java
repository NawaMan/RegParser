package net.nawaman.regparser.newway;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import net.nawaman.regparser.AsChecker;
import net.nawaman.regparser.ParserTypeProvider;

public class MatchCache {
	
	public static final int NOT_KNOWN = Integer.MIN_VALUE;
	public static final int NOT_MATCH = -1;

	private class Key implements Comparable<Key> {
		final int       offset;
		final AsChecker checker;
		
		public Key(int offset, AsChecker checker) {
			this.offset  = offset;
			this.checker = checker;
		}
		
		@Override
		public int hashCode() {
			final int prime  = 31;
			int       result = 1;
			result = prime * result + Objects.hash(checker, offset);
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			return Objects.equals(checker, other.checker) && offset == other.offset;
		}
		
		@Override
		public String toString() {
			return "Key [offset=" + offset + ", checker=" + checker + "]";
		}
		
		@Override
		public int compareTo(Key o) {
			if (this.offset != o.offset) {
				return Integer.compare(offset, o.offset);
			}
			return Integer.compare(checker.hashCode(), o.checker.hashCode());
		}
	}
	
	// TODO - replace with something more memory efficient.
	private final Map<Key, Integer> map = new TreeMap<>();
	
	int match(RPText text, int offset, AsChecker asChecker, ParserTypeProvider typeProvider) {
		var key = new Key(offset, asChecker);
		int length = map.getOrDefault(key, NOT_KNOWN);
		if (length == MatchCache.NOT_KNOWN) {
			var checker = asChecker.asChecker();
			length = checker.startLengthOf(text, offset, typeProvider);
			map.putIfAbsent(key, length);
		}
		
		return length;
	}
	
	public int size() {
		return map.size();
	}
	
	@Override
	public String toString() {
		return "MatchCache [map=" + map + "]";
	}
	
}
