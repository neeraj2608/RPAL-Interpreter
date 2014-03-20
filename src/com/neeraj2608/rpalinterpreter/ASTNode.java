package com.neeraj2608.rpalinterpreter;

public class ASTNode{
  private TokenType token;
  private ASTNode child;
  private ASTNode sibling;
  
  public String getName(){
    return token.name();
  }

  public TokenType getToken(){
    return token;
  }

  public void setToken(TokenType token){
    this.token = token;
  }

  public ASTNode getChild(){
    return child;
  }

  public void setChild(ASTNode child){
    this.child = child;
  }

  public ASTNode getSibling(){
    return sibling;
  }

  public void setSibling(ASTNode sibling){
    this.sibling = sibling;
  }
}
