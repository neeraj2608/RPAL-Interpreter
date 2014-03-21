package com.neeraj2608.rpalinterpreter;

import java.util.Stack;

/**
 * Parser: Recursive descent parser that complies with RPAL's phrase structure grammar.
 * This class does all the heavy lifting. It gets input from the scanner, and builds the
 * abstract syntax tree.
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
    //TODO: need guards here??
    return new AST(stack.pop());
  }

  public void startParse(){
    readNT();
    procE(); //extra readNT in procE()
    if(currentToken!=null)
      throw new ParseException("Expected EOF.");
  }

  private void readNT(){
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
    do{
      currentToken = s.readNextToken(); //load next token
    }while(isCurrentTokenType(TokenType.DELETE));
  }
  
  private boolean isCurrentToken(TokenType type, String value){
    if(currentToken==null)
      return false;
    if(currentToken.getType()!=type || !currentToken.getValue().equals(value))
      return false;
    return true;
  }
  
  private boolean isCurrentTokenType(TokenType type){
    if(currentToken==null)
      return false;
    if(currentToken.getType()==type)
      return true;
    return false;
  }
  
  private void buildASTNode(ASTNodeType type, int treesToPop){
    ASTNode node = new ASTNode();
    node.setType(type);
    while(treesToPop>0){
      ASTNode child = stack.pop();
      if(node.getChild()!=null)
        child.setSibling(node.getChild());
      node.setChild(child);
      treesToPop--;
    }
    stack.push(node);
  }

  private void createTerminalASTNode(ASTNodeType type, String value){
    ASTNode node = new ASTNode();
    node.setType(type);
    node.setValue(value);
    stack.push(node);
  }
  
  /******************************
   * Expressions
   *******************************/
  
  private void procE(){
    if(isCurrentToken(TokenType.RESERVED, "let")){ //E -> ’let’ D ’in’ E => 'let'
      readNT();
      procD();
      if(!isCurrentToken(TokenType.RESERVED, "in"))
        throw new ParseException("E:  'in' expected");
      readNT();
      procE(); //extra readNT in procE()
      buildASTNode(ASTNodeType.LET, 2);
    }
    else if(isCurrentToken(TokenType.RESERVED, "fn")){ //E -> ’fn’ Vb+ ’.’ E => ’lambda’
      int treesToPop = 0;
      
      readNT();
      while(isCurrentTokenType(TokenType.IDENTIFIER) || isCurrentTokenType(TokenType.L_PAREN)){
        procVB(); //extra readNT in procVB()
        treesToPop++;
      }
      
      if(treesToPop==0)
        throw new ParseException("E: at least one 'Vb' expected");
      
      if(!isCurrentToken(TokenType.OPERATOR, "."))
        throw new ParseException("E: '.' expected");
      
      readNT();
      procE(); //extra readNT in procE()
      
      buildASTNode(ASTNodeType.LAMBDA, treesToPop+1); //+1 for the last E 
    }
    else //E -> Ew
      procEW();
  }

  private void procEW(){
    procT(); //Ew -> T
    //extra readToken done in procT()
    if(isCurrentToken(TokenType.RESERVED, "where")){ //Ew -> T 'where' Dr => 'where'
      readNT();
      procDR(); //extra readToken() in procDR()
      buildASTNode(ASTNodeType.WHERE, 2);
    }
  }
  
  /******************************
   * Tuple Expressions
   *******************************/
  
  private void procT(){
    procTA(); //T -> Ta
    //extra readToken() in procTA()
    int treesToPop = 0;
    while(isCurrentToken(TokenType.OPERATOR, ",")){ //T -> Ta (’,’ Ta )+ => ’tau’
      readNT();
      procTA(); //extra readToken() done in procTA()
      treesToPop++;
    }
    if(treesToPop > 0) buildASTNode(ASTNodeType.TAU, treesToPop+1);
  }

  private void procTA(){
    procTC(); //Ta -> Tc
    //extra readNT done in procTC()
    int treesToPop = 0;
    while(isCurrentToken(TokenType.RESERVED, "aug")){ //Ta -> Ta ’aug’ Tc => ’aug’
      readNT();
      procTC(); //extra readNT done in procTC()
      treesToPop++;
    }
    if(treesToPop>0) buildASTNode(ASTNodeType.AUG, treesToPop+1);
  }

  private void procTC(){
    procB(); //Tc -> B
    //extra readNT in procBT()
    if(isCurrentToken(TokenType.OPERATOR, "->")){ //Tc -> B '->' Tc '|' Tc => '->'
      readNT();
      procTC(); //extra readNT done in procTC
      if(!isCurrentToken(TokenType.OPERATOR, "|"))
        throw new ParseException("TC: '|' expected");
      readNT();
      procTC();  //extra readNT done in procTC
      buildASTNode(ASTNodeType.CONDITIONAL, 3);
    }
  }
  
  /******************************
   * Boolean Expressions
   *******************************/
  
  private void procB(){
    procBT(); //B -> Bt
    //extra readNT in procBT()
    while(isCurrentToken(TokenType.RESERVED, "or")){ //B -> B 'or' Bt => 'or'
      readNT();
      procBT();
      buildASTNode(ASTNodeType.OR, 2);
    }
  }
  
  private void procBT(){
    procBS(); //Bt -> Bs;
    //extra readNT in procBS()
    while(isCurrentToken(TokenType.OPERATOR, "&")){ //Bt -> Bt ’&’ Bs => ’&’
      readNT();
      procBS(); //extra readNT in procBS()
      buildASTNode(ASTNodeType.AND, 2);
    }
  }
  
  private void procBS(){
    if(isCurrentToken(TokenType.RESERVED, "not")){ //Bs -> ’not’ Bp => ’not’
      readNT();
      procBP(); //extra readNT in procBP()
      buildASTNode(ASTNodeType.NOT, 1);
    }
    else
      procBP(); //Bs -> Bp
      //extra readNT in procBP()
  }
  
  private void procBP(){
    procA(); //Bp -> A
    if(isCurrentToken(TokenType.RESERVED,"gr")||isCurrentToken(TokenType.OPERATOR,">")){ //Bp -> A(’gr’ | ’>’ ) A => 'gr'
      readNT();
      procA(); //extra readNT in procA()
      buildASTNode(ASTNodeType.GR, 2);
    }
    else if(isCurrentToken(TokenType.RESERVED,"ge")||isCurrentToken(TokenType.OPERATOR,">=")){ //Bp -> A (’ge’ | ’>=’) A => ’ge’
      readNT();
      procA(); //extra readNT in procA()
      buildASTNode(ASTNodeType.GE, 2);
    }
    else if(isCurrentToken(TokenType.RESERVED,"ls")||isCurrentToken(TokenType.OPERATOR,"<")){ //Bp -> A (’ls’ | ’<’ ) A => ’ls’
      readNT();
      procA(); //extra readNT in procA()
      buildASTNode(ASTNodeType.GE, 2);
    }
    else if(isCurrentToken(TokenType.RESERVED,"le")||isCurrentToken(TokenType.OPERATOR,"<=")){ //Bp -> A (’le’ | ’<=’) A => ’le’
      readNT();
      procA(); //extra readNT in procA()
      buildASTNode(ASTNodeType.GE, 2);
    }
    else if(isCurrentToken(TokenType.RESERVED,"eq")){ //Bp -> A ’eq’ A => ’eq’
      readNT();
      procA(); //extra readNT in procA()
      buildASTNode(ASTNodeType.EQ, 2);
    }
    else if(isCurrentToken(TokenType.RESERVED,"ne")){ //Bp -> A ’ne’ A => ’ne’
      readNT();
      procA(); //extra readNT in procA()
      buildASTNode(ASTNodeType.NE, 2);
    }
  }
  
  
  /******************************
   * Arithmetic Expressions
   *******************************/
  
  private void procA(){
    if(isCurrentToken(TokenType.OPERATOR, "+")){
      readNT();
      procAT(); //extra readNT in procAT()
    }
    else if(isCurrentToken(TokenType.OPERATOR, "-")){
      readNT();
      procAT(); //extra readNT in procAT()
      buildASTNode(ASTNodeType.NEG, 1);
    }
    else
      procAT(); //extra readNT in procAT()
    
    boolean plus = true;
    while(isCurrentToken(TokenType.OPERATOR, "+")||isCurrentToken(TokenType.OPERATOR, "-")){
      if(currentToken.getValue().equals("+"))
        plus = true;
      else if(currentToken.getValue().equals("-"))
        plus = false;
      readNT();
      procAT(); //extra readNT in procAT()
      if(plus)
        buildASTNode(ASTNodeType.PLUS, 2);
      else
        buildASTNode(ASTNodeType.MINUS, 2);
    }
  }
  
  private void procAT(){
    procAF(); //extra readNT in procAF()
    boolean mult = true;
    while(isCurrentToken(TokenType.OPERATOR, "*")||isCurrentToken(TokenType.OPERATOR, "/")){
      if(currentToken.getValue().equals("*"))
        mult = true;
      else if(currentToken.getValue().equals("/"))
        mult = false;
      readNT();
      procAF(); //extra readNT in procAF()
      if(mult)
        buildASTNode(ASTNodeType.MULT, 2);
      else
        buildASTNode(ASTNodeType.DIV, 2);
    }
  }
  
  private void procAF(){
    procAP(); //extra readNT in procAP()
    if(isCurrentToken(TokenType.OPERATOR, "**")){
      readNT();
      procAF();
      buildASTNode(ASTNodeType.EXP, 2);
    }
  }
  
  
  private void procAP(){
    procR(); //extra readNT in procR()
    int treesToPop = 0;
    while(isCurrentToken(TokenType.OPERATOR, "@")){
      readNT();
      if(!isCurrentTokenType(TokenType.IDENTIFIER))
        throw new ParseException("AP: expected Identifier");
      readNT();
      procR(); //extra readNT in procR()
      treesToPop++;
    }
    if(treesToPop > 0) buildASTNode(ASTNodeType.AT, treesToPop+1);
  }
  
  /******************************
   * Rators and Rands
   *******************************/
  
  private void procR(){
    procRN();
    //extra readNT in procRN()
    int treesToPop = 0;
    while(isCurrentTokenType(TokenType.INTEGER)||
        isCurrentTokenType(TokenType.STRING)|| 
        isCurrentTokenType(TokenType.IDENTIFIER)||
        isCurrentToken(TokenType.RESERVED, "true")||
        isCurrentToken(TokenType.RESERVED, "false")||
        isCurrentToken(TokenType.RESERVED, "nil")||
        isCurrentToken(TokenType.RESERVED, "dummy")||
        isCurrentTokenType(TokenType.L_PAREN)){ //R -> R Rn => 'gamma'
      procRN(); //extra readNT in procRN()
      treesToPop++;
    }
    
    if(treesToPop > 0) buildASTNode(ASTNodeType.GAMMA, treesToPop+1);
  }

  private void procRN(){
    if(isCurrentTokenType(TokenType.IDENTIFIER)|| //R -> '<IDENTIFIER>'
       isCurrentTokenType(TokenType.INTEGER)|| //R -> '<INTEGER>' 
       isCurrentTokenType(TokenType.STRING)){ //R-> '<STRING>'
      readNT();
    }
    else if(isCurrentToken(TokenType.RESERVED, "true")){ //R -> 'true' => 'true'
      createTerminalASTNode(ASTNodeType.TRUE, "");
      readNT();
    }
    else if(isCurrentToken(TokenType.RESERVED, "false")){ //R -> 'false' => 'false'
      createTerminalASTNode(ASTNodeType.FALSE, "");
      readNT();
    } 
    else if(isCurrentToken(TokenType.RESERVED, "nil")){ //R -> 'nil' => 'nil'
      createTerminalASTNode(ASTNodeType.NIL, "");
      readNT();
    }
    else if(isCurrentTokenType(TokenType.L_PAREN)){
      readNT();
      procE(); //extra readNT in procE()
      if(!isCurrentTokenType(TokenType.R_PAREN))
        throw new ParseException("RN: ')' expected");
      readNT();
    }
    else if(isCurrentToken(TokenType.RESERVED, "dummy")){ //R -> 'dummy' => 'dummy'
      createTerminalASTNode(ASTNodeType.DUMMY, "");
      readNT();
    }
  }

  /******************************
   * Definitions
   *******************************/
  
  private void procD(){
    procDA(); //D -> Da
    //extra readToken() in procDA()
    if(isCurrentToken(TokenType.RESERVED, "within")){ //D -> Da 'within' D => 'within'
      readNT();
      procD();
      buildASTNode(ASTNodeType.WITHIN, 2);
    }
  }
  
  private void procDA(){
    procDR(); //Da -> Dr
    //extra readToken() in procDR()
    if(isCurrentToken(TokenType.RESERVED, "and")){ //Da -> Dr ( ’and’ Dr )+ => 'and'
      int treesToPop = 0;
      do{
        readNT();
        procDR(); //extra readToken() in procDR()
        treesToPop++;
      } while(isCurrentToken(TokenType.RESERVED, "and"));
      buildASTNode(ASTNodeType.SIMULTDEF, treesToPop+1);
    }
  }
  
  private void procDR(){ //extra readToken() in procDR()
    if(isCurrentToken(TokenType.RESERVED, "rec")){ //Dr -> 'rec' Db => 'rec'
      readNT();
      procDB(); //extra readToken() in procDB()
      buildASTNode(ASTNodeType.REC, 1);
    }
    else{ //Dr -> Db
      procDB(); //extra readToken() in procDB()
    }
  }
  
  private void procDB(){
    if(isCurrentTokenType(TokenType.L_PAREN)){ //Db -> ’(’ D ’)’
      procD();
      readNT();
      if(!isCurrentTokenType(TokenType.R_PAREN))
        throw new ParseException("DB: ')' expected");
      readNT();
    }
    else if(isCurrentTokenType(TokenType.IDENTIFIER)){
      readNT();
      if(isCurrentToken(TokenType.OPERATOR, ",")){ //Db -> Vl ’=’ E => '='
        readNT();
        procVL(); //extra readNT in procVB()
        //VL makes its COMMA nodes for all the tokens EXCEPT the ones
        //we just read above (i.e., the first identifier and the comma after it)
        //Hence, we must pop the top of the tree VL just made and put it under a
        //comma node with the identifier it missed.
        if(!isCurrentToken(TokenType.OPERATOR, "="))
          throw new ParseException("DB: = expected.");
        buildASTNode(ASTNodeType.COMMA, 2);
        readNT();
        procE(); //extra readNT in procE()
        buildASTNode(ASTNodeType.EQUAL, 2);
      }
      else{ //Db -> ’<IDENTIFIER>’ Vb+ ’=’ E => 'fcn_form'
        int treesToPop = 0;
        
        while(isCurrentTokenType(TokenType.IDENTIFIER) || isCurrentTokenType(TokenType.L_PAREN)){
          procVB(); //extra readNT in procVB()
          treesToPop++;
        }
        
        if(treesToPop==0)
          throw new ParseException("E: at least one 'Vb' expected");
        
        if(!isCurrentToken(TokenType.OPERATOR, "="))
          throw new ParseException("DB: = expected.");
        
        readNT();
        procE(); //extra readNT in procE()
        
        buildASTNode(ASTNodeType.FCNFORM, treesToPop+2); //+1 for the last E and +1 for the first identifier
      }
    }
  }
  
  /******************************
   * Variables
   *******************************/
  
  private void procVB(){
    if(isCurrentTokenType(TokenType.IDENTIFIER)){ //Vb -> '<IDENTIFIER>'
      readNT();
    }
    else if(isCurrentTokenType(TokenType.L_PAREN)){
      readNT();
      if(isCurrentTokenType(TokenType.R_PAREN)){ //Vb -> ’(’ ’)’
        createTerminalASTNode(ASTNodeType.PAREN, "");
        readNT();
      }
      else{ //Vb -> ’(’ Vl ’)’
        procVL(); //extra readNT in procVB()
        if(!isCurrentTokenType(TokenType.R_PAREN))
          throw new ParseException("VB: ')' expected");
        readNT();
      }
    }
  }

  private void procVL(){
    if(!isCurrentTokenType(TokenType.IDENTIFIER))
      throw new ParseException("VL: Identifier expected");
    else{
      readNT();
      int treesToPop = 0;
      while(isCurrentToken(TokenType.OPERATOR, ",")){ //Vl -> ’<IDENTIFIER>’ list ’,’ => ','?
        readNT();
        if(!isCurrentTokenType(TokenType.IDENTIFIER))
          throw new ParseException("VL: Identifier expected");
        readNT();
        treesToPop++;
      }
      if(treesToPop > 0) buildASTNode(ASTNodeType.COMMA, treesToPop+1); //+1 for the first identifier
    }
  }

}

