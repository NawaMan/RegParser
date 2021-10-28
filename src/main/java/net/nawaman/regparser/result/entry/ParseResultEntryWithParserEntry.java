package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RPEntry;


public class ParseResultEntryWithParserEntry extends ParseResultEntry {
    
    static private final long serialVersionUID = 2254558458854566555L;
    
    public ParseResultEntryWithParserEntry(int pEndPosition, RPEntry pEntry) {
        super(pEndPosition);
        this.RPEntry = pEntry;
    }
    
    RPEntry RPEntry;
    
    @Override
    public boolean hasRPEntry() {
        return (this.RPEntry != null);
    }
    
    @Override
    public RPEntry parserEntry() {
        return this.RPEntry;
    }
}