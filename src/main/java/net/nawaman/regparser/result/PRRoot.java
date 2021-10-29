package net.nawaman.regparser.result;

import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;

/** Root Result */
public class PRRoot extends PRNormal {
    
    static private final long serialVersionUID = 2543546515135214354L;
    
    public PRRoot(int pStartPosition, CharSequence pOrgText) {
        this(pStartPosition, pOrgText, null);
    }
    
    public PRRoot(int pStartPosition, CharSequence pOrgText, List<PREntry> resultEntries) {
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
        PRRoot R = new PRRoot(this.startPosition(), this.OrgText, this.entryList());
        return R;
    }
}