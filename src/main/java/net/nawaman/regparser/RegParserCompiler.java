package net.nawaman.regparser;

import static net.nawaman.regparser.utils.Util.loadObjectsFromFile;
import static net.nawaman.regparser.utils.Util.loadObjectsFromStream;
import static net.nawaman.regparser.utils.Util.saveObjectsToFile;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

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

class RegParserCompiler {

	public  static final String regParserTypeFileExt  = "rpt";
	private static final String regParserCompilerFile = "RegParserCompiler." + regParserTypeFileExt;
	
	private static AtomicReference<ParserTypeProvider> regParserTypeProvider = new AtomicReference<>(null);

	/** Compiles a new RegParser from a RegParser code */
	public static RegParser compile(ParserTypeProvider typeProvider, String regParserText) {
		var compilerTypeProvider = regParserTypeProvider.updateAndGet(provider -> {
			if (provider == null) {
				provider = prepareTypeProvider();
			}
			return provider;
		});
		
		var compilerType   = compilerTypeProvider.type(RPRegParserParserType.name);
		var compilerParser = (RegParser)compilerType.compile(regParserText, null, null, compilerTypeProvider);
		
		// If have type provider
		if ((compilerParser != null) && (typeProvider != null)) {
			var entries
					= compilerParser
					.entries()
					.toArray(RegParserEntry[]::new);
			compilerParser = new RegParserWithDefaultTypeProvider(entries, typeProvider);
		}
		
		return compilerParser;
	}
	
	private static ParserTypeProvider prepareTypeProvider() {
		// Try to load from Resource
		try {
			var resource = ClassLoader.getSystemResourceAsStream(regParserCompilerFile);
			var objects  = loadObjectsFromStream(resource);
			return (ParserTypeProvider.Extensible) objects[0];
		} catch (Exception E) {
		}
		
		// Try to load from local file
		try {
			var objects = loadObjectsFromFile(regParserCompilerFile);
			return (ParserTypeProvider.Extensible) objects[0];
		} catch (Exception E) {
		}
		
		// Create one
		var provider = new ParserTypeProvider.Extensible();
		provider.addType(TextCaseInsensitiveParserType.instance);
		// Add the type
		provider.addType(IdentifierParserType.instance);
		provider.addType(StringLiteralParserType.instance);
		provider.addType(RPCommentParserType.instance);
		provider.addType(RPTypeParserType.instance);
		provider.addType(RPQuantifierParserType.instance);
		provider.addType(RPRegParserItemParserType.instance);
		provider.addType(RPEscapeParserType.instance);
		provider.addType(RPEscapeOctParserType.instance);
		provider.addType(RPEscapeHexParserType.instance);
		provider.addType(RPEscapeUnicodeParserType.instance);
		provider.addType(RPRangeParserType.instance);
		provider.addType(RPCharSetItemParserType.instance);
		provider.addType(RPRegParserParserType.instance);
		
		// Try to get checker of every all type in the provider so that when it is saved
		var names = provider.typeNames();
		for (var name : names) {
			var type = provider.type(name);
			type.checker(null, null, provider);
		}
		
		// Save for later use
		try {
			saveObjectsToFile(regParserCompilerFile, new Serializable[] { provider });
		} catch (Exception E) {
		}
		
		return provider;
	}
}
