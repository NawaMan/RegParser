package net.nawaman.regparser.newway;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.RegParserEntry;

public class RPRootEntryIndex extends RPEntryIndex {
	
	private final int       index;
	private final RegParser regParser;
	
	public RPRootEntryIndex(RegParser regParser, int index) {
		this.index     = index;
		this.regParser = regParser;
		
		if (index < 0) {
			throw new IndexOutOfBoundsException("Entry index is less than zero: "
					+ "index=[" + index + "]");
		}
		
		if (index >= regParser.getEntryCount()) {
			throw new IndexOutOfBoundsException("Entry index is greater than the total entry count: "
						+ "index=[" + index + "], "
						+ "count=[" + regParser.getEntryCount() + "]");
		}
	}
	
	@Override
	public RegParserEntry entry() {
		return regParser.getEntryAt(index);
	}
	
	@Override
	public int level() {
		return 0;
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public RPEntryIndex parent() {
		return null;
	}
	
	@Override
	public IntStream indexStream() {
		return IntStream.of(index);
	}
	
	@Override
	public Stream<RegParserEntry> stream() {
		return Stream.of(entry());
	}
	
}
