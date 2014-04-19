package com.neeraj2608.rpalinterpreter.ast;

/*
 * Abstract Syntax Tree: The nodes use a first-child
 * next-sibling representation.
 */
public class AST{
  private ASTNode root;

  public AST(ASTNode node){
    this.root = node;
  }

  /**
   * Prints the tree nodes in pre-order fashion.
   */
  public void print(){
    preOrderPrint(root,"");
  }

  private void preOrderPrint(ASTNode node, String printPrefix){
    if(node==null)
      return;
    
    printASTNodeDetails(node, printPrefix);
    preOrderPrint(node.getChild(),printPrefix+".");
    preOrderPrint(node.getSibling(),printPrefix);
  }

  private void printASTNodeDetails(ASTNode node, String printPrefix){
    if(node.getType() == ASTNodeType.IDENTIFIER ||
       node.getType() == ASTNodeType.INTEGER ||
       node.getType() == ASTNodeType.STRING){
      System.out.printf(printPrefix+node.getType().getPrintName()+"\n",node.getValue());
    }
    else{
      System.out.println(printPrefix+node.getType().getPrintName());
    }
  }
  
  /**
   * Standardize this tree
   */
  public void standardize(){
    standardize(root);
  }

  /**
   * Standardize the tree bottom-up
   * @param node node to standardize
   */
  private void standardize(ASTNode node){
    //standardize the children first
    if(node.getChild()!=null){
      ASTNode childNode = node.getChild();
      while(childNode!=null){
        standardize(childNode);
        childNode = childNode.getSibling();
      }
    }
    
    //all children standardized. now standardize this node
    switch(node.getType()){
      case LET:
        ASTNode equalNode = node.getChild();
        if(equalNode.getType()!=ASTNodeType.EQUAL)
          throw new RuntimeException("LET/WHERE: left child is not EQUAL"); //TODO: this may not be required
        ASTNode e = equalNode.getChild().getSibling();
        equalNode.getChild().setSibling(equalNode.getSibling());
        equalNode.setSibling(e);
        equalNode.setType(ASTNodeType.LAMBDA);
        node.setType(ASTNodeType.GAMMA);
        break;
      case WHERE:
        //make this is a LET node and standardize that
        equalNode = node.getChild().getSibling();
        node.getChild().setSibling(null);
        equalNode.setSibling(node.getChild());
        node.setChild(equalNode);
        node.setType(ASTNodeType.LET);
        standardize(node);
        break;
      case FCNFORM:
        ASTNode childSibling = node.getChild().getSibling();
        node.getChild().setSibling(constructLambdaChain(childSibling));
        node.setType(ASTNodeType.EQUAL);
      default:
        break;
    }
  }

  private ASTNode constructLambdaChain(ASTNode node){
    ASTNode lambdaNode = new ASTNode();
    lambdaNode.setType(ASTNodeType.LAMBDA);
    lambdaNode.setChild(node);
    if(node.getSibling().getSibling()!=null)
      node.setSibling(constructLambdaChain(node.getSibling()));
    return lambdaNode;
  }
}
