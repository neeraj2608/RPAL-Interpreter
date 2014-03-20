package com.neeraj2608.rpalinterpreter;

public class Parser{
  private Scanner s;

  public Parser(Scanner s){
    this.s = s;
  }

  public void startParse(){
    Token nextToken = s.readNextToken();
    while(nextToken!=null){
      System.out.println(nextToken.getType().name()+": "+nextToken.getValue());
      nextToken = s.readNextToken();
    }
  }

  public AST buildAST(){
    startParse();
    return null;
  }

}
