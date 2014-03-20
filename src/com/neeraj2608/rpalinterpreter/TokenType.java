package com.neeraj2608.rpalinterpreter;

public enum TokenType{
  IDENTIFIER(""),
  INTEGER(""),
  STRING(""),
  OPERATOR(""),
  DELETE(""),
  L_PAREN(""),
  R_PAREN(""),
  SEMICOLON(""),
  COMMA(""),
  
  LAMBDA("lambda"),
  TAU("tau"),
  CONDITIONAL("->"),
  ATX("@"),
  AND2("and"),
  FUNCT_FORM("function_form"),
  FN("fn"),
  
  IN(""),
  PERIOD("."),
  
  LET("let"),
  WHERE("where"),
  AUG("aug"),
  OR("or"),
  AND("&"),
  NOT("not"),
  
  GR("gr"),
  GE("ge"),
  LS("ls"),
  LE("le"),
  EQ("eq"),
  NE("ne"),
  
  PLUS("+"),
  MINUS("+"),
  NEG("neg"),
  TIMES("*"),
  DIVIDE("/"),
  EXPO("**"),
  BAR(""),
  GAMMA("gamma"),
  TRUE("<true>"),
  FALSE("<false"),
  XNIL("<nil>"),
  DUMMY("<dummy>"),
  WITHIN("within"),
  REC("rec"),
  EQUALS("="),
  EMPTY_PAREN("<()>"),
  YSTAR("<Y*>");
  
  private String name;

  private TokenType(String name){
    this.name = name;
  }
  
}
