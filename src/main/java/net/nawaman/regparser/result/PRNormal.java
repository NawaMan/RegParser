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
    
    protected PRNormal(int pStartPosition) {
        this(pStartPosition, null);
    }
    protected PRNormal(int pStartPosition, List<PREntry> resultEntries) {
        super(resultEntries);
        this.StartPosition = pStartPosition;
    }
    
    int StartPosition = 0;
    
    @Override
    public int startPosition() {
        return this.StartPosition;
    }
}