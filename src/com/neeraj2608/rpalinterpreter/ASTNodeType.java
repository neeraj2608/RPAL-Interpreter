package com.neeraj2608.rpalinterpreter;

public enum ASTNodeType{
  //General
  IDENTIFIER("<INT:>"),
  STRING("<STR:>"),
  INTEGER("<INT:>"),
  
  //Expressions
  LET("let"),
  LAMBDA("lambda"),
  WHERE("where"),
  
  //Tuple expressions
  TAU("tau"),
  AUG("aug"),
  CONDITIONAL("->"),
  
  //Boolean Expressions
  OR("or"),
  AND("&"),
  NOT("not"),
  GR("gr"),
  GE("ge"),
  LS("ls"),
  LE("le"),
  EQ("eq"),
  NE("ne"),
  
  //Arithmetic Expressions
  PLUS("+"),
  MINUS("-"),
  NEG("neg"),
  MULT("*"),
  DIV("/"),
  EXP("**"),
  AT("@"),
  
  //Rators and Rands
  GAMMA("gamma"),
  TRUE("<true>"),
  FALSE("<false>"),
  NIL("<nil>"),
  DUMMY("<dummy>"),
  
  //Definitions
  WITHIN("within"),
  SIMULTDEF("and"),
  REC("rec"),
  EQUAL("="),
  FCNFORM("function_form"),
  
  //Variables
  PAREN("<()>"),
  COMMA(",");
  
  //Ystar? Bar?
  
  private String printName;
  
  private ASTNodeType(String name){
    printName = name;
  }
}
