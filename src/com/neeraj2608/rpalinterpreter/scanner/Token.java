package com.neeraj2608.rpalinterpreter.scanner;

/**
 * Token given by the scanner to the parser.
 * A token has a type and a value. The value is unimportant
 * for certain kinds of tokens (e.g. DELETE, L_PAREN tokens).
 * @author Raj
 *
 */
public class Token{
  private TokenType type;
  private String value;
  
  public TokenType getType(){
    return type;
  }
  
  public void setType(TokenType type){
    this.type = type;
  }
  
  public String getValue(){
    return value;
  }
  
  public void setValue(String value){
    this.value = value;
  }
}
