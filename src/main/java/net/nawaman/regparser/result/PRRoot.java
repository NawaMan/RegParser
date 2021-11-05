package net.nawaman.regparser.result;

import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;

/** Root Result */
public final class PRRoot extends PRNormal {
    
    static private final long serialVersionUID = 2543546515135214354L;
    
    private final CharSequence originalText;
    
    PRRoot(int startPosition, CharSequence originalText) {
        this(startPosition, originalText, null);
    }
    
    private PRRoot(int startPosition, CharSequence originalText, List<PREntry> resultEntries) {
        super(startPosition, resultEntries);
        this.originalText = originalText;
    }
    
    @Override
    public final CharSequence originalText() {
        return originalText;
    }
    
    @Override
    public final ParseResult duplicate() {
        int startPosition = startPosition();
        var entryList     = entryList();
        return new PRRoot(startPosition, originalText, entryList);
    }
    
}