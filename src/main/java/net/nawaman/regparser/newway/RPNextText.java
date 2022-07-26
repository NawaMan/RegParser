package net.nawaman.regparser.newway;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * Parse result for the match that is from the check of the previous one.
 * 
 * @author nawa
 */
public class RPNextText extends RPMatchText {
	
	final RPMatchText  previous;
	final int          length;
	
	public RPNextText(RPMatchText previous, int length) {
		this.previous   = requireNonNull(previous);
		this.length     = length;
	}
	
	@Override
	public RPRootText root() {
		return previous.root();
	}
	
	@Override
	public RPText previous() {
		return (RPText)previous;
	}
	
	@Override
	public int startOffset() {
		return previous.endOffset();
	}
	
	@Override
	public int endOffset() {
		return previous.endOffset() + length;
	}
	
	@Override
	public CharSequence originalText() {
		return previous.originalText();
	}
	
	@Override
	public RPEntryIndex parserEntryIndex() {
		return previous.parserEntryIndex();
	}
	
	@Override
	public String toString() {
		int offset  = startOffset();
		var indexes = Arrays.toString(parserEntryIndex().indexStream().toArray());
		var entry   = entry();
		return previous.toString() + "\n"
		+ "offset: " + offset + ", length: " + length + ", indexes: " + indexes + ", entry: " + entry;
	}
}
