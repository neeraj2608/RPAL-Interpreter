package com.neeraj2608.rpalinterpreter.scanner;

/**
 * Type of token constructed by the scanner.
 * @author Raj
 *
 */
public enum TokenType{
  IDENTIFIER,
  INTEGER,
  STRING,
  OPERATOR,
  DELETE,
  L_PAREN,
  R_PAREN,
  SEMICOLON,
  COMMA,
  RESERVED; //this is used to distinguish reserved RPAL keywords (complete list defined in Token.java)
            //from other identifiers (which are represented by the IDENTIFIER type) to simplify the
            //parser logic
}
