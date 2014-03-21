.PHONY: dirs all clean cl

JC = javac
JFLAGS = -g
CLASSDIR = class
SRCDIR = src

.SUFFIXES: .java .class

$(CLASSDIR)/%.class : $(SRCDIR)/%.java
	@echo -n ">>> Compiling $@..."
	@$(JC) $(JFLAGS) -cp $(CLASSDIR) -d $(CLASSDIR) $<
	@echo " done."

SOURCEFILES := \
               com/neeraj2608/rpalinterpreter/scanner/TokenType.java \
               com/neeraj2608/rpalinterpreter/scanner/Token.java \
               com/neeraj2608/rpalinterpreter/scanner/LexicalRegexPatterns.java \
               com/neeraj2608/rpalinterpreter/scanner/Scanner.java \
               com/neeraj2608/rpalinterpreter/ast/ASTNodeType.java \
               com/neeraj2608/rpalinterpreter/ast/ASTNode.java \
               com/neeraj2608/rpalinterpreter/ast/AST.java \
               com/neeraj2608/rpalinterpreter/parser/ParseException.java \
               com/neeraj2608/rpalinterpreter/parser/Parser.java \
               com/neeraj2608/rpalinterpreter/Controller.java \

all: dirs classestocompile

classestocompile: $(addprefix $(CLASSDIR)/, $(SOURCEFILES:.java=.class))

run:
	@java -cp $(CLASSDIR) com.neeraj2608.rpalinterpreter.Controller

dirs:
	@mkdir -p $(CLASSDIR)

cl: clean

clean:
	@rm -rf ./class
