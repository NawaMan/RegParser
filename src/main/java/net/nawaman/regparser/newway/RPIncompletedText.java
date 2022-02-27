package net.nawaman.regparser.newway;

public class RPIncompletedText extends RPText {
	
	final RPEndText endText;
	
	public RPIncompletedText(RPEndText endText) {
		this.endText = endText;
	}
	
	@Override
	public RPRootText root() {
		return endText.root();
	}
	
	@Override
	public RPText parent() {
		return endText.parent();
	}
	
	@Override
	public CharSequence originalText() {
		return endText.originalText();
	}
	
	@Override
	public String toString() {
		return "RPIncompletedText: Match found but do not covering the whole text: \n"
				+ endText;
	}
	
}
