package net.nawaman.regparser.result;

import java.util.List;

import net.nawaman.regparser.result.entry.PREntry;
import net.nawaman.regparser.utils.IStream;


/** Node Result - For sub result*/
public class PRNode extends PRNormal {
    
    static private final long serialVersionUID = 2545684654651635454L;
    
    private ParseResult parent;
    private int         index;
    
    PRNode(int startPosition, ParseResult parseResult) {
        this(startPosition, parseResult, null);
    }
    
    private PRNode(int startPosition, ParseResult parseResult, List<PREntry> resultEntries) {
        super(startPosition, resultEntries);
        this.parent = parseResult;
        
        int index = 0;
        for (int i = 0; i < this.parent.entryCount(); i++) {
            if (startPosition == this.parent.endPositionAt(i)) {
                index = i;
                break;
            }
        }
        this.index = index;
    }
    
    void parent(ParseResult parent) {
        this.parent = parent;
    }
    
    @Override
    public ParseResult parent() {
        return this.parent;
    }
    
    @Override
    public CharSequence originalText() {
        return parent.originalText();
    }
    
    @Override
    public ParseResult duplicate() {
        // Duplication of Node cannot be optimize the same way with Temp (by avoiding recursive) 
        //     because Node hold structure that is important for verification and compilation.
        int startPosition = startPosition();
        var duplicate     = parent.duplicate();
        var entryList     = entryList();
        
        var node = new PRNode(startPosition, duplicate, entryList);
        node.index = index;
        return node;
    }
    
    // Get Element by name -----------------------------------------------------------------------
    
    @Override
    public IStream<String> names() {
        var names = super.names();
        return names.concatWith((parent == null) ? null : parent.names());
    }
    
    /**{@inheritDoc}*/
    @Override
    public String lastStringFor(String name) {
        var string = super.lastStringFor(name);
        if (string != null) {
            return string;
        }
        if (parent != null) {
            return parent.lastStringFor(name);
        }
        return null;
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