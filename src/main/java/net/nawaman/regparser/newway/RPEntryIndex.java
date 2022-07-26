package net.nawaman.regparser.newway;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.nawaman.regparser.RegParserEntry;

public abstract class RPEntryIndex {
	
	RPEntryIndex() {
	}
	
	public abstract int level();
	
	public abstract int index();
	
	public abstract RPEntryIndex parent();
	
	public abstract RegParserEntry entry();
	
	public abstract IntStream indexStream();
	
	public abstract Stream<RegParserEntry> stream();
	
	@Override
	public String toString() {
		return "[" + indexStream().mapToObj(i -> "" + i).collect(Collectors.joining(", ")) + "]";
	}
	
}
