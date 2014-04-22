package com.neeraj2608.rpalinterpreter.csem;

import com.neeraj2608.rpalinterpreter.ast.ASTNode;
import com.neeraj2608.rpalinterpreter.ast.ASTNodeType;

public class Tuple extends ASTNode{
  
  public Tuple(){
    setType(ASTNodeType.TUPLE);
  }
  
  @Override
  public String getValue(){
    ASTNode childNode = getChild();
    if(childNode==null)
      return "nil";
    
    String printValue = "(";
    while(childNode.getSibling()!=null){
      printValue += childNode.getValue() + ", ";
      childNode = childNode.getSibling();
    }
    printValue += childNode.getValue() + ")";
    return printValue;
  }
}
