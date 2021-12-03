package net.nawaman.regparser.compiler;

import static net.nawaman.regparser.PredefinedCharClasses.Any;
import static net.nawaman.regparser.RegParser.newRegParser;

import net.nawaman.regparser.Checker;
import net.nawaman.regparser.ParserType;
import net.nawaman.regparser.ParserTypeProvider;
import net.nawaman.regparser.ParserTypeRef;
import net.nawaman.regparser.checkers.CheckerAlternative;
import net.nawaman.regparser.checkers.CheckerNot;
import net.nawaman.regparser.checkers.WordChecker;
import net.nawaman.regparser.result.ParseResult;

public class RPCommentParserType extends ParserType {
	
	private static final long serialVersionUID = -3778591162776321408L;
	
	public static String              name     = "Comment";
	public static RPCommentParserType instance = new RPCommentParserType();
	public static ParserTypeRef       typeRef  = instance.typeRef();
	
	@Override
	public String name() {
		return name;
	}
	
	private final Checker checker;
	
	public RPCommentParserType() {
		this.checker = new CheckerAlternative(
		        newRegParser(
		            new WordChecker("/*"),
		            new CheckerNot(new WordChecker("*/")).zeroOrMore(),
		            new WordChecker("*/")),
		        newRegParser(
		            new WordChecker("(*"),
		            new CheckerNot(new WordChecker("*)")).zeroOrMore(),
		            new WordChecker("*)")),
		        newRegParser(
		            new WordChecker("//"),
	                new CheckerNot(new CheckerAlternative(new WordChecker("\n"), Any.zero())).zeroOrMore(),
	                new CheckerAlternative(new WordChecker("\n"), Any.zero())));
	}
	
	@Override
	public Checker checker(ParseResult hostResult, String parameter, ParserTypeProvider typeProvider) {
		return checker;
	}
}
