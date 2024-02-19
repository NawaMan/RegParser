package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.result.ParseResult;

/**
 * Parse result entry with sub result.
 */
public class ParseResultEntryWithSubResult extends ParseResultEntry {
    
    private static final long serialVersionUID = 3256954552565455451L;
    
    private final ParseResult subResult;
    
    ParseResultEntryWithSubResult(int endPosition, ParseResult subResult) {
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
