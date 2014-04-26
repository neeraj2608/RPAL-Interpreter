.PHONY: dirs all clean cl

JC = javac
JFLAGS = -g
CLASSDIR = $(shell pwd)
#CLASSDIR = class #need only this when CLASSDIR = class (and NOT pwd)
SRCDIR = src

.SUFFIXES: .java .class

$(CLASSDIR)/%.class : $(SRCDIR)/%.java
	@echo -n ">>> Compiling $<..."
	@$(JC) $(JFLAGS) -sourcepath $(SRCDIR) -cp $(CLASSDIR) -d $(CLASSDIR) $<
	@echo " done."

SOURCEFILES := \
               com/neeraj2608/rpalinterpreter/ast/AST.java \
               com/neeraj2608/rpalinterpreter/ast/ASTNode.java \
               com/neeraj2608/rpalinterpreter/ast/ASTNodeType.java \
               com/neeraj2608/rpalinterpreter/ast/StandardizeException.java \
               com/neeraj2608/rpalinterpreter/csem/Beta.java \
               com/neeraj2608/rpalinterpreter/csem/CSEMachine.java \
               com/neeraj2608/rpalinterpreter/csem/Delta.java \
               com/neeraj2608/rpalinterpreter/csem/Environment.java \
               com/neeraj2608/rpalinterpreter/csem/Eta.java \
               com/neeraj2608/rpalinterpreter/csem/EvaluationError.java \
               com/neeraj2608/rpalinterpreter/csem/NodeCopier.java \
               com/neeraj2608/rpalinterpreter/csem/Tuple.java \
               com/neeraj2608/rpalinterpreter/parser/ParseException.java \
               com/neeraj2608/rpalinterpreter/parser/Parser.java \
               com/neeraj2608/rpalinterpreter/scanner/LexicalRegexPatterns.java \
               com/neeraj2608/rpalinterpreter/scanner/Scanner.java \
               com/neeraj2608/rpalinterpreter/scanner/Token.java \
               com/neeraj2608/rpalinterpreter/scanner/TokenType.java \
               com/neeraj2608/rpalinterpreter/driver/P1.java \
               com/neeraj2608/rpalinterpreter/driver/P2.java \

all: dirs classestocompile

classestocompile: $(addprefix $(CLASSDIR)/, $(SOURCEFILES:.java=.class))

# Example usage: `make run cmd='-ast ~/rpal/tests/add'`
# The cmd variable is passed to the p1 script, which in turn passes
# it to P1.java
run: all
  ifeq ($(strip $(cmd)),) #NOTE: conditional directive must NOT start with a tab!
		@echo "Please provide parser switches using the cmd variable e.g."
		@echo "make run cmd='-ast <filename>'"
  else
		@./p1 $(cmd)
  endif
#@java -cp $(CLASSDIR) P1 #need only this when CLASSDIR = class (and NOT pwd)


# example usage: `make jar`
jar: all
	@echo -n ">>> Generating P1.jar... "
	@jar -cf P1.jar -m MANIFEST.MF -C . com/ P1.class
	@echo " done."

# example usage: `make test`
test: all
	./difftest.pl -1 "./rpal -st FILE" -2 "java -cp $(CLASSDIR) P2 -st FILE" -t ~/rpal/tests/
#./difftest.pl -1 "./rpal -ast -noout FILE" -2 "java P1 -ast -noout FILE" -t ~/rpal/tests/

dirs:
	@mkdir -p $(CLASSDIR)

cl: clean

clean:
	@rm -rf com
	@rm -f P1.class
	@rm -f P2.class
	@rm -f *.jar
	@rm -fr diffresult
#@rm -fr $(CLASSDIR) #need only this when CLASSDIR = class (and NOT pwd)
