package net.nawaman.regparser.newway;

import java.util.function.Supplier;

public class RPUnmatchedText extends RPText {
	
	final Supplier<String> message;
	final RPRootText       root;
	final RPText           previous;
	final int              endOffset;
	
	public RPUnmatchedText(String message, RPText previous, int endOffset) {
		this(previous, endOffset, () -> message);
	}
	
	public RPUnmatchedText(RPText previous, int endOffset, Supplier<String> message) {
		this.previous  = previous;
		this.root      = previous.root();
		this.endOffset = endOffset;
		this.message   = message;
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
	
	public String message() {
		return message.get();
	}
	
	@Override
	public String toString() {
		return "RPUnmatchedText: " + message.get() + "\n"
				+ previous.toString() + "\n"
				+ "offset: " + endOffset;
	}
	
}
