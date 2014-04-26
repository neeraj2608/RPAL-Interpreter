package com.neeraj2608.rpalinterpreter.csem;

import com.neeraj2608.rpalinterpreter.driver.P2;

public class EvaluationError{
  
  public static void printError(int sourceLineNumber, String message){
    System.out.println(P2.fileName+":"+sourceLineNumber+": "+message);
    System.exit(1);
  }

}
