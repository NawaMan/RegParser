package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.result.ParseResult;


public class ParseResultEntryWithSub extends ParseResultEntry {
	
	static private final long serialVersionUID = 3256954552565455451L;
	
	private final ParseResult subResult;
	
	ParseResultEntryWithSub(int endPosition, ParseResult subResult) {
		super(endPosition);
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
