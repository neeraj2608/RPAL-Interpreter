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
    if(applyBinaryOperation(node))
      return;
    else if(applyUnaryOperation(node))
      return;
    else{
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
  }
  
  // RULE 6
  private boolean applyBinaryOperation(ASTNode rator){
    switch(rator.getType()){
      case PLUS:
      case MINUS:
      case MULT:
      case DIV:
      case EXP:
      case LS:
      case LE:
      case GR:
      case GE:
        binaryArithmeticOp(rator.getType());
        return true;
      case EQ:
      case NE:
        binaryLogicalEqNeOp(rator.getType());
        return true;
      case OR:
      case AND:
        binaryLogicalOrAndOp(rator.getType());
        return true;
      default:
        return false;
    }
  }

  private void binaryArithmeticOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getType()!=ASTNodeType.INTEGER || rand2.getType()!=ASTNodeType.INTEGER)
      throw new EvaluationException("Expected two integers; was given "+rand1.getValue()+", "+rand2.getValue());
    
    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    
    switch(type){
      case PLUS:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue())+Integer.parseInt(rand2.getValue())));
        break;
      case MINUS:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue())-Integer.parseInt(rand2.getValue())));
        break;
      case MULT:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue())*Integer.parseInt(rand2.getValue())));
        break;
      case DIV:
        result.setValue(Integer.toString(Integer.parseInt(rand1.getValue())/Integer.parseInt(rand2.getValue())));
        break;
      case EXP:
        result.setValue(Integer.toString((int)Math.pow(Integer.parseInt(rand1.getValue()), Integer.parseInt(rand2.getValue()))));
        break;
      case LS:
        if(Integer.parseInt(rand1.getValue())<Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case LE:
        if(Integer.parseInt(rand1.getValue())<=Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GR:
        if(Integer.parseInt(rand1.getValue())>Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      case GE:
        if(Integer.parseInt(rand1.getValue())>=Integer.parseInt(rand2.getValue()))
          pushTrueNode();
        else
          pushFalseNode();
        return;
      default:
        break;
    }
    valueStack.push(result);
  }
  
  private void binaryLogicalEqNeOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();
    
    if(rand1.getType()==ASTNodeType.TRUE || rand1.getType()==ASTNodeType.FALSE){
      if(rand2.getType()!=ASTNodeType.TRUE && rand2.getType()!=ASTNodeType.FALSE)
        throw new EvaluationException("Cannot compare dissimilar types; was given "+rand1.getValue()+", "+rand2.getValue());
      compareTruthValues(rand1, rand2, type);
      return;
    }
    
    if(rand1.getType()!=rand2.getType())
      throw new EvaluationException("Cannot compare dissimilar types; was given "+rand1.getValue()+", "+rand2.getValue());
    
    if(rand1.getType()==ASTNodeType.STRING)
      compareStrings(rand1, rand2, type);
    else if(rand1.getType()==ASTNodeType.INTEGER)
      compareIntegers(rand1, rand2, type);
    else
      throw new EvaluationException("Don't know how to " + type + " "+rand1.getValue()+", "+rand2.getValue());
    
  }
  
  private void compareTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(rand1.getType()==rand2.getType())
      if(type==ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else
      if(type==ASTNodeType.EQ)
        pushFalseNode();
      else
        pushTrueNode();
  }

  private void compareStrings(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(rand1.getValue().equals(rand2.getValue()))
      if(type==ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else
      if(type==ASTNodeType.EQ)
        pushFalseNode();
      else
        pushTrueNode();
  }

  private void compareIntegers(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(Integer.parseInt(rand1.getValue())==Integer.parseInt(rand2.getValue()))
      if(type==ASTNodeType.EQ)
        pushTrueNode();
      else
        pushFalseNode();
    else
      if(type==ASTNodeType.EQ)
        pushFalseNode();
      else
        pushTrueNode();
  }
  
  private void binaryLogicalOrAndOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();
    
    if((rand1.getType()==ASTNodeType.TRUE || rand1.getType()==ASTNodeType.FALSE) &&
      (rand2.getType()==ASTNodeType.TRUE || rand2.getType()==ASTNodeType.FALSE)){
      orAndTruthValues(rand1, rand2, type);
      return;
    }
    
    throw new EvaluationException("Don't know how to " + type + " "+rand1.getValue()+", "+rand2.getValue());
  }

  private void orAndTruthValues(ASTNode rand1, ASTNode rand2, ASTNodeType type){
    if(type==ASTNodeType.OR){
      if(rand1.getType()==ASTNodeType.TRUE || rand2.getType()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
    else{
      if(rand1.getType()==ASTNodeType.TRUE && rand2.getType()==ASTNodeType.TRUE)
        pushTrueNode();
      else
        pushFalseNode();
    }
  }
  
  // RULE 7
  private boolean applyUnaryOperation(ASTNode rator){
    switch(rator.getType()){
      case NOT:
        not();
        return true;
      default:
        return false;
    }
  }

  private void not(){
    ASTNode rand = valueStack.pop();
    if(rand.getType()!=ASTNodeType.TRUE && rand.getType()!=ASTNodeType.FALSE)
      throw new EvaluationException("Expecting a truthvalue; was given "+rand.getValue());
    
    if(rand.getType()==ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();
    
  }

  //RULE 3
  private void applyGamma(){
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();

    if(evaluateReservedIdentifiers(rator, rand))
      return;
    else if(rator.getType()==ASTNodeType.DELTA){
      updateCurrentDelta((Delta)rator);
      //add binding for this delta
      return;
    }
    else
      throw new EvaluationException("Don't know how to evaluate "+rator.getValue());
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
      case "Stem":
        stem(rand);
        return true;
      case "Stern":
        stern(rand);
        return true;
      case "Conc":
      case "conc": //typos
        conc(rand);
        return true;
      case "Print":
      case "print": //typos
        valueStack.push(rand);
        return true;
      case "ItoS":
        itos(rand);
        return true;
      case "Istuple":
      case "Order":
      case "Null":
        return true;
    }
    return false;
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
    trueNode.setValue("true");
    valueStack.push(trueNode);
  }

  private void pushFalseNode(){
    ASTNode falseNode = new ASTNode();
    falseNode.setType(ASTNodeType.FALSE);
    falseNode.setValue("false");
    valueStack.push(falseNode);
  }

  private void stem(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      throw new EvaluationException("Expected a string; was given "+rand.getValue());
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

  private void conc(ASTNode rand1){
    controlStack.pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getType()!=ASTNodeType.STRING || rand2.getType()!=ASTNodeType.STRING)
      throw new EvaluationException("Expected two strings; was given "+rand1.getValue()+", "+rand2.getValue());

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setValue(rand1.getValue()+rand2.getValue());
    valueStack.push(result);
  }

  private void itos(ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      throw new EvaluationException("Expected an integer; was given "+rand.getValue());

    rand.setType(ASTNodeType.STRING); //we store all values internally as strings, so nothing to do
    valueStack.push(rand);
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
