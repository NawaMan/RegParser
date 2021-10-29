package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RPEntry;
import net.nawaman.regparser.result.ParseResult;


public class PREntryWithParserEntryAndSub extends PREntryWithParserEntry {
    
    static private final long serialVersionUID = 2548545452415545545L;
    
    private final ParseResult subResult;
    
    PREntryWithParserEntryAndSub(int endPosition, RPEntry entry, ParseResult subResult) {
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
