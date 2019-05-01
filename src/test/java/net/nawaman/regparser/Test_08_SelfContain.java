package net.nawaman.regparser;
import java.util.Random;


public class Test_08_SelfContain {
	
	@SuppressWarnings("serial")
	static abstract class RPTypeTest extends PType {
		RPTypeTest(boolean pIsQuiet) { this.IsQuiet = pIsQuiet; }
		boolean IsQuiet;
	}

	static public void main(String ... Args) {
		boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));

		RegParser   RP = null;
		ParseResult PR = null;

		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test# 8 ----------------------------------");
		
		@SuppressWarnings("serial")
		PType RT_Int_0To24 = new RPTypeTest(IsQuiet) {
			@Override public String getName()  { return "int_0To24~"; }
			Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1,2, Greediness.Maximum));
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
			@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
					PTypeProvider pProvider) {
				String S = pThisResult.getText();
				int I = Integer.parseInt(S);
				if(!this.IsQuiet) System.out.println(this.getName() + ": " + S + " = "+((I >= 0) && (I <= 24))+";");
				return (I >= 0) && (I <= 24);
			}
		};
		
		@SuppressWarnings("serial")
		PType RT_Int_25To50 = new RPTypeTest(IsQuiet) {
			@Override public String getName()  { return "$int_25To50~"; }
			Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1,2, Greediness.Maximum));
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
			@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
					PTypeProvider pProvider) {
				String S = pThisResult.getText();
				int I = Integer.parseInt(S);
				if(!this.IsQuiet) System.out.println(this.getName() + ": " + S + " = "+((I >= 25) && (I <= 50))+";");
				return (I >= 25) && (I <= 50);
			}
		};
		RP = RegParser.newRegParser(
				new CheckerAlternative(
					RegParser.newRegParser("#Value_Low",  RT_Int_0To24),
					RegParser.newRegParser("#Value_High", RT_Int_25To50)
				),
				Quantifier.ZeroOrMore_Maximum
			);
		if(!IsQuiet) System.out.println(RP);
		
		String S = "";
		Random R = new Random();
		for(int i = 50; --i >= 0; ) S +=  R.nextInt(100);
		
		if(!IsQuiet) System.out.println(S);
		//PR = RP.parse("02154054657654521125432135765354362344");
		PR = RP.parse(S);
		if(PR == null) throw new AssertionError();
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(Util.toString(PR.textsOf("#Value_Low")));
		if(!IsQuiet) System.out.println(Util.toString(PR.textsOf("#Value_High")));

		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(S);
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(PR.toString());
		
		/* */
		System.out.println();
		System.out.println("All Success.");
	} 
	
}
