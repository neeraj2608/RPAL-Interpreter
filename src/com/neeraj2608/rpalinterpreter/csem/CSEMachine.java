package com.neeraj2608.rpalinterpreter.csem;

import java.util.Stack;

import com.neeraj2608.rpalinterpreter.ast.AST;
import com.neeraj2608.rpalinterpreter.ast.ASTNode;

public class CSEMachine{
  
  private Stack<ASTNode> controlStack;
  private Environment currentEnv;
  private Delta currentDelta;
  
  public CSEMachine(AST ast){
    if(!ast.isStandardized())
      throw new EvaluationException("AST has NOT been standardized!");
    currentDelta = ast.createDelta();
  }

  public String evaluateProgram(){
    controlStack = currentDelta.getBody();
    currentEnv = currentDelta.getCurrentEnv();
    return null;
  }

}
