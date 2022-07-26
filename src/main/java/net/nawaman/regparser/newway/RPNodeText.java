package net.nawaman.regparser.newway;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

public class RPNodeText extends RPMatchText {
	
	final RPRootText   root;
	final RPText       previous;
	final int          endOffset;
	final RPEntryIndex entryIndex;
	
	public RPNodeText(RPText previous, int endOffset, RPEntryIndex entryIndex) {
		this.previous   = previous;
		this.root       = previous.root();
		this.endOffset  = endOffset;
		this.entryIndex = requireNonNull(entryIndex);
		
		if (!(previous instanceof RPMatchText) && !(previous instanceof RPRootText)) {
			throw new IllegalStateException("Unexpected previous result: " + previous);
		}
	}
	
	@Override
	public RPRootText root() {
		return root;
	}
	
	public RPText previous() {
		return previous;
	}
	
	@Override
	public int startOffset() {
		if (previous instanceof RPMatchText) {
			return ((RPMatchText)previous).endOffset();
		}
		if (previous instanceof RPRootText) {
			return 0;
		}
		throw new IllegalStateException("Unexpected previous result: " + previous);
	}
	
	public int endOffset() {
		return endOffset;
	}

	@Override
	public RPEntryIndex parserEntryIndex() {
		return entryIndex;
	}
	
	@Override
	public CharSequence originalText() {
		return root.originalText();
	}
	
	@Override
	public String toString() {
		int offset  = startOffset();
		int length  = endOffset - offset;
		var indexes = Arrays.toString(parserEntryIndex().indexStream().toArray());
		var entry   = entry();
		return previous.toString() + "\n"
				+ "offset: " + offset + ", length: " + length + ", indexes: " + indexes + ", entry: " + entry;
	}
	
}
