package net.nawaman.regparser.newway;

import net.nawaman.regparser.RegParserEntry;

/**
 * Parse result for the match.
 * 
 * @author nawa
 */
public abstract class RPMatchText extends RPText {
	
	public abstract RPRootText root();
	
	public abstract RPText previous();
	
	public abstract int startOffset();
	
	public abstract int endOffset();
	
	public abstract CharSequence originalText();
	
	public abstract RPEntryIndex parserEntryIndex();
	
	public final int level() {
		return parserEntryIndex().level();
	}
	
	public final int entryIndex() {
		return parserEntryIndex().index();
	}
	
	public final RegParserEntry entry() {
		return parserEntryIndex().entry();
	}
	
}
