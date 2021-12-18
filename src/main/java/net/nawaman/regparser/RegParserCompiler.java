package net.nawaman.regparser;

import java.io.Serializable;
import java.util.Set;

import net.nawaman.regparser.compiler.RPCharSetItemParserType;
import net.nawaman.regparser.compiler.RPCommentParserType;
import net.nawaman.regparser.compiler.RPEscapeHexParserType;
import net.nawaman.regparser.compiler.RPEscapeOctParserType;
import net.nawaman.regparser.compiler.RPEscapeParserType;
import net.nawaman.regparser.compiler.RPEscapeUnicodeParserType;
import net.nawaman.regparser.compiler.RPQuantifierParserType;
import net.nawaman.regparser.compiler.RPRangeParserType;
import net.nawaman.regparser.compiler.RPRegParserItemParserType;
import net.nawaman.regparser.compiler.RPRegParserParserType;
import net.nawaman.regparser.compiler.RPTypeParserType;
import net.nawaman.regparser.types.IdentifierParserType;
import net.nawaman.regparser.types.StringLiteralParserType;
import net.nawaman.regparser.types.TextCaseInsensitiveParserType;
import net.nawaman.regparser.utils.Util;

public class RegParserCompiler {

	public static final String RegParserTypeExt = "rpt";
	
	private static ParserTypeProvider.Extensible RPTProvider       = null;
	private static String                        RegParserCompiler = "RegParserCompiler." + RegParserTypeExt;

	/** Compiles a new RegParser from a RegParser code */
	public static RegParser compile(ParserTypeProvider typeProvider, String regParserText) {
		boolean IsToSave = true;
		
		if (RPTProvider == null) {
			// Try to load from Resource
			try {
				ParserTypeProvider PT = (ParserTypeProvider.Extensible) (Util
				        .loadObjectsFromStream(ClassLoader.getSystemResourceAsStream(RegParserCompiler))[0]);
				RPTProvider = (ParserTypeProvider.Extensible) PT;
				IsToSave    = false;
			} catch (Exception E) {
			}
			
			// Try to load from local file
			if (RPTProvider == null) {
				try {
					ParserTypeProvider PT = (ParserTypeProvider.Extensible) Util
					        .loadObjectsFromFile(RegParserCompiler)[0];
					RPTProvider = (ParserTypeProvider.Extensible) PT;
					IsToSave    = false;
				} catch (Exception E) {
				}
			}
			
			// Try to create one
			if (RPTProvider == null) {
				RPTProvider = new ParserTypeProvider.Extensible();
				RPTProvider.addType(TextCaseInsensitiveParserType.instance);
				// Add the type
				RPTProvider.addType(IdentifierParserType.instance);
				RPTProvider.addType(StringLiteralParserType.instance);
				RPTProvider.addType(RPCommentParserType.instance);
				RPTProvider.addType(RPTypeParserType.instance);
				RPTProvider.addType(RPQuantifierParserType.instance);
				RPTProvider.addType(RPRegParserItemParserType.instance);
				RPTProvider.addType(RPEscapeParserType.instance);
				RPTProvider.addType(RPEscapeOctParserType.instance);
				RPTProvider.addType(RPEscapeHexParserType.instance);
				RPTProvider.addType(RPEscapeUnicodeParserType.instance);
				RPTProvider.addType(RPRangeParserType.instance);
				RPTProvider.addType(RPCharSetItemParserType.instance);
				RPTProvider.addType(RPRegParserParserType.instance);
			}
		} else
			IsToSave = false;
		
		ParserType RPT = RPTProvider.type(RPRegParserParserType.name);
		RegParser  RP  = (RegParser) (RPT.compile(regParserText, null, null, RPTProvider));
		
		// If have type provider
		if ((RP != null) && (typeProvider != null)) {
			RegParserEntry[] Es = RP.entries().toArray(RegParserEntry[]::new);
			RP = new RegParser.WithDefaultTypeProvider(Es, typeProvider);
		}
		
		if (IsToSave) {
			// Try to get checker of every all type in the provider so that when it is saved
			Set<String> Ns = RPTProvider.typeNames();
			for (String N : Ns) {
				ParserType RPType = RPTProvider.type(N);
				RPType.checker(null, null, RPTProvider);
			}
			
			// Save for later use
			try {
				Util.saveObjectsToFile(RegParserCompiler, new Serializable[] { RPTProvider });
			} catch (Exception E) {
			}
		}
		
		return RP;
	}
}
