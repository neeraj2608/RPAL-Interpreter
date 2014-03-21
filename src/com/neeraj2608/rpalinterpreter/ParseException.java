package com.neeraj2608.rpalinterpreter;

public class ParseException extends RuntimeException{
  private static final long serialVersionUID = 1L;
  
  public ParseException(String message){
    super("ParseException: "+message);
  }

}
