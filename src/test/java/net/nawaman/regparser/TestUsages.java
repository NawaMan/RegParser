package net.nawaman.regparser;

import static net.nawaman.regparser.RegParser.compile;
import static net.nawaman.regparser.TestUtils.validate;

import org.junit.Test;

import net.nawaman.regparser.types.IdentifierParserType;

public class TestUsages {
    
    @Test
    public void testDeclareVariable() {
        var typeProvider = new ParserTypeProvider.Extensible(new IdentifierParserType());
        
        var parser = compile(
                        typeProvider,
                        "var[:WhiteSpace:]+($Name:!$Identifier!)[:WhiteSpace:]+[:=:][:WhiteSpace:]+($Value:~[0-9]+~)[:;:]");
        var result = parser.parse("var v = 5;");
        validate("\n"
               + "00 => [    4] = <NoName>        :<NoType>         = \"var \"\n"
               + "01 => [    5] = $Name           :$Identifier      = \"v\"\n"
               + "02 => [    8] = <NoName>        :<NoType>         = \" = \"\n"
               + "03 => [    9] = $Value          :<NoType>         = \"5\"\n"
               + "04 => [   10] = <NoName>        :<NoType>         = \";\"",
               result);
    }
    
    @Test
    public void testDeclareArray() {
        var typeProvider = new ParserTypeProvider.Extensible(new IdentifierParserType());
        
        var parser = compile(typeProvider,
                "var"
                + "[:WhiteSpace:]+($Name:!$Identifier!)"
                + "[:WhiteSpace:]*[:=:]"
                + "[:WhiteSpace:]*[:[:]"
                + "[:WhiteSpace:]*(($Values:~[0-9]+~)?([:WhiteSpace:]*[:,:][:WhiteSpace:]*($Values:~[0-9]+~))*)"
                + "[:WhiteSpace:]*[:]:]"
                + "[:WhiteSpace:]*[:;:]");
        
        var result = parser.parse("var v = [5];");
        validate("\n"
               + "00 => [    4] = <NoName>        :<NoType>         = \"var \"\n"
               + "01 => [    5] = $Name           :$Identifier      = \"v\"\n"
               + "02 => [    9] = <NoName>        :<NoType>         = \" = [\"\n"
               + "03 => [   10] = $Values         :<NoType>         = \"5\"\n"
               + "04 => [   12] = <NoName>        :<NoType>         = \"];\"",
               result);
        
        result = parser.parse("var v = [1, 1, 2, 3, 5, 8];");
        validate("\n"
               + "00 => [    4] = <NoName>        :<NoType>         = \"var \"\n"
               + "01 => [    5] = $Name           :$Identifier      = \"v\"\n"
               + "02 => [    9] = <NoName>        :<NoType>         = \" = [\"\n"
               + "03 => [   10] = $Values         :<NoType>         = \"1\"\n"
               + "04 => [   12] = <NoName>        :<NoType>         = \", \"\n"
               + "05 => [   13] = $Values         :<NoType>         = \"1\"\n"
               + "06 => [   15] = <NoName>        :<NoType>         = \", \"\n"
               + "07 => [   16] = $Values         :<NoType>         = \"2\"\n"
               + "08 => [   18] = <NoName>        :<NoType>         = \", \"\n"
               + "09 => [   19] = $Values         :<NoType>         = \"3\"\n"
               + "10 => [   21] = <NoName>        :<NoType>         = \", \"\n"
               + "11 => [   22] = $Values         :<NoType>         = \"5\"\n"
               + "12 => [   24] = <NoName>        :<NoType>         = \", \"\n"
               + "13 => [   25] = $Values         :<NoType>         = \"8\"\n"
               + "14 => [   27] = <NoName>        :<NoType>         = \"];\"", result);
        validate("v",
                 result.textOf("$Name"));
        validate("[1, 1, 2, 3, 5, 8]",
                 result.textsOf("$Values"));
    }
    
}
