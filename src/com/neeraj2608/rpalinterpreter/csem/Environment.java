package com.neeraj2608.rpalinterpreter.csem;

import java.util.HashMap;
import java.util.Map;

public class Environment{
  private Environment parent;
  private Map<String, String> nameValueMap; //note that the String -> String mapping is independent of what we save on the value stack
                                            //in the CSE machine (ASTNodes). When we encounter a gamma and apply a rator to a rand,
                                            //we can, depending on the rator, either use the rand ASTNode as it is or look at its value field.
                                            //For instance,
                                            //1. If the rator is 'isInteger', we can check if the rand ASTNode's type is indeed
                                            //   ASTNodeType.INTEGER
                                            //2. If the rator is a Delta, we extract the value of the rand ASTNode and bind it to the Delta's
                                            //   boundVar field in the Delta's currentEnv
  
  public Environment(){
    nameValueMap = new HashMap<String,String>();
  }

  public Environment getParent(){
    return parent;
  }

  public void setParent(Environment parent){
    this.parent = parent;
  }
  
  public String lookup(String key){
    String retValue = null;
    Map<String, String> map = nameValueMap;
    while(map!=null){
      retValue = (String) map.get(key);
      if(retValue!=null)
        break;
      map = parent.nameValueMap;
    }
    
    if(retValue==null)
      throw new EvaluationException("Identifier "+key+" not found in environment");
    
    return retValue;
  }
}
