package net.nawaman.regparser.result;

import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;


/**
 * This class is a normal parse result.
 * 
 * @author nawa
 */
abstract public class PRNormal extends ParseResult {
    
    static private final long serialVersionUID = 4121353565468546546L;
    
    private final int startPosition;
    
    PRNormal(int startPosition, List<PREntry> resultEntries) {
        super(resultEntries);
        this.startPosition = startPosition;
    }
    
    @Override
    public final int startPosition() {
        return startPosition;
    }
    
}