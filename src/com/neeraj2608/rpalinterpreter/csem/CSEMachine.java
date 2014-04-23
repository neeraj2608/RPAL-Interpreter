package com.neeraj2608.rpalinterpreter.csem;

import java.util.Stack;

import com.neeraj2608.rpalinterpreter.ast.AST;
import com.neeraj2608.rpalinterpreter.ast.ASTNode;
import com.neeraj2608.rpalinterpreter.ast.ASTNodeType;

public class CSEMachine{

  private Stack<ASTNode> valueStack;
  private Delta rootDelta;

  public CSEMachine(AST ast){
    if(!ast.isStandardized())
      throw new EvaluationException("AST has NOT been standardized!");
    rootDelta = ast.createDeltas();
    valueStack = new Stack<ASTNode>();
  }

  public String evaluateProgram(){
    processControlStack(rootDelta);
    if(valueStack.peek().getType()==ASTNodeType.PRINT)
      return valueStack.pop().getValue(); //RULE 5
    else
      return "";
  }

  private void processControlStack(Delta currentDelta){
    while(!currentDelta.getBody().isEmpty())
      processCurrentNode(currentDelta, currentDelta.getBody().pop());
  }

  private void processCurrentNode(Delta currentDelta, ASTNode node){
    if(applyBinaryOperation(node))
      return;
    else if(applyUnaryOperation(node))
      return;
    else{
      switch(node.getType()){
        case IDENTIFIER:
          handleIdentifiers(currentDelta, node);
          break;
        case NIL:
        case TAU:
          createTuple(node);
          break;
        case CONDITIONAL:
          handleConditional(node);
          break;
        case GAMMA:
          applyGamma(currentDelta, node);
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
      case AUG:
        augTuples();
        return true;
      default:
        return false;
    }
  }

  private void binaryArithmeticOp(ASTNodeType type){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getType()!=ASTNodeType.INTEGER || rand2.getType()!=ASTNodeType.INTEGER)
      throw new EvaluationException("Expected two integers; was given \""+rand1.getValue()+"\", \""+rand2.getValue()+"\"");

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
        throw new EvaluationException("Cannot compare dissimilar types; was given \""+rand1.getValue()+"\", \""+rand2.getValue()+"\"");
      compareTruthValues(rand1, rand2, type);
      return;
    }

    if(rand1.getType()!=rand2.getType())
      throw new EvaluationException("Cannot compare dissimilar types; was given \""+rand1.getValue()+"\", \""+rand2.getValue()+"\"");

    if(rand1.getType()==ASTNodeType.STRING)
      compareStrings(rand1, rand2, type);
    else if(rand1.getType()==ASTNodeType.INTEGER)
      compareIntegers(rand1, rand2, type);
    else
      throw new EvaluationException("Don't know how to " + type + " \""+rand1.getValue()+"\", \""+rand2.getValue()+"\"");

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

    throw new EvaluationException("Don't know how to " + type + " \""+rand1.getValue()+"\", \""+rand2.getValue()+"\"");
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

  private void augTuples(){
    ASTNode rand1 = valueStack.pop();
    ASTNode rand2 = valueStack.pop();

    if(rand1.getType()!=ASTNodeType.TUPLE)
      throw new EvaluationException("Cannot augment a non-tuple \""+rand1.getValue()+"\"");

    ASTNode childNode = rand1.getChild();
    if(childNode==null)
      rand1.setChild(rand2);
    else{
      while(childNode.getSibling()!=null)
        childNode = childNode.getSibling();
      childNode.setSibling(rand2);
    }

    valueStack.push(rand1);
  }

  // RULE 7
  private boolean applyUnaryOperation(ASTNode rator){
    switch(rator.getType()){
      case NOT:
        not();
        return true;
      case NEG:
        neg();
        return true;
      default:
        return false;
    }
  }

  private void not(){
    ASTNode rand = valueStack.pop();
    if(rand.getType()!=ASTNodeType.TRUE && rand.getType()!=ASTNodeType.FALSE)
      throw new EvaluationException("Expecting a truthvalue; was given \""+rand.getValue()+"\"");

    if(rand.getType()==ASTNodeType.TRUE)
      pushFalseNode();
    else
      pushTrueNode();

  }

  private void neg(){
    ASTNode rand = valueStack.pop();
    if(rand.getType()!=ASTNodeType.INTEGER)
      throw new EvaluationException("Expecting a truthvalue; was given \""+rand.getValue()+"\"");

    rand.setValue(Integer.toString(-1*Integer.parseInt(rand.getValue())));
    valueStack.push(rand);

  }

  //RULE 3
  private void applyGamma(Delta currentDelta, ASTNode node){
    ASTNode rator = valueStack.pop();
    ASTNode rand = valueStack.pop();

    if(rator.getType()==ASTNodeType.DELTA){
      Delta nextDelta = (Delta) rator;
      //RULE 4
      if(nextDelta.getBoundVars().size()==1){
        nextDelta.getCurrentEnv().addMapping(nextDelta.getBoundVars().get(0), rand);
      }
      //RULE 11
      else{
        if(rand.getType()!=ASTNodeType.TUPLE)
          throw new EvaluationException("Expected a tuple; was given \""+rand.getValue()+"\"");
        
        for(int i = 0; i < nextDelta.getBoundVars().size(); i++){
          nextDelta.getCurrentEnv().addMapping(nextDelta.getBoundVars().get(i), getNthTupleChild((Tuple)rand, i+1));
        }
      }
      processControlStack(nextDelta);
      return;
    }
    else if(rator.getType()==ASTNodeType.TUPLE){
      tupleSelection((Tuple)rator, rand);
      return;
    }
    else if(evaluateReservedIdentifiers(currentDelta, rator, rand))
      return;
    else
      throw new EvaluationException("Don't know how to evaluate \""+rator.getValue()+"\"");
  }

  private boolean evaluateReservedIdentifiers(Delta currentDelta, ASTNode rator, ASTNode rand){
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
      case "Istuple":
        checkTypeAndPushTrueOrFalse(rand, ASTNodeType.TUPLE);
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
        conc(currentDelta, rand);
        return true;
      case "Print":
      case "print": //typos
        rand.setType(ASTNodeType.PRINT);
        valueStack.push(rand);
        return true;
      case "ItoS":
        itos(rand);
        return true;
      case "Order":
        order(rand);
        return true;
      case "Null":
        isNullTuple(rand);
        return true;
      default:
        return false;
    }
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
      throw new EvaluationException("Expected a string; was given \""+rand.getValue()+"\"");
    if(rand.getValue().length()>1)
      rand.setValue(rand.getValue().substring(0,1));
    else
      rand.setValue("");
    valueStack.push(rand);
  }

  private void stern(ASTNode rand){
    if(rand.getType()!=ASTNodeType.STRING)
      throw new EvaluationException("Expected a string; was given \""+rand.getValue()+"\"");
    if(rand.getValue().length()>1)
      rand.setValue(rand.getValue().substring(1));
    else
      rand.setValue("");
    valueStack.push(rand);
  }

  private void conc(Delta currentDelta,ASTNode rand1){
    currentDelta.getBody().pop();
    ASTNode rand2 = valueStack.pop();
    if(rand1.getType()!=ASTNodeType.STRING || rand2.getType()!=ASTNodeType.STRING)
      throw new EvaluationException("Expected two strings; was given \""+rand1.getValue()+"\", \""+rand2.getValue()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.STRING);
    result.setValue(rand1.getValue()+rand2.getValue());
    valueStack.push(result);
  }

  private void itos(ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      throw new EvaluationException("Expected an integer; was given \""+rand.getValue()+"\"");

    rand.setType(ASTNodeType.STRING); //we store all values internally as strings, so nothing to do
    valueStack.push(rand);
  }

  private void order(ASTNode rand){
    if(rand.getType()!=ASTNodeType.TUPLE)
      throw new EvaluationException("Expected a tuple; was given \""+rand.getValue()+"\"");

    ASTNode result = new ASTNode();
    result.setType(ASTNodeType.INTEGER);
    result.setValue(Integer.toString(getNumChildren(rand)));
    valueStack.push(result);
  }

  private void isNullTuple(ASTNode rand){
    if(rand.getType()!=ASTNodeType.TUPLE)
      throw new EvaluationException("Expected a tuple; was given \""+rand.getValue()+"\"");

    if(getNumChildren(rand)==0)
      pushTrueNode();
    else
      pushFalseNode();
  }

  // RULE 10
  private void tupleSelection(Tuple rator, ASTNode rand){
    if(rand.getType()!=ASTNodeType.INTEGER)
      throw new EvaluationException("Non-integer tuple selection with \""+rand.getValue()+"\"");

    ASTNode result = getNthTupleChild(rator, Integer.parseInt(rand.getValue()));
    if(result==null)
      throw new EvaluationException("Tuple selection index "+rand.getValue()+" out of bounds");

    valueStack.push(result);
  }

  /**
   * Get the nth element of the tuple. Note that n starts from 1 and NOT 0.
   * @param tupleNode
   * @param n n starts from 1 and NOT 0.
   * @return
   */
  private ASTNode getNthTupleChild(Tuple tupleNode, int n){
    ASTNode childNode = tupleNode.getChild();
    for(int i=1;i<n;++i){ //tuple selection index starts at 1
      if(childNode==null)
        break;
      childNode = childNode.getSibling();
    }
    return childNode;
  }

  private void handleIdentifiers(Delta currentDelta, ASTNode node){
    if(isReservedIdentifier(node.getValue()))
      valueStack.push(node);
    else // RULE 1
      valueStack.push(currentDelta.getCurrentEnv().lookup(node.getValue()));
  }

  //RULE 9
  private void createTuple(ASTNode node){
    int numChildren = getNumChildren(node);
    Tuple tupleNode = new Tuple();
    if(numChildren==0){
      valueStack.push(tupleNode);
      return;
    }

    ASTNode childNode = null, tempNode = null;
    for(int i=0;i<numChildren;++i){
      if(childNode==null)
        childNode = valueStack.pop();
      else if(tempNode==null){
        tempNode = valueStack.pop();
        childNode.setSibling(tempNode);
      }
      else{
        tempNode.setSibling(valueStack.pop());
        tempNode = tempNode.getSibling();
      }
    }
    tupleNode.setChild(childNode);
    valueStack.push(tupleNode);
  }

  // RULE 8
  private void handleConditional(ASTNode node){
    ASTNode conditionNode = valueStack.pop();

    if(conditionNode.getType()!=ASTNodeType.TRUE && conditionNode.getType()!=ASTNodeType.FALSE)
      throw new EvaluationException("Expecting a truthvalue; found \""+conditionNode.getValue()+"\"");

    if(conditionNode.getType()==ASTNodeType.TRUE){
      ASTNode thenNode = valueStack.pop(); //discard the elseNode
      valueStack.pop();
      valueStack.push(thenNode);
    }
    else{
      valueStack.pop(); //discard the thenNode
    }
  }

  private int getNumChildren(ASTNode node){
    int numChildren = 0;
    ASTNode childNode = node.getChild();
    while(childNode!=null){
      numChildren++;
      childNode = childNode.getSibling();
    }
    return numChildren;
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
      case "neg":
        return true;
    }
    return false;
  }

}
