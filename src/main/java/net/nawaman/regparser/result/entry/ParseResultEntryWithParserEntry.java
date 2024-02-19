package net.nawaman.regparser.result.entry;

import net.nawaman.regparser.RegParserEntry;

/**
 * Parse result entry with parser entry.
 */
public class ParseResultEntryWithParserEntry extends ParseResultEntry {
    
    static private final long serialVersionUID = 2254558458854566555L;
    
    private final RegParserEntry entry;
    
    ParseResultEntryWithParserEntry(int endPosition, RegParserEntry entry) {
        super(endPosition);
        this.entry = entry;
    }
    
    @Override
    public boolean hasParserEntry() {
        return (entry != null);
    }
    
    @Override
    public RegParserEntry parserEntry() {
        return entry;
    }
    
}
