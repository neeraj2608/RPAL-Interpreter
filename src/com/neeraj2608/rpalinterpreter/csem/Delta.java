package com.neeraj2608.rpalinterpreter.csem;

import java.util.Stack;

import com.neeraj2608.rpalinterpreter.ast.ASTNode;
import com.neeraj2608.rpalinterpreter.ast.ASTNodeType;

public class Delta extends ASTNode{
  private String boundVar;
  private Environment currentEnv; //the environment in which this delta's bindings live
                                  //refers back (via the parent field) to the environment
                                  //in effect when this delta was created
  private Stack<ASTNode> body;
  private Delta previousDelta;
  
  public Delta(){
    setType(ASTNodeType.DELTA);
  }
  
  public String getBoundVar(){
    return boundVar;
  }
  
  //used if the program evaluation results in a partial application
  @Override
  public String getValue(){
    return "[lambda closure: "+boundVar+": "+currentEnv.lookup(boundVar)+"]";
  }
  
  public void setBoundVar(String boundVar){
    this.boundVar = boundVar;
  }
  
  public Environment getCurrentEnv(){
    return currentEnv;
  }
  
  public void setCurrentEnv(Environment currentEnv){
    this.currentEnv = currentEnv;
  }
  
  public Stack<ASTNode> getBody(){
    return body;
  }
  
  public void setBody(Stack<ASTNode> body){
    this.body = body;
  }

  public Delta getPreviousDelta(){
    return previousDelta;
  }

  public void setPreviousDelta(Delta previousDelta){
    this.previousDelta = previousDelta;
  }
}
