package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.result.ParseResult;


public class PREntryWithSub extends PREntry {
    
    static private final long serialVersionUID = 3256954552565455451L;
    
    private final ParseResult subResult;
    
    PREntryWithSub(int endPosition, ParseResult subResult) {
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
