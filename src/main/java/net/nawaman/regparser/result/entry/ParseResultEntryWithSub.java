package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.result.ParseResult;


public class ParseResultEntryWithSub extends ParseResultEntry {
    
    static private final long serialVersionUID = 3256954552565455451L;
    
    public ParseResultEntryWithSub(int pEndPosition, ParseResult pSubResult) {
        super(pEndPosition);
        this.SubResult = pSubResult;
    }
    
    ParseResult SubResult;
    
    @Override
    public boolean hasSubResult() {
        return (this.SubResult != null);
    }
    
    @Override
    public ParseResult subResult() {
        return this.SubResult;
    }
}