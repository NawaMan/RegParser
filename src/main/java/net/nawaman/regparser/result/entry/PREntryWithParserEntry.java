package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RPEntry;


public class PREntryWithParserEntry extends PREntry {
    
    static private final long serialVersionUID = 2254558458854566555L;
    
    private final RPEntry entry;
    
    PREntryWithParserEntry(int endPosition, RPEntry entry) {
        super(endPosition);
        this.entry = entry;
    }
    
    @Override
    public boolean hasParserEntry() {
        return (this.entry != null);
    }
    
    @Override
    public RPEntry parserEntry() {
        return this.entry;
    }
}