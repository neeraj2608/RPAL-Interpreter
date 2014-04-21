package com.neeraj2608.rpalinterpreter.csem;

import java.util.HashMap;
import java.util.Map;

public class Environment{
  Environment parent;
  Map nameValueMap;
  
  public Environment(){
    nameValueMap = new HashMap<String,String>();
  }

  public Environment getParent(){
    return parent;
  }

  public void setParent(Environment parent){
    this.parent = parent;
  }
}
