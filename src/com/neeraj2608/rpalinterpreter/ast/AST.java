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
}
