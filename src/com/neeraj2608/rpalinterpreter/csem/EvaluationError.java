package com.neeraj2608.rpalinterpreter.csem;

import com.neeraj2608.rpalinterpreter.scanner.Scanner;

public class EvaluationError{
  
  public static void printError(int sourceLineNumber, String message){
    System.out.println(Scanner.fileName+":"+sourceLineNumber+": "+message);
    System.exit(1);
  }

}
