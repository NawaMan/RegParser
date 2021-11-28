package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.result.ParseResult;


/**
 * Parse result entry with sub result.
 */
public class ParseResultEntryWithParserEntryAndSubResult extends ParseResultEntryWithParserEntry {
	
	static private final long serialVersionUID = 2548545452415545545L;
	
	private final ParseResult subResult;
	
	ParseResultEntryWithParserEntryAndSubResult(int endPosition, RegParserEntry entry, ParseResult subResult) {
		super(endPosition, entry);
		this.subResult = subResult;
	}
	
	@Override
	public boolean hasSubResult() {
		return (subResult != null);
	}
	
	@Override
	public ParseResult subResult() {
		return subResult;
	}
	
}
