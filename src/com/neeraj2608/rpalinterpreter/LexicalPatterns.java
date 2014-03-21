package com.neeraj2608.rpalinterpreter;

import java.util.regex.Pattern;

public class LexicalPatterns{
  public static final Pattern LetterPattern = Pattern.compile("[a-zA-Z]");
  
  public static final Pattern IdentifierPattern = Pattern.compile("[a-zA-Z\\d_]");

  public static final Pattern DigitPattern = Pattern.compile("\\d");

  public static final String punctionRegex = "[();,]";
  public static final Pattern PunctuationPattern = Pattern.compile(punctionRegex);

  public static final String opSymbolChars = "+-/~:=|!#%_{}\"*<>.&$^\\[\\]?@";
  public static final String opSymbolCharsToEscape = "([*<>.&$^?])";
  public static final String opSymbolRegex = "[" + escapeRegEx(opSymbolChars, opSymbolCharsToEscape) + "]";
  public static final Pattern OpSymbolPattern = Pattern.compile(opSymbolRegex);
  
  public static final Pattern StringPattern = Pattern.compile("[a-zA-Z\\d"+ escapeRegEx(opSymbolChars, opSymbolCharsToEscape) +"]");
  
  public static final Pattern SpacePattern = Pattern.compile("\\s");
  
  public static final Pattern CommentPattern = Pattern.compile("[a-zA-Z\\d\\'();,\\ \\\\ \\r"+ escapeRegEx(opSymbolChars, opSymbolCharsToEscape) +"]"); //the \\r is for Windows LF; not really required since we're targeting *nix systems
  
  private static String escapeRegEx(String inputString, String charsToEscape){
    return inputString.replaceAll(charsToEscape,"\\\\\\\\$1");
  }
}
