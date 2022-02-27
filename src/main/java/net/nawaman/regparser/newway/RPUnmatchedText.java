package net.nawaman.regparser.newway;

import java.util.function.Supplier;

public class RPUnmatchedText extends RPText {
	
	final Supplier<String> message;
	final RPRootText       root;
	final RPText           parent;
	final int              endOffset;
	
	public RPUnmatchedText(String message, RPText parent, int endOffset) {
		this(parent, endOffset, () -> message);
	}
	
	public RPUnmatchedText(RPText parent, int endOffset, Supplier<String> message) {
		this.parent    = parent;
		this.root      = parent.root();
		this.endOffset = endOffset;
		this.message   = message;
	}
	
	@Override
	public RPRootText root() {
		return root;
	}
	
	@Override
	public RPText parent() {
		return parent;
	}
	
	@Override
	public CharSequence originalText() {
		return parent.root().originalText();
	}
	
	public String message() {
		return message.get();
	}
	
	@Override
	public String toString() {
		return "RPUnmatchedText: " + message.get() + "\n"
				+ parent.toString() + "\n"
				+ "offset: " + endOffset;
	}
	
}
