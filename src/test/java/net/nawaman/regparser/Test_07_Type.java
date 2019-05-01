package net.nawaman.regparser;

@SuppressWarnings("serial")
class RTByte extends PType {
	static public final RTByte Instance = new RTByte();
	
	@Override public String getName() { return "$byte?"; }
	
	Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1,3));
	@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
	
	@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
			PTypeProvider pProvider) {
		String S = pThisResult.getText();
		int I = Integer.parseInt(S);
		return (I >= 0) && (I <= 255);
	}
}

public class Test_07_Type {
	
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
		
		System.out.println("Type ---------------------------------------------------------");

		RegParser    RP = null;
		ParseResult PR = null;
		
		// Add the type
		PTypeProvider.Simple.getDefault().addRPType(RTByte.Instance);
		/* */
		if(!IsQuiet) System.out.println(PTypeProvider.Simple.getDefault().getType("$byte?"));
		
		System.out.println();
		System.out.println("Test 1 ----------------------------------");

		RP = RegParser.newRegParser(RPEntry._new("#Value", PTypeProvider.Simple.getDefault().getType("$byte?")));
		if(!IsQuiet) System.out.println(RP);
		
		PR = RP.parse("192");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.textOf("#Value"));
		Assert(PR.textOf("#Value"), "192");
		
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 2 ----------------------------------");
		
		@SuppressWarnings("serial")
		PTypeRef RTR_Byte = new PTypeRef() {
			@Override public String getName()  { return "$byte?"; }
		};
		RP = RegParser.newRegParser(RPEntry._new("#Value", RTR_Byte));
		if(!IsQuiet) System.out.println(RP);
		
		PR = RP.parse("192");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.textOf("#Value"));
		Assert(PR.textOf("#Value"), "192");
		
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 3 ----------------------------------");
		
		@SuppressWarnings("serial")
		PType RT_Byte = new PType() {
			@Override public String getName()  { return "$byte?"; }
			Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1,3));
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
			@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
					PTypeProvider pProvider) {
				String S = pThisResult.getText();
				int I = Integer.parseInt(S);
				return (I >= 0) && (I <= 255);
			}
		};
		RP = RegParser.newRegParser(RPEntry._new("#Value", RT_Byte));
		if(!IsQuiet) System.out.println(RP);
		
		PR = RP.parse("192");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.textOf("#Value"));
		Assert(PR.textOf("#Value"), "192");
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 4 ----------------------------------");
		
		@SuppressWarnings("serial")
		PType RT_Int_0To4 = new PType() {
			@Override public String getName()  { return "$int(0-4)?"; }
			Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1,1, Greediness.Maximum));
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
			@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
					PTypeProvider pProvider) {
				String S = pThisResult.getText();
				int I = Integer.parseInt(S);
				return (I >= 0) && (I <= 4);
			}
		};
		
		@SuppressWarnings("serial")
		PType RT_Int_5To9 = new PType() {
			@Override public String getName()  { return "$int(5-9)?"; }
			Checker Checker = RegParser.newRegParser(PredefinedCharClasses.Digit, new Quantifier(1,1, Greediness.Maximum));
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
			@Override public boolean doValidate(ParseResult pHostResult, ParseResult pThisResult, String pParam,
					PTypeProvider pProvider) {
				String S = pThisResult.getText();
				int I = Integer.parseInt(S);
				return (I >= 5) && (I <= 9);
			}
		};
		RP = RegParser.newRegParser(
				new CheckerAlternative(
					RegParser.newRegParser("#Value_Low",  RT_Int_0To4),
					RegParser.newRegParser("#Value_High", RT_Int_5To9)
				),
				Quantifier.ZeroOrMore_Maximum
			);
		if(!IsQuiet) System.out.println(RP);
		
		PR = RP.parse("3895482565");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.textsOf("#Value_Low").toString());
		if(!IsQuiet) System.out.println(PR.textsOf("#Value_High").toString());

		Assert(Util.toString(PR.textsOf("#Value_Low")),  "[3,4,2]");
		Assert(Util.toString(PR.textsOf("#Value_High")), "[8,9,5,8,5,6,5]");
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(PR.toString());
		
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 6 ----------------------------------");
		@SuppressWarnings("serial")
		PType RT_Block = new PType() {
			@Override public String getName()  { return "block"; }
			Checker Checker = RegParser.newRegParser(	// <([^<]|!block!)*+>
				new CharSingle('<'),
				new CheckerAlternative(
					RegParser.newRegParser("#Other", RegParser.newRegParser(new CharNot(new CharSet("<>")), Quantifier.OneOrMore)),
					RegParser.newRegParser("#SubBlock", new PTypeRef.Simple("block"))
				), Quantifier.ZeroOrMore_Minimum,
				new CharSingle('>')
			);
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) {
				return this.Checker;
			}
		};
		
		// Add the type
		PTypeProvider.Simple.getDefault().addRPType(RT_Block);
		
		if(!IsQuiet) System.out.println(PTypeProvider.Simple.getDefault().getType("block"));
		RP = RegParser.newRegParser(RPEntry._new("#Block", RT_Block));
		if(!IsQuiet) System.out.println(RP);
		
		PR = RP.parse("< < - >  < : > >");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.getText());
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(PR.toString());

		PR = RP.parse("<123456>");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.getText());
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(PR.toString());

		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 7 ----------------------------------");
		
		// Add the type
		PTypeProvider.Simple.getDefault().addRPType(PTBackRef.BackRef_Instance);
		PTypeProvider.Simple.getDefault().addRPType(PTBackRefCI.BackRefCI_Instance);

		RP = RegParser.newRegParser(
				RPEntry._new("#X", new PTypeRef.Simple("$byte?")),
				new CharSingle('x'),
				RegParser.newRegParser(new PTypeRef.Simple(PTBackRef.BackRef_Instance.getName(), "#X"))
			);
		if(!IsQuiet) System.out.println(RP);

		PR = RP.parse("56x56");
		if(!IsQuiet) System.out.println(PR);
		if(!IsQuiet) System.out.println(PR.getText());
		

		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 8 ----------------------------------");
		
		// Add the type
		PTypeProvider.Simple.getDefault().addRPType(PTBackRef.BackRef_Instance);
		RP = RegParser.newRegParser(
				new CharSingle('<'),
				RPEntry._new("Begin",
					RegParser.newRegParser(new CharUnion(new CharRange('a','z'), new CharRange('A','Z')), Quantifier.OneOrMore)
				),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle('>'),
				PredefinedCharClasses.Any, Quantifier.ZeroOrMore_Minimum,
				RegParser.newRegParser(
					"#End",
					RegParser.newRegParser(
						new WordChecker("</"),
						RPEntry._new("#EndTag", new PTypeRef.Simple(PTBackRef.BackRef_Instance.getName(), "Begin")),
						new CharSingle('>')
					)
				)
			);
		if(!IsQuiet) System.out.println(RP);

		PR = RP.parse("<tag> <br /> </tag>");
		if(!IsQuiet) System.out.println(PR);
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(PR.toString());


		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println();
		System.out.println("Test 9 ----------------------------------");
		@SuppressWarnings("serial")
		PType RT_Identifier = new PType() {
			@Override public String getName()  { return "Identifier"; }
			Checker Checker = RegParser.newRegParser(
				new CharUnion(PredefinedCharClasses.Alphabet, new CharSingle('_')),
				new CharUnion(PredefinedCharClasses.Alphabet, new CharSingle('_'), PredefinedCharClasses.Digit),
				Quantifier.ZeroOrMore
			);
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
		};
		
		@SuppressWarnings("serial")
		PType RT_StringLiteral = new PType() {
			@Override public String getName()  { return "StringValue"; }
			Checker Checker = RegParser.newRegParser(
				new CheckerAlternative(
						RegParser.newRegParser(
						new CharSingle('\"'),
						new CheckerAlternative(
							new CharNot(new CharSingle('\"')),
							new WordChecker("\\\"")
						),
						Quantifier.ZeroOrMore_Minimum,
						new CharSingle('\"')
					),
					RegParser.newRegParser(
						new CharSingle('\''),
						new CheckerAlternative(
							new CharNot(new CharSingle('\'')),
							new WordChecker("\\\'")
						),
						Quantifier.ZeroOrMore_Minimum,
						new CharSingle('\'')
					)
				)
			);
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
		};
		
		@SuppressWarnings("serial")
		PType RT_Attribute = new PType() {
			@Override public String getName()  { return "Attribute"; }
			Checker Checker = RegParser.newRegParser(
				"#AttrName", new PTypeRef.Simple("Identifier"),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				new CharSingle('='),
				PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
				"#AttrValue", new PTypeRef.Simple("StringValue")
			);
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
		};
		
		@SuppressWarnings("serial")
		PType RT_Tag = new PType() {
			@Override public String getName()  { return "Tag"; }
			Checker Checker = RegParser.newRegParser(
					new CharSingle('<'),
					RPEntry._new("$Begin",
						RegParser.newRegParser(new CharUnion(new CharRange('a','z'), new CharRange('A','Z')), Quantifier.OneOrMore)
					),
					PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
					RegParser.newRegParser(
						RegParser.newRegParser(
							PredefinedCharClasses.Blank, Quantifier.ZeroOrMore,
							"$Attr", new PTypeRef.Simple("Attribute"),
							PredefinedCharClasses.Blank, Quantifier.ZeroOrMore
						), Quantifier.ZeroOrMore
					),
					new CheckerAlternative(
						RegParser.newRegParser(
							new CharSingle('>'),
							new CheckerAlternative(
									RegParser.newRegParser("#Other", RegParser.newRegParser(new CharNot(new CharSet("<>")), Quantifier.OneOrMore)),
									RegParser.newRegParser("#SubBlock", new PTypeRef.Simple("Tag"))
							), Quantifier.ZeroOrMore_Minimum,
							RegParser.newRegParser(
								"#End",
								RegParser.newRegParser(
									new WordChecker("</"),
									RPEntry._new("#EndTag", new PTypeRef.Simple(PTBackRefCI.BackRefCI_Instance.getName(), "$Begin")),
									new CharSingle('>')
								)
							)
						),
						RegParser.newRegParser(
							new WordChecker("/>")
						)
					)
				);
			@Override public Checker getChecker(ParseResult pHostResult, String pParam, PTypeProvider pProvider) { return this.Checker; }
		};
		
		// Add the type
		PTypeProvider.Extensible MyRPTP = new PTypeProvider.Extensible();
		MyRPTP.addRPType(RT_Tag);
		MyRPTP.addRPType(RT_Identifier);
		MyRPTP.addRPType(RT_StringLiteral);
		MyRPTP.addRPType(RT_Attribute);
		
		if(!IsQuiet) System.out.println(PTypeProvider.Simple.getDefault().getType("tag"));
		RP = RegParser.newRegParser(RPEntry._new("#Block", RT_Tag));
		if(!IsQuiet) System.out.println(RP);

		PR = RP.parse("<tag attr1='value1' attr2='value2'> <p>Something <b> Some more <br /> thing </b> </p> </Tag>", MyRPTP);
		if(!IsQuiet) System.out.println(PR);
		
		if(!IsQuiet) System.out.println();
		if(!IsQuiet) System.out.println(PR.toString());
		
		/* */
		System.out.println();
		System.out.println("All Success.");
	}
}
