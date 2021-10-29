package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RPEntry;


public class PREntryWithParserEntry extends PREntry {
    
    static private final long serialVersionUID = 2254558458854566555L;
    
    public PREntryWithParserEntry(int pEndPosition, RPEntry pEntry) {
        super(pEndPosition);
        this.RPEntry = pEntry;
    }
    
    RPEntry RPEntry;
    
    @Override
    public boolean hasParserEntry() {
        return (this.RPEntry != null);
    }
    
    @Override
    public RPEntry parserEntry() {
        return this.RPEntry;
    }
}