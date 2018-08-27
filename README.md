# BioDivine Model Toolkit

Libraries and utilities for defining and examining continuous, discrete and hybrid dynamical 
systems in a custom text format. The main idea is to provide a general extensible format suitable 
for many types of models without caring too much about the actual semantics or constraints of the tool
which will eventually process the model.

The idea is to have one format which can conveniently handle almost anything and then convert it to a tool specific,
machine readable format before the actual processing.  

#### Why a new format?

There are already universal formats like SBML. However, these typically work poorly with the discrete 
and computational aspects of modelling. Furthermore, SBML is designed for huge, machine defined models
(hence the use of XML) while BioDivine format is more focused on editing and curation by humans.   

# Model File Structure

Each file consists of a set of declarations (intentionally unordered). Each declaration can be extended with 
a set of annotations. However, before we get into the different types of declarations and the annotation specification,
let us first talk about the basic building blocks of each declaration: expressions and literals.

### Literals

The model format has support for many different types of data:
 - numbers (including scientific notation): 3.14, -4, 35e-2.
 - boolean values: true, false.
 - strings (including standard escaping mechanism): "hello", "two \n lines".
 - arrays of arbitrary comma-separated expressions: \["hello", 3.14, 5 - 6 > 3.0, \[true, sin(x)\]\]
 
We do not impose any sort of typing in the format itself (so all operators accept any input, assuming it has 
the correct arity). This is mainly to allow "extending" the format using existing mechanism. 

Let's say I would like to handle a custom logic which has true/false/unknown literals. I could extend the 
whole format witch new literal, or I can simply use the string "unknown" where necessary, replacing it 
with the unknown logical value while translating the model file to the actual model. 

### Expressions

Expression is a piece of model which, depending on current context, returns a value. Depending on the semantics of
the model, this value can be non-deterministic and of any type. Each expression consists of the following operators
(in the listed priority):

Binary:
 - ||: logical or
 - &&: logical and
 - -: minus
 - +: plus
 - /: division
 - *: multiplication

Unary:
 - !: logical negation
 - +: plus
 - -: minus

Finally, there is one control statement: switch {
    expression: expression
    ...
}

Here, each new guard has to be on a new line. Other whitespace is arbitrary.
  
Naturally, the priority can be adjusted arbitrarily using parentheses.

To preserve extensibility and universality, we do not impose any type constraints here either. 
I can write and parse "a" + "b" == "cd", however, it is up to the model processing tool to actually decide what 
this means. 

### Declarations

Each declaration defines 