package net.nawaman.regparser.newway;

public class RPEndText extends RPText {
	
	final RPRootText root;
	final RPText     parent;
	final int        endOffset;
	
	public RPEndText(RPText parent, int endOffset) {
		this.parent    = parent;
		this.root      = parent.root();
		this.endOffset = endOffset;
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
	
	@Override
	public String toString() {
		return parent.toString() + "\n"
				+ "offset: " + endOffset;
	}
	
}
