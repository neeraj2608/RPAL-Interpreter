package com.neeraj2608.rpalinterpreter;

import java.util.Stack;

/**
 * Parser.
 * currentToken is assumed to have been loaded with the correct token BEFORE a given procedure is called.
 * "correct" = the token AFTER the last token processed by the previous procedure.
 * @author Raj
 */
public class Parser{
  private Scanner s;
  private Token currentToken;
  Stack<ASTNode> stack;

  public Parser(Scanner s){
    this.s = s;
    stack = new Stack<ASTNode>();
  }
  
  public AST buildAST(){
    startParse();
    return null;
  }

  public void startParse(){
    readNextToken();
    procE();
    if(currentToken!=null)
      throw new ParseException("Expected EOF.");
  }
  
  private void procE(){
    if(checkCurrentToken(TokenType.RESERVED, "let")){ //E -> ’let’ D ’in’ E => 'let'
      procD();
      readNextToken();
      if(!checkCurrentToken(TokenType.RESERVED, "in"))
        throw new ParseException("E: parse error 'in' expected");
      readNextToken();
      procE();
      BuildASTNode(ASTNodeType.LET, 2);
    }
    else if(checkCurrentToken(TokenType.RESERVED, "fn")){ //E -> ’fn’ Vb+ ’.’ E => ’lambda’
      readNextToken();
      int treesToPop = 0;
      if(!checkCurrentTokenType(TokenType.IDENTIFIER) && !checkCurrentTokenType(TokenType.L_PAREN))
        throw new ParseException("E: parse error 'Vb' expected");
      else{
        do{
          procVB(); //extra readNextToken() in procVB()
          treesToPop++;
        }while(checkCurrentTokenType(TokenType.IDENTIFIER)||checkCurrentTokenType(TokenType.L_PAREN));
      }
      if(!checkCurrentToken(TokenType.OPERATOR, "."))
        throw new ParseException("E: parse error '.' expected");
      procE();
      BuildASTNode(ASTNodeType.LAMBDA, treesToPop+1); //+1 for the last E 
    }
    else //E -> Ew
      procEW();
    
  }

  private void procEW(){
    procT(); //Ew -> T
    //extra readToken done in procT()
    if(checkCurrentToken(TokenType.RESERVED, "where")){ //Ew -> T 'where' Dr => 'where'
      procDR();
      BuildASTNode(ASTNodeType.WHERE, 2);
    }
  }
  
  private void procT(){
    procTA(); //T -> Ta
    //extra readToken() done in procTA()
    if(checkCurrentToken(TokenType.OPERATOR, ",")){ //T -> Ta (’,’ Ta )+ => ’tau’
      int treesToPop = 0;
      readNextToken();
      do{
        procTA(); //extra readToken() done in procTA()
        treesToPop++;
      } while(checkCurrentToken(TokenType.OPERATOR, ","));
      BuildASTNode(ASTNodeType.TAU, treesToPop+1);
    }
  }

  private void procTA(){
    procTC(); //Ta -> Tc
    readNextToken();
    if(checkCurrentToken(TokenType.RESERVED, "aug")){ //Ta -> Ta ’aug’ Tc => ’aug’
      int treesToPop = 0;
      readNextToken();
      do{
        procTC(); //extra readNextToken done in procTC 
        treesToPop++;
      } while(checkCurrentToken(TokenType.RESERVED, "aug"));
      BuildASTNode(ASTNodeType.TAU, treesToPop+1);
    }
  }

  private void procTC(){
    procB(); //Tc -> B
    readNextToken();
    if(checkCurrentToken(TokenType.OPERATOR, "->")){ //Tc -> B '->' Tc '|' Tc => '->'
      readNextToken();
      procTC();
      readNextToken();
      if(!checkCurrentToken(TokenType.OPERATOR, "|"))
        throw new ParseException("TC: '|' expected");
      readNextToken();
      procTC();
      BuildASTNode(ASTNodeType.CONDITIONAL, 3);
      readNextToken();
    }
  }

  private void procB(){
    // TODO Auto-generated method stub
    
  }

  private void procD(){
    procDA(); //D -> Da
    if(checkCurrentToken(TokenType.RESERVED, "within")){ //D -> Da 'within' D => 'within'
      readNextToken();
      procD();
      BuildASTNode(ASTNodeType.WITHIN, 2);
    }
  }
  
  private void procDA(){
    procDR(); //Da -> Dr
    readNextToken();
    if(checkCurrentToken(TokenType.RESERVED, "and")){ //Da -> Dr ( ’and’ Dr )+ => 'and'
      int treesToPop = 0;
      do{
        procDR();
        treesToPop++;
        readNextToken();
      } while(checkCurrentToken(TokenType.RESERVED, "and"));
      BuildASTNode(ASTNodeType.SIMULTDEF, treesToPop+1);
    }
  }
  
  private void procDR(){
    if(checkCurrentToken(TokenType.RESERVED, "rec")){ //Dr -> 'rec' Db => 'rec'
      readNextToken();
      procDB();
      BuildASTNode(ASTNodeType.REC, 1);
    }
    else{ //Dr -> Db
      procDB();
    }
  }
  
  private void procDB(){
    if(checkCurrentTokenType(TokenType.L_PAREN)){ //Db -> ’(’ D ’)’
      procD();
      readNextToken();
      if(!checkCurrentTokenType(TokenType.R_PAREN))
        throw new ParseException("DB: ')' expected");
      readNextToken();
    }
    else if(checkCurrentTokenType(TokenType.IDENTIFIER)){
      readNextToken();
      if(checkCurrentToken(TokenType.OPERATOR, ",")){ //Db -> Vl ’=’ E => '='
        readNextToken();
        procVL(); //extra readNextToken() in procVB()
        //VL makes its COMMA nodes for all the tokens EXCEPT the ones
        //we just read above (i.e., the first identifier and the comma after it)
        //Hence, we must pop the top of the tree VL just made and put it under a
        //comma node with the identifier it missed.
        if(!checkCurrentToken(TokenType.OPERATOR, "="))
          throw new ParseException("DB: = expected.");
        BuildASTNode(ASTNodeType.COMMA, 2);
        readNextToken();
        procE();
        BuildASTNode(ASTNodeType.EQUAL, 2);
        readNextToken();
      }
      else{ //Db -> ’<IDENTIFIER>’ Vb+ ’=’ E => 'fcn_form'
        int treesToPop = 0;
        if(!checkCurrentTokenType(TokenType.IDENTIFIER) && !checkCurrentTokenType(TokenType.L_PAREN))
          throw new ParseException("E: parse error 'Vb' expected");
        else{
          do{
            procVB(); //extra readNextToken() in procVB()
            treesToPop++;
          }while(checkCurrentTokenType(TokenType.IDENTIFIER)||checkCurrentTokenType(TokenType.L_PAREN));
        }
        if(!checkCurrentToken(TokenType.OPERATOR, "="))
          throw new ParseException("DB: parse error '=' expected");
        procE();
        BuildASTNode(ASTNodeType.FCNFORM, treesToPop+2); //+1 for the last E and +1 for the first identifier
        readNextToken();
      }
    }
  }
  
  private void procVB(){
    if(checkCurrentTokenType(TokenType.IDENTIFIER)){ //Vb -> '<IDENTIFIER>'
      readNextToken();
    }
    else if(checkCurrentTokenType(TokenType.L_PAREN)){
      readNextToken();
      if(checkCurrentTokenType(TokenType.R_PAREN)){ //Vb -> ’(’ ’)’
        createTerminalASTNode(ASTNodeType.PAREN, "");
        readNextToken();
      }
      else{ //Vb -> ’(’ Vl ’)’
        procVL(); //extra readNextToken() in procVB()
        if(!checkCurrentTokenType(TokenType.R_PAREN))
          throw new ParseException("VB: ')' expected");
        readNextToken();
      }
    }
  }

  private void procVL(){
    if(!checkCurrentTokenType(TokenType.IDENTIFIER))
      throw new ParseException("VL: Identifier expected");
    else{
      readNextToken();
      if(checkCurrentToken(TokenType.OPERATOR, ",")){ //Vl -> ’<IDENTIFIER>’ list ’,’ => ','?
        int treesToPop = 0;
        do{
          treesToPop++;
          readNextToken();
          if(!checkCurrentTokenType(TokenType.IDENTIFIER))
            throw new ParseException("VL: Identifier expected");
          readNextToken();
        } while(checkCurrentToken(TokenType.OPERATOR, ","));
        BuildASTNode(ASTNodeType.COMMA, treesToPop+1);
      }
    }
  }

  private void BuildASTNode(ASTNodeType type, int treesToPop){
    // TODO
  }

  private void readNextToken(){
    if(null != currentToken){
      if(currentToken.getType()==TokenType.IDENTIFIER){
        createTerminalASTNode(ASTNodeType.IDENTIFIER, currentToken.getValue());
      }
      else if(currentToken.getType()==TokenType.INTEGER){
        createTerminalASTNode(ASTNodeType.INTEGER, currentToken.getValue());
      } 
      else if(currentToken.getType()==TokenType.STRING){
        createTerminalASTNode(ASTNodeType.STRING, currentToken.getValue());
      }
    }
    currentToken = s.readNextToken(); //load next token
  }
  
  private boolean checkCurrentToken(TokenType type, String value){
    if(currentToken.getType()!=type || !currentToken.getValue().equals(value))
      return false;
    return true;
  }
  
  private boolean checkCurrentTokenType(TokenType type){
    if(currentToken.getType()!=type)
      return false;
    return true;
  }

  private void createTerminalASTNode(ASTNodeType type, String value){
    ASTNode node = new ASTNode();
    node.setType(type);
    node.setValue(value);
    stack.push(node);
  }

}

