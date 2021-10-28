package net.nawaman.regparser.result;

import java.util.List;

import net.nawaman.regparser.result.entry.ParseResultEntry;


/**
 * This class is a normal parse result.
 * 
 * @author nawa
 */
abstract public class ParseResultNormal extends ParseResult {
    
    static private final long serialVersionUID = 4121353565468546546L;
    
    protected ParseResultNormal(int pStartPosition) {
        this(pStartPosition, null);
    }
    protected ParseResultNormal(int pStartPosition, List<ParseResultEntry> resultEntries) {
        super(resultEntries);
        this.StartPosition = pStartPosition;
    }
    
    int StartPosition = 0;
    
    @Override
    public int startPosition() {
        return this.StartPosition;
    }
}