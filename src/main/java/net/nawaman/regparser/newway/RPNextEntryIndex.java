package net.nawaman.regparser.newway;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.RegParserEntry;

public class RPNextEntryIndex extends RPEntryIndex {
	
	private final int          index;
	private final RPEntryIndex parent;
	
	public RPNextEntryIndex(RPEntryIndex parent, int index) {
		this.index  = index;
		this.parent = Objects.requireNonNull(parent, "`parent` entry-index cannot be null.");
		
		if (index < 0) {
			throw new IndexOutOfBoundsException("Entry index is less than zero: "
					+ "index=[" + index + "]");
		}
		
		var regParserEntry = parent.entry();
		var checker        = regParserEntry.checker();
		if (!(checker instanceof RegParser)) {
			throw new IllegalArgumentException("The parent entry is not a RegParser so it cannot have sub entry: " + checker);
		}
		
		var regParser = (RegParser)checker;
		if (index >= regParser.getEntryCount()) {
			throw new IndexOutOfBoundsException("Entry index is greater than the total entry count: "
						+ "index=[" + index + "], "
						+ "count=[" + regParser.getEntryCount() + "]");
		}
	}
	
	@Override
	public int level() {
		return parent.level() + 1;
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public RPEntryIndex parent() {
		return parent;
	}
	
	@Override
	public RegParserEntry entry() {
		var regParserEntry = parent.entry();
		var checker        = regParserEntry.checker();
		var regParser      = (RegParser)checker;
		return regParser.getEntryAt(index);
	}
	
	public IntStream indexStream() {
		return IntStream.concat(parent.indexStream(), IntStream.of(index));
	}
	
	@Override
	public Stream<RegParserEntry> stream() {
		var entry = entry();
		return Stream.concat(parent.stream(), Stream.of(entry));
	}
	
}
