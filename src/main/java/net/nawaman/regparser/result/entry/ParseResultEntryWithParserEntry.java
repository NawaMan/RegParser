package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RPEntry;

/**
 * Parse result entry with parser entry.
 */
public class ParseResultEntryWithParserEntry extends ParseResultEntry {
	
	static private final long serialVersionUID = 2254558458854566555L;
	
	private final RPEntry entry;
	
	ParseResultEntryWithParserEntry(int endPosition, RPEntry entry) {
		super(endPosition);
		this.entry = entry;
	}
	
	@Override
	public boolean hasParserEntry() {
		return (entry != null);
	}
	
	@Override
	public RPEntry parserEntry() {
		return entry;
	}
	
}
