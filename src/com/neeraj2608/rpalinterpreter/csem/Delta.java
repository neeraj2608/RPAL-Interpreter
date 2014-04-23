package com.neeraj2608.rpalinterpreter.csem;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.neeraj2608.rpalinterpreter.ast.ASTNode;
import com.neeraj2608.rpalinterpreter.ast.ASTNodeType;

public class Delta extends ASTNode{
  private List<String> boundVars;
  private Environment currentEnv; //the environment in which this delta's bindings live
                                  //refers back (via the parent field) to the environment
                                  //in effect when this delta was created
  private Stack<ASTNode> body;
  private int index;
  
  public Delta(){
    setType(ASTNodeType.DELTA);
    boundVars = new ArrayList<String>();
  }
  
  //used if the program evaluation results in a partial application
  @Override
  public String getValue(){
    return "[lambda closure: "+boundVars.get(0)+": "+index+"]";
  }

  public List<String> getBoundVars(){
    return boundVars;
  }
  
  public void addBoundVars(String boundVar){
    boundVars.add(boundVar);
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

  public void setIndex(int index){
    this.index = index;
  }
}
