package com.neeraj2608.rpalinterpreter.scanner;

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
