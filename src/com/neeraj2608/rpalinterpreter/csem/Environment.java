package com.neeraj2608.rpalinterpreter.csem;

import java.util.HashMap;
import java.util.Map;

import com.neeraj2608.rpalinterpreter.ast.ASTNode;

public class Environment{
  private Environment parent;
  private Map<String, ASTNode> nameValueMap;
  
  public Environment(){
    nameValueMap = new HashMap<String, ASTNode>();
  }

  public Environment getParent(){
    return parent;
  }

  public void setParent(Environment parent){
    this.parent = parent;
  }
  
  public ASTNode lookup(String key){
    ASTNode retValue = null;
    Map<String, ASTNode> map = nameValueMap;
    while(map!=null){
      retValue = map.get(key);
      if(retValue!=null || parent==null)
        break;
      map = parent.nameValueMap;
    }
    
    if(retValue==null)
      throw new EvaluationException("Identifier "+key+" not found in environment");
    
    return retValue;
  }
  
  public void addMapping(String key, ASTNode value){
    nameValueMap.put(key, value);
  }
}
