RPAL Interpreter
=======
This is an interpreter written for the RPAL functional language. RPAL stands for *Right-reference Pedagogic Algorithmic Language*. In true functional style, RPAL has no concept of ��assignment� � or of � �memory� �. There are no loops, only recursion. An RPAL program is simply an expression. It consists exclusively of two notions: function definition and function application. 

As a quick example, here is the RPAL syntax for the canonical functional example that calculates factorials:

    let rec f x = x eq 0 -> 1 | x * f (x - 1) in Print (f 3)

The `rec` makes function `f` recursive and ensures that the `f` in the expression `x * f (x - 1)` is not free. Further details and examples of RPAL methods can be found in [this](http://www.cise.ufl.edu/class/cop5555sp14/rpal/rpal.pdf) PDF.

RPAL functions can be partially applied, as is normal in a functional programming language. In addition, functions are also first-class citizens.

This system implements the following features (the modules responsible are given in parentheses):

| Features | Module |
| --------------|------------|
|Recursive-Descent parse a source file in accordance with RPAL's [phrase structure grammar (PDF)](http://www.cise.ufl.edu/class/cop5555sp14/rpal/rpal.grammar.pdf)|Scanner|
|Create an abstract syntax tree | Parser |
|Standardize the abstract syntax tree | Parser |
|Execute the program by processing the standardized syntax tree in a Control-Stack-Execution Machine (CSEM). The CSEM manages the overhead of creating lambda closures including managing nested environments for those closures, providing basic functionality such as arithmetic or logical operations on RPAL data types etc. | Control Stack Execution Machine |

##### Scanner + Parser + Control-Stack Execution Machine == Interpeter!

The system lets you view a textual representation of abstract syntax tree or the standardized syntax tree generated.
Here is the abstract syntax tree for the recursion example given above:

<pre>
let
.rec
..function_form
...&lt;ID:f&gt;
...&lt;ID:x&gt;
...-&gt;
....eq
.....&lt;ID:x&gt;
.....&lt;INT:0&gt;
....&lt;INT:1&gt;
....*
.....&lt;ID:x&gt;
.....gamma
......&lt;ID:f&gt;
......-
.......&lt;ID:x&gt;
.......&lt;INT:1&gt;
.gamma
..&lt;ID:Print&gt;
..gamma
...&lt;ID:f&gt;
...&lt;INT:3&gt;
</pre>

There are three ways to get this textual representation:
 
* Use the p2 script provided: <code>p2 -ast &lt;rpal\_program\_filename&gt;</code>. This script can be invoked from ANY directory e.g. <code>other\_directory$ path\_to\_RPAL-Interpreter/p2 -ast &lt;filename&gt;</code>

* Use the makefile from this directory: <code>make run cmd='-ast &lt;rpal\_program\_filename&gt;'</code>. From another directory, <code>other\_directory$ make run cmd='-ast &lt;filename&gt;' -C path\_to\_RPAL-Interpreter</code>.

* Invoke the parser yourself from this directory (after compiling the code, of course): <code>java -cp . P2 -ast &lt;filename&gt;</code> or from another directory <code>java -cp path\_to\_RPAL-Interpreter P2 -ast &lt;filename&gt;</code>


In the examples above, the `-ast` switch prints the non-standardized abstract syntax tree. `-st` will print the standardized tree, adding `-noout` to either of those will prevent the program from executing. Finally, simply calling p2 without any parameters will not print either tree but will run the program and print the output.


To run a diff on the AST generated by this code and Steve Walstra's C implementation for various RPAL programs, run (in this directory): <code>make test</code>. From another directory, <code>make test -C path\_to\_RPAL-Interpreter</code>. Note that this requires you to have the RPAL programs that you wish to run in the directory <code>~/rpal/tests/</code>