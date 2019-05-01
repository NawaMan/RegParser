package net.nawaman.regparser;
import java.util.Arrays;

public class Test_06_Name {
	
	static public void Assert(Object pValue, Object pCorrectValue) {
		if(!Util.equal(pValue, pCorrectValue)) {
			System.out.println("It's " + Util.toString(pValue) + " but it should be "
					+ Util.toString(pCorrectValue) + ".");
			
			if((pValue instanceof String) && (pCorrectValue instanceof String)) {
				String S1 = (String)pValue;
				String S2 = (String)pCorrectValue;
				System.out.println(S1.length() + " : " + S2.length());
			}
			
			throw new AssertionError();
		}
	}
	
	static public void main(String ... Args) {
		boolean IsQuiet = ((Args != null) && (Args.length > 0) && (Tests.ToQuiet.equals(Args[0])));
		
		System.out.println("Name ---------------------------------------------------------");

		RegParser RP = null;
		
		System.out.println();
		System.out.println("Test 1 ----------------------------------");
		RP = RegParser.newRegParser(
				RPEntry._new(
					"#Name",
					RegParser.newRegParser(
						PredefinedCharClasses.Alphabet,
						PredefinedCharClasses.AlphabetAndDigit, Quantifier.ZeroOrMore
					)
				),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle('['),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				RPEntry._new("#Index", PredefinedCharClasses.Digit, Quantifier.OneOrMore),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle(']'),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				RPEntry._new(
					RegParser.newRegParser(
						new CharSingle('='),
						PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
						RPEntry._new(
							"#Value",
							RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)
						)
					),
					Quantifier.ZeroOrOne
				),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle(';')
			);
		if(!IsQuiet) System.out.println(RP.toString());
		
		ParseResult PR = RP.parse("Var[55] = 70;");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.getAllNames().toString());
		if(!IsQuiet) System.out.println(PR.textOf("#Name"));
		if(!IsQuiet) System.out.println(PR.textOf("#Index"));
		if(!IsQuiet) System.out.println(PR.textOf("#Value"));
		
		Assert(PR.getAllNames().contains("#Name"),  true);
		Assert(PR.getAllNames().contains("#Index"), true);
		Assert(PR.getAllNames().contains("#Value"), true);
		
		Assert(PR.textOf("#Name"),  "Var");
		Assert(PR.textOf("#Index"),   "5");
		Assert(PR.textOf("#Value"),  "70");
		
		if(!IsQuiet) System.out.println();
		PR = RP.parse("Var[55];");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.getAllNames().toString());
		if(!IsQuiet) System.out.println(PR.textOf("#Name"));
		if(!IsQuiet) System.out.println(PR.textOf("#Index"));
		if(!IsQuiet) System.out.println(PR.textOf("#Value"));
		
		Assert(PR.getAllNames().contains("#Name"),  true);
		Assert(PR.getAllNames().contains("#Index"), true);
		
		Assert(PR.textOf("#Name"),  "Var");
		Assert(PR.textOf("#Index"),   "5");
		Assert(PR.textOf("#Value"),  null);
		
		if(!IsQuiet) System.out.println();
		System.out.println("Test 2 ----------------------------------");
		RP = RegParser.newRegParser(
				new CharSingle('{'),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				RPEntry._new(
					"#Value",
					RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)
				),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				RPEntry._new(
					RegParser.newRegParser(
						new CharSingle(','),
						PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
						RPEntry._new(
							"#Value", RegParser.newRegParser(PredefinedCharClasses.Digit, Quantifier.OneOrMore)
						)
					),
					Quantifier.ZeroOrMore
				),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle(','), Quantifier.ZeroOrOne,
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle('}'),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle(';')
			);
		if(!IsQuiet) System.out.println(RP.toString());
		PR = RP.parse("{ 5, 7, 454, 5 };");
		if(!IsQuiet) System.out.println(PR);

		if(!IsQuiet) System.out.println(PR.getAllNames().toString());
		if(!IsQuiet) System.out.println(Arrays.toString(PR.textsOf("#Value")));
		if(!IsQuiet) System.out.println(Arrays.toString(PR.getAllOfStrMatchesByName("#Value")));
		
		Assert(PR.getAllNames().contains("#Value"),                    true);
		Assert(Util.toString(PR.textsOf("#Value")),                  "[5,7,454,5]");
		Assert(Util.toString(PR.getAllOfStrMatchesByName("#Value")), "[[5],[7],[454],[5]]");
		
		if(!IsQuiet) System.out.println();
		System.out.println("All Success.");
	}

}
