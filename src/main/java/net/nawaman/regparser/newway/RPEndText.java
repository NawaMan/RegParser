package net.nawaman.regparser.newway;

/**
 * ParseResult for the end text.
 * 
 * @author nawa
 */
public class RPEndText extends RPText {
	
	final RPRootText root;
	final RPText     previous;
	final int        endOffset;
	
	public RPEndText(RPText previous, int endOffset) {
		this.previous  = previous;
		this.root      = previous.root();
		this.endOffset = endOffset;
	}
	
	@Override
	public RPRootText root() {
		return root;
	}
	
	@Override
	public RPText previous() {
		return previous;
	}
	
	@Override
	public CharSequence originalText() {
		return previous.root().originalText();
	}
	
	@Override
	public String toString() {
		return previous.toString() + "\n"
				+ "offset: " + endOffset;
	}
	
}
