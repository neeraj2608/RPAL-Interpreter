.PHONY: dirs all clean cl

JC = javac
JFLAGS = -g
CLASSDIR = $(shell pwd)
#CLASSDIR = class #need only this when CLASSDIR = class (and NOT pwd)
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
               P1.java \

all: dirs classestocompile

classestocompile: $(addprefix $(CLASSDIR)/, $(SOURCEFILES:.java=.class))

run: all
	@java P1
#@java -cp $(CLASSDIR) P1 #need only this when CLASSDIR = class (and NOT pwd)

test:
	./difftest.pl -1 "./rpal -ast -noout FILE" -2 "java P1 -ast -noout FILE" -t ~/rpal/tests/
#./difftest.pl -1 "./rpal -ast -noout FILE" -2 "java P1 -ast -noout FILE" -t ~/rpal/tests/

dirs:
	@mkdir -p $(CLASSDIR)

cl: clean

clean:
	@rm -rf com
	@rm -f P1
#@rm -fr $(CLASSDIR) #need only this when CLASSDIR = class (and NOT pwd)
