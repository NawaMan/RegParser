package net.nawaman.regparser;

import net.nawaman.regparser.PType;
import net.nawaman.regparser.PTypeProvider;
import net.nawaman.regparser.ParseResult;
import net.nawaman.regparser.Util;

public class Test_13_TypeProvider_ExclusiveMember {
	
	static public void Assert(Object pValue, Object pCorrectValue) {
		if(!Util.equal(pValue, pCorrectValue)) {
			System.out.println("It's " + Util.toString(pValue) + " but it should be "
					+ Util.toString(pCorrectValue) + ".");
			
			if((pValue instanceof String) && (pCorrectValue instanceof String)) {
				String S1 = (String)pValue;
				String S2 = (String)pCorrectValue;
				System.out.println(S1.length() + " : " + S2.length());

				for(int i = 0; i < S1.length(); i++) {
					if(S1.charAt(i) != S2.charAt(i)) System.out.println("|");
					System.out.print(S1.charAt(i));
				}
			}
			
			throw new AssertionError();
		}
	}
	
	static public void main(String ... Args) {
		
		boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
		
		ParseResult              PR  = null;		
		PTypeProvider.Extensible PTP = new PTypeProvider.Extensible();
		
		PTP.addType("Number",  "[0-9]+");
		PTP.addType("Plus",    "!Number![:WhiteSpace:]*[:+:][:WhiteSpace:]*!Number!");
		PType PT = PTP.getType("Plus");
		
		PR = PT.parse("5 + 10");
		if(!IsQuiet) System.out.println(PR);
		
		System.out.println("END --------------------------------------------------------------");
	}
	
}
