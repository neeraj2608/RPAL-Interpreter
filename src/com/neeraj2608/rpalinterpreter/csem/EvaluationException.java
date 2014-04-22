package com.neeraj2608.rpalinterpreter.csem;

public class EvaluationException extends RuntimeException{
  private static final long serialVersionUID = 1L;
  
  public EvaluationException(String message){
    super("ERROR: "+message);
  }

}
