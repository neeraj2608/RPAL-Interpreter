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
  
  /**
   * Tries to find the binding of the given key in the mappings of this Environment's
   * inheritance hierarchy, starting with the Environment this method is invoked on.
   * Throws a runtime exception if the key was not found anywhere in this Environment's
   * inheritance hierarchy. 
   * 
   * @param key key the mapping of which to find
   * @return ASTNode that corresponds to the mapping of the key passed in as an argument
   */
  public ASTNode lookup(String key){
    ASTNode retValue = null;
    Map<String, ASTNode> map = nameValueMap;
    
    retValue = map.get(key);
    
    if(retValue!=null)
      return retValue;
    
    if(parent!=null)
      return parent.lookup(key);
    else
      throw new EvaluationException("Undeclared identifier \""+key+"\"");
  }
  
  public void addMapping(String key, ASTNode value){
    nameValueMap.put(key, value);
  }

  public String printMappings(){
    String retValue = "";
    for(String key: nameValueMap.keySet()){
      retValue += " "+key+"="+nameValueMap.get(key).getValue();
    }
    return retValue+((parent==null)?"":parent.printMappings());
  }
}
