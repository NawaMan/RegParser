package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RPEntry;
import net.nawaman.regparser.result.ParseResult;


public class ParseResultEntryWithParserEntryAndSub extends ParseResultEntryWithParserEntry {
    
    static private final long serialVersionUID = 2548545452415545545L;
    
    public ParseResultEntryWithParserEntryAndSub(int pEndPosition, RPEntry pEntry, ParseResult pSubResult) {
        super(pEndPosition, pEntry);
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