package net.nawaman.regparser.result.entry;

import java.io.Serializable;

import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.RegParserEntry;
import net.nawaman.regparser.result.ParseResult;


/**
 * Parse result entry.
 */
public class ParseResultEntry implements Serializable {
    
    private static final long serialVersionUID = 3256524452125552551L;
    
    //== Factory method ================================================================================================
    
    static public ParseResultEntry newEntry(int endPosition) {
        return new ParseResultEntry(endPosition);
    }
    
    static public ParseResultEntry newEntry(int endPosition, RegParserEntry entry) {
        return (entry == null)
                ? new ParseResultEntry               (endPosition)
                : new ParseResultEntryWithParserEntry(endPosition, entry);
    }
    
    static public ParseResultEntry newEntry(int endPosition, ParseResult subResult) {
        return (subResult == null)
                ? new ParseResultEntry       (endPosition)
                : new ParseResultEntryWithSubResult(endPosition, subResult);
    }
    
    static public ParseResultEntry newEntry(int endPosition, RegParserEntry entry, ParseResult subResult) {
        if (entry == null) {
            return (subResult == null)
                    ? new ParseResultEntry       (endPosition)
                    : new ParseResultEntryWithSubResult(endPosition, subResult);
        } else {
            return (subResult == null)
                    ? new ParseResultEntryWithParserEntry      (endPosition, entry)
                    : new ParseResultEntryWithParserEntryAndSubResult(endPosition, entry, subResult);
        }
    }
    
    
    //== Constructor ===================================================================================================
    
    private final int endPosition;
    
    ParseResultEntry(int endPosition) {
        this.endPosition = endPosition;
    }
    
    public final int endPosition() {
        return endPosition;
    }
    
    public final String name() {
        return hasParserEntry()
                ? parserEntry().name()
                : null;
    }
    
    public final ParserTypeRef typeRef() {
        return hasParserEntry()
                ? parserEntry().typeRef()
                : null;
    }
    
    public final ParserType type() {
        return hasParserEntry()
                ? parserEntry().type()
                : null;
    }
    
    public final String typeName() {
        var typeRef = typeRef();
        if (typeRef != null)
            return typeRef.name();
        
        var type = type();
        return (type != null)
                ? type.name()
                : null;
    }
    
    public final String parameter() {
        var typeRef = typeRef();
        return (typeRef != null)
                ? typeRef.parameter()
                : null;
    }
    
    public boolean hasParserEntry() {
        return false;
    }
    
    public RegParserEntry parserEntry() {
        return null;
    }
    
    public boolean hasSubResult() {
        return false;
    }
    
    public ParseResult subResult() {
        return null;
    }
    
    @Override
    public final String toString() {
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
