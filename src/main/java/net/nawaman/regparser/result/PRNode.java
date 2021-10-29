package net.nawaman.regparser.result;

import java.util.HashSet;
import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;


/** Node Result - For sub result*/
public class PRNode extends PRNormal {
    
    static private final long serialVersionUID = 2545684654651635454L;
    
    public PRNode(int pStartPosition, ParseResult pParseResult) {
        this(pStartPosition, pParseResult, null);
    }
    
    public PRNode(int pStartPosition, ParseResult pParseResult, List<PREntry> resultEntries) {
        super(pStartPosition, resultEntries);
        this.parent = pParseResult;
        for (int i = 0; i < this.parent.entryCount(); i++) {
            if (pStartPosition == this.parent.endPositionAt(i)) {
                this.Index = i;
                break;
            }
        }
    }
    
    ParseResult parent = null;
    int         Index  = 0;
    
    void parent(ParseResult parent) {
        this.parent = parent;
    }
    
    @Override
    public ParseResult parent() {
        return this.parent;
    }
    
    @Override
    public CharSequence originalText() {
        return this.parent.originalText();
    }
    
    @Override
    public ParseResult getDuplicate() {
        // Duplication of Node cannot be optimize the same way with Temp (by avoiding recursive) because Node hold
        //     structure that is important for verification and compilation.
        PRNode N = new PRNode(this.startPosition(), this.parent.getDuplicate(), this.entryList());
        N.Index = this.Index;
        return N;
    }
    
    // Get Element by name -----------------------------------------------------------------------
    
    @Override
    public HashSet<String> getAllNames() {
        HashSet<String> Ns = super.getAllNames();
        if (this.parent != null) {
            if (Ns == null)
                return this.parent.getAllNames();
            HashSet<String> PNs = this.parent.getAllNames();
            if (PNs != null)
                Ns.addAll(this.parent.getAllNames());
        }
        return Ns;
    }
    
    @Override
    public HashSet<String> getAllNames(String pPrefix) {
        HashSet<String> Ns = super.getAllNames(pPrefix);
        if (this.parent != null) {
            if (Ns == null)
                return this.parent.getAllNames(pPrefix);
            HashSet<String> PNs = this.parent.getAllNames(pPrefix);
            if (PNs != null)
                Ns.addAll(this.parent.getAllNames(pPrefix));
        }
        return Ns;
    }
    
    /**{@inheritDoc}*/
    @Override
    public String getLastStrMatchByName(String pName) {
        String N = super.getLastStrMatchByName(pName);
        if (N != null)
            return N;
        if (this.parent != null)
            return this.parent.getLastStrMatchByName(pName);
        return null;
    }
    
    /**{@inheritDoc}*/
    @Override
    String[] getLastStrMatchesByName(String pName) {
        String[] S_Ms = super.getLastStrMatchesByName(pName);
        String[] Ms   = new String[((S_Ms == null) ? 0 : S_Ms.length)];
        if (S_Ms != null)
            System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
        return Ms;
    }
    
    /**{@inheritDoc}*/
    @Override
    String[] getAllStrMatchesByName(String pName) {
        String[] S_Ms = super.getAllStrMatchesByName(pName);
        String[] Ms   = new String[((S_Ms == null) ? 0 : S_Ms.length)];
        if (S_Ms != null)
            System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
        return Ms;
    }
    
    /** Returns the all the match */
    @Override
    public String[][] getAllOfStrMatchesByName(String pName) {
        String[][] S_Ms = super.getAllOfStrMatchesByName(pName);
        String[][] Ms   = new String[((S_Ms == null) ? 0 : S_Ms.length)][];
        if (S_Ms != null) {
            int O = 0;
            for (int i = S_Ms.length; --i >= 0;)
                Ms[O + i] = S_Ms[i].clone();
        }
        return Ms;
    }
    
    /** Returns the last match */
    @Override
    public PREntry getLastMatchByName(String pName) {
        PREntry E = super.getLastMatchByName(pName);
        if (E != null)
            return E;
        return null;
    }
    
    /** Returns the last group of continuous match */
    @Override
    public PREntry[] getLastMatchesByName(String pName) {
        PREntry[] S_Ms = super.getLastMatchesByName(pName);
        PREntry[] Ms   = new PREntry[((S_Ms == null) ? 0 : S_Ms.length)];
        if (S_Ms != null)
            System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
        return Ms;
    }
    
    /** Returns the all the match */
    @Override
    public PREntry[] getAllMatchesByName(String pName) {
        PREntry[] S_Ms = super.getAllMatchesByName(pName);
        PREntry[] Ms   = new PREntry[((S_Ms == null) ? 0 : S_Ms.length)];
        if (S_Ms != null)
            System.arraycopy(S_Ms, 0, Ms, Ms.length - S_Ms.length, S_Ms.length);
        return Ms;
    }
    
    /** Returns the all the match */
    @Override
    public PREntry[][] getAllOfMatchesByName(String pName) {
        PREntry[][] P_Ms = (this.parent == null) ? null : this.parent.getAllOfMatchesByName(pName);
        PREntry[][] S_Ms = super.getAllOfMatchesByName(pName);
        PREntry[][] Ms   = new PREntry[((S_Ms == null) ? 0 : S_Ms.length) + ((P_Ms == null) ? 0 : P_Ms.length)][];
        if (P_Ms != null)
            for (int i = P_Ms.length; --i >= 0;)
                Ms[i] = P_Ms[i].clone();
        if (S_Ms != null) {
            int O = (P_Ms != null) ? P_Ms.length : 0;
            for (int i = S_Ms.length; --i >= 0;)
                Ms[O + i] = S_Ms[i].clone();
        }
        return Ms;
    }
}