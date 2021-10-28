package net.nawaman.regparser.result;

import java.util.List;

import net.nawaman.regparser.result.entry.ParseResultEntry;

/** Root Result */
public class ParseResultRoot extends ParseResultNormal {
    
    static private final long serialVersionUID = 2543546515135214354L;
    
    public ParseResultRoot(int pStartPosition, CharSequence pOrgText) {
        this(pStartPosition, pOrgText, null);
    }
    
    public ParseResultRoot(int pStartPosition, CharSequence pOrgText, List<ParseResultEntry> resultEntries) {
        super(pStartPosition, resultEntries);
        this.OrgText = pOrgText;
    }
    
    CharSequence OrgText = null;
    
    @Override
    public CharSequence originalText() {
        return this.OrgText;
    }
    
    @Override
    public ParseResult getDuplicate() {
        ParseResultRoot R = new ParseResultRoot(this.startPosition(), this.OrgText, this.entryList());
        return R;
    }
}