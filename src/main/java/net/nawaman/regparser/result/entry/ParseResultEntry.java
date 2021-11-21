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
	
	static public ParseResultEntry newEntry(int endPosition) {
		return new ParseResultEntry(endPosition);
	}
	
	static public ParseResultEntry newEntry(int endPosition, RPEntry entry) {
		return (entry == null)
		        ? new ParseResultEntry               (endPosition)
		        : new ParseResultEntryWithParserEntry(endPosition, entry);
	}
	
	static public ParseResultEntry newEntry(int endPosition, ParseResult subResult) {
		return (subResult == null)
		        ? new ParseResultEntry       (endPosition)
		        : new ParseResultEntryWithSub(endPosition, subResult);
	}
	
	static public ParseResultEntry newEntry(int endPosition, RPEntry entry, ParseResult subResult) {
		if (entry == null) {
			return (subResult == null)
			        ? new ParseResultEntry       (endPosition)
			        : new ParseResultEntryWithSub(endPosition, subResult);
		} else {
			return (subResult == null)
			        ? new ParseResultEntryWithParserEntry      (endPosition, entry)
			        : new ParseResultEntryWithParserEntryAndSub(endPosition, entry, subResult);
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
	
	public final PTypeRef typeRef() {
		return hasParserEntry()
		        ? parserEntry().typeRef()
		        : null;
	}
	
	public final PType type() {
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