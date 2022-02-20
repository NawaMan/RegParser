package net.nawaman.regparser.newway;

import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.RegParser;
import net.nawaman.regparser.utils.Util;

public class NewRegParser {
	
	public static void main(String[] args) {
		run("Color");
		System.out.println();
		
		run("Colour");
		System.out.println();
		
		System.out.println("DONE!");
	}
	
	private static void run(String orgText) {
		long startTime = System.currentTimeMillis();
		var parser     = RegParser.compile("Colou?r");
		
		System.out.println(Util.toString(parser));
		
		var typeProvider = (ParserTypeProvider)null;
		
		long midTime = System.currentTimeMillis();
		RPText text  = new RPRootText(orgText);
		int count  = parser.getEntryCount();
		int offset = 0;
		for (int i = 0; i < count; i++) {
			var entry   = parser.getEntryAt(i);
			var checker = entry.checker();
			int length  = text.match(offset, checker, typeProvider);
			
			System.out.println(i + ": " + entry + " = " + length);
			if (length != -1) {
				text = new RPNodeText(text, offset, checker);
				offset += length;
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Start -> Mid: " + (midTime - startTime));
		System.out.println("Mid   -> End: " + (endTime - midTime));
		System.out.println(text);
		System.out.println(text.root().matches);
	}
	
}
