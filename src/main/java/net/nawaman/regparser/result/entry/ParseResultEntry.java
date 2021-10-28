package net.nawaman.regparser.result.entry;

import java.io.Serializable;

import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeRef;
import net.nawaman.regparser.RPEntry;
import net.nawaman.regparser.result.ParseResult;


/** The entry of the parse result */
public class ParseResultEntry implements Serializable {
    
    static private final long serialVersionUID = 3256524452125552551L;
    
    //== Factory method ================================================================================================
    
    static public ParseResultEntry newEntry(int pEndPosition) {
        return new ParseResultEntry(pEndPosition);
    }
    
    static public ParseResultEntry newEntry(int pEndPosition, RPEntry pEntry) {
        if (pEntry == null)
            return new ParseResultEntry(pEndPosition);
        else
            return new ParseResultEntryWithParserEntry(pEndPosition, pEntry);
    }
    
    static public ParseResultEntry newEntry(int pEndPosition, ParseResult pSubResult) {
        if (pSubResult == null)
            return new ParseResultEntry(pEndPosition);
        else
            return new ParseResultEntryWithSub(pEndPosition, pSubResult);
    }
    
    static public ParseResultEntry newEntry(int pEndPosition, RPEntry pEntry, ParseResult pSubResult) {
        if (pEntry == null) {
            if (pSubResult == null)
                return new ParseResultEntry(pEndPosition);
            else
                return new ParseResultEntryWithSub(pEndPosition, pSubResult);
        } else {
            if (pSubResult == null)
                return new ParseResultEntryWithParserEntry(pEndPosition, pEntry);
            else
                return new ParseResultEntryWithParserEntryAndSub(pEndPosition, pEntry, pSubResult);
        }
    }
    
    
    //== Constructor ===================================================================================================
    
    public ParseResultEntry(int pEndPosition) {
        this.EndPosition = pEndPosition;
    }
    
    int EndPosition;
    
    public int endPosition() {
        return this.EndPosition;
    }
    
    public String name() {
        return this.hasRPEntry() ? this.parserEntry().name() : null;
    }
    
    public PTypeRef getTypeRef() {
        return this.hasRPEntry() ? this.parserEntry().typeRef() : null;
    }
    
    public PType getType() {
        return this.hasRPEntry() ? this.parserEntry().type() : null;
    }
    
    public String typeName() {
        PTypeRef TR = this.getTypeRef();
        if (TR != null)
            return TR.name();
        PType T = this.getType();
        if (T != null)
            return T.name();
        return null;
    }
    
    public String parameter() {
        PTypeRef TR = this.getTypeRef();
        if (TR != null)
            return TR.parameter();
        return null;
    }
    
    public boolean hasRPEntry() {
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
        StringBuffer SB = new StringBuffer();
        SB.append("Entry").append(" { ");
        SB.append("End = ").append(this.EndPosition).append("; ");
        if (this.hasRPEntry())
            SB.append("RPEntry = ").append(this.parserEntry()).append("; ");
        if (this.hasSubResult())
            SB.append("Sub = ").append(this.subResult());
        SB.append("}");
        return SB.toString();
    }
}