package net.nawaman.regparser.newway;

/**
 * Parse result for the incomplete match.
 * 
 * @author nawa
 */
public class RPIncompletedMatchedText extends RPText {
	
	final RPEndText endText;
	
	public RPIncompletedMatchedText(RPEndText endText) {
		this.endText = endText;
	}
	
	@Override
	public RPRootText root() {
		return endText.root();
	}
	
	@Override
	public RPText previous() {
		return endText.previous();
	}
	
	@Override
	public CharSequence originalText() {
		return endText.originalText();
	}
	
	@Override
	public String toString() {
		return "RPIncompletedMatchedText: Match found but do not covering the whole text: \n"
				+ endText;
	}
	
}
