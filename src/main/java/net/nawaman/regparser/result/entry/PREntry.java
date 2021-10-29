package net.nawaman.regparser.result.entry;

import java.io.Serializable;

import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeRef;
import net.nawaman.regparser.RPEntry;
import net.nawaman.regparser.result.ParseResult;


/** The entry of the parse result */
public class PREntry implements Serializable {
    
    static private final long serialVersionUID = 3256524452125552551L;
    
    //== Factory method ================================================================================================
    
    static public PREntry newEntry(int endPosition) {
        return new PREntry(endPosition);
    }
    
    static public PREntry newEntry(int endPosition, RPEntry entry) {
        return (entry == null)
                ? new PREntry               (endPosition)
                : new PREntryWithParserEntry(endPosition, entry);
    }
    
    static public PREntry newEntry(int endPosition, ParseResult subResult) {
        return (subResult == null)
                ? new PREntry       (endPosition)
                : new PREntryWithSub(endPosition, subResult);
    }
    
    static public PREntry newEntry(int endPosition, RPEntry entry, ParseResult subResult) {
        if (entry == null) {
            return (subResult == null)
                    ? new PREntry       (endPosition)
                    : new PREntryWithSub(endPosition, subResult);
        } else {
            return (subResult == null)
                    ? new PREntryWithParserEntry      (endPosition, entry)
                    : new PREntryWithParserEntryAndSub(endPosition, entry, subResult);
        }
    }
    
    
    //== Constructor ===================================================================================================
    
    private final int endPosition;
    
    PREntry(int endPosition) {
        this.endPosition = endPosition;
    }
    
    public int endPosition() {
        return endPosition;
    }
    
    public String name() {
        return hasParserEntry()
                ? parserEntry().name()
                : null;
    }
    
    public PTypeRef typeRef() {
        return hasParserEntry()
                ? parserEntry().typeRef()
                : null;
    }
    
    public PType type() {
        return hasParserEntry()
                ? parserEntry().type()
                : null;
    }
    
    public String typeName() {
        var typeRef = typeRef();
        if (typeRef != null) {
            return typeRef.name();
        }
        
        var type = type();
        return (type != null) ? type.name() : null;
    }
    
    public String parameter() {
        var typeRef = typeRef();
        return (typeRef != null)
                ? typeRef.parameter()
                : null;
    }
    
    public boolean hasParserEntry() {
        return false;
    }
    
    public RPEntry parserEntry() {
        return null;
    }
    
    public boolean hasSubResult() {
        return false;
    }
    
    public ParseResult subResult() {
        return null;
    }
    
    @Override
    public String toString() {
        var buffer = new StringBuffer();
        buffer.append("Entry").append(" { ");
        buffer.append("End = ").append(endPosition).append("; ");
        if (hasParserEntry()) {
            var parserEntry = parserEntry();
            buffer.append("RPEntry = ").append(parserEntry).append("; ");
        }
        if (hasSubResult()) {
            var subResult = subResult();
            buffer.append("Sub = ").append(subResult);
        }
        buffer.append("}");
        return buffer.toString();
    }
    
}