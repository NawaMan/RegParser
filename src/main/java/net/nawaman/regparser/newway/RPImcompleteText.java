package net.nawaman.regparser.newway;

import java.util.function.Supplier;

public class RPImcompleteText extends RPText {
	
	private final Supplier<String> message;
	private final RPRootText       root;
	private final RPText           parent;
	private final int              endOffset;
	
	public RPImcompleteText(String message, RPText parent, int endOffset) {
		this(parent, endOffset, () -> message);
	}
	
	public RPImcompleteText(RPText parent, int endOffset, Supplier<String> message) {
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
		return "RPImcompleteText: " + message.get() + "\n"
				+ parent.toString() + "\n"
				+ "offset: " + endOffset;
	}
	
}
