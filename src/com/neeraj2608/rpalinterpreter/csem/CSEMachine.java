package com.neeraj2608.rpalinterpreter.csem;

import java.util.Stack;

import com.neeraj2608.rpalinterpreter.ast.AST;
import com.neeraj2608.rpalinterpreter.ast.ASTNode;
import com.neeraj2608.rpalinterpreter.ast.ASTNodeType;

public class CSEMachine{
  
  private Stack<ASTNode> controlStack;
  private Stack<ASTNode> valueStack;
  private Environment envInEffect;
  private Delta currentDelta;
  
  public CSEMachine(AST ast){
    if(!ast.isStandardized())
      throw new EvaluationException("AST has NOT been standardized!");
    updateCurrentDelta(ast.createDelta());
    valueStack = new Stack<ASTNode>();
  }

  private void updateCurrentDelta(Delta node){
    currentDelta = node;
    initControlStackAndEnvironment();
  }

  public String evaluateProgram(){
    processControlStack();
    return valueStack.pop().getValue(); //RULE 5
  }

  private void initControlStackAndEnvironment(){
    controlStack = currentDelta.getBody();
    envInEffect = currentDelta.getCurrentEnv();
  }

  private void processControlStack(){
    while(!controlStack.isEmpty()){
      processCurrentNode(controlStack.pop());
    }
    if(currentDelta.getPreviousDelta()!=null){
      updateCurrentDelta(currentDelta.getPreviousDelta());
      processControlStack();
    }
  }

  private void processCurrentNode(ASTNode node){
    switch(node.getType()){
      case IDENTIFIER:
        handleIdentifiers(node);
        break;
      case GAMMA:
        applyGamma();
        break;
      default:
        // Although we use ASTNodes, a CSEM will only ever see a subset of all possible ASTNodeTypes.
        // These are the types that are NOT standardized away into lambdas and gammas. E.g. types
        // such as LET, WHERE, WITHIN, SIMULTDEF etc will NEVER be encountered by the CSEM
        valueStack.push(node);
        break;
    }
  }

  //RULE 3
  private void applyGamma(){
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();
    
    if(rator.getType()==ASTNodeType.DELTA){
      updateCurrentDelta((Delta)rator);
      //add binding for this delta
    }
    else if(evaluateReservedIdentifiers(rator, rand))
      return;
    else if(applyBinaryOperation(rator, rand))
      return;
    else if(applyUnaryOperation(rator,rand))
      return;
    else
      throw new EvaluationException("Don't know how to evaluate "+rator.getValue());
  }
  
  // RULE 6
  private boolean applyBinaryOperation(ASTNode rator, ASTNode rand1){
    //read another rand
    return false;
  }

  // RULE 7
  private boolean applyUnaryOperation(ASTNode rator, ASTNode rand){
    return false;
  }

  private boolean evaluateReservedIdentifiers(ASTNode rator, ASTNode rand){
    switch(rator.getValue()){
      case "Isinteger":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.INTEGER);
        return true;
      case "Isstring":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.STRING);
        return true;
      case "Isdummy":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DUMMY);
        return true;
      case "Isfunction":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.DELTA);
        return true;
      case "Istruthvalue":
        if(rand.getType()==ASTNodeType.TRUE||rand.getType()==ASTNodeType.FALSE)
          pushTrueNode();
        else
          pushFalseNode();
        return true;
      case "Stern":
        stern(rand);
        return true;
      case "Stem":
        stem(rand);
        return true;
      case "Print":
      case "print": //typos
        valueStack.push(rand);
        return true;
      case "Istuple":
      case "ItoS":
      case "Order":
      case "Null":
      case "Conc":
      case "conc": //typos
        return true;
    }
    return false;
  }
  
  private void stem(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      throw new EvaluationException("Expected a string, not "+rand.getValue());
    if(rand.getValue().length()>1)
      rand.setValue(rand.getValue().substring(0,1));
    else
      rand.setValue("");
    valueStack.push(rand);
  }

  private void stern(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      throw new EvaluationException("Expected a string, not "+rand.getValue());
    if(rand.getValue().length()>1)
      rand.setValue(rand.getValue().substring(1));
    else
      rand.setValue("");
    valueStack.push(rand);
  }

  private void checkTypeAndPushTrueOrFalse(ASTNode rand, ASTNodeType type){
    if(rand.getType()==type)
      pushTrueNode();
    else
      pushFalseNode();
  }
  
  private void pushTrueNode(){
    ASTNode trueNode = new ASTNode();
    trueNode.setType(ASTNodeType.TRUE);
    trueNode.setValue("");
    valueStack.push(trueNode);
  }
  
  private void pushFalseNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setValue("");
    valueStack.push(falseNode);
  }

  private void handleIdentifiers(ASTNode node){
    if(isReservedIdentifier(node.getValue()))
      valueStack.push(node);
    else // RULE 1
      valueStack.push(envInEffect.lookup(node.getValue()));
  }

  // Note how this list is different from the one defined in Scanner.java
  private boolean isReservedIdentifier(String value){
    switch(value){
      case "Isinteger":
      case "Isstring":
      case "Istuple":
      case "Isdummy":
      case "Istruthvalue":
      case "Isfunction":
      case "ItoS":
      case "Order":
      case "Conc":
      case "conc": //typos
      case "Stern":
      case "Stem":
      case "Null":
      case "Print":
      case "print": //typos
        return true;
    }
    return false;
  }

}
