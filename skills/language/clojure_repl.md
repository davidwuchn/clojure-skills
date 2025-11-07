---
name: clojure_repl
description: |
  Guide for interactive REPL-driven development in Clojure. Use when working
  interactively, testing code, exploring libraries, looking up documentation,
  debugging exceptions, or developing iteratively. Essential for the Clojure
  development workflow.
---

# Clojure REPL

## Quick Start

The REPL (Read-Eval-Print Loop) is Clojure's interactive programming environment.
It reads expressions, evaluates them, prints results, and loops. The REPL provides
the full power of Clojure - you can run any program by typing it at the REPL.

```clojure
user=> (+ 2 3)
5
user=> (defn greet [name] (str "Hello, " name))
#'user/greet
user=> (greet "World")
"Hello, World"
```

## Core Concepts

### Read-Eval-Print Loop
The REPL **R**eads your expression, **E**valuates it, **P**rints the result,
and **L**oops to repeat. Every expression you type produces a result that is
printed back to you.

### Side Effects vs Return Values
Understanding the difference between side effects and return values is crucial:

```clojure
user=> (println "Hello World")
Hello World    ; <- Side effect: printed by your code
nil            ; <- Return value: printed by the REPL
```

- `Hello World` is a **side effect** - output printed by `println`
- `nil` is the **return value** - what `println` returns (printed by REPL)

### Namespace Management
Libraries must be loaded before you can use them or query their documentation:

```clojure
;; Basic require
(require '[clojure.string])
(clojure.string/upper-case "hello")  ; => "HELLO"

;; With alias (recommended)
(require '[clojure.string :as str])
(str/upper-case "hello")  ; => "HELLO"

;; With refer (use sparingly)
(require '[clojure.string :refer [upper-case]])
(upper-case "hello")  ; => "HELLO"
```

## Common Workflows

### Exploring with clojure.repl

The `clojure.repl` namespace provides essential REPL utilities. Load it first:

```clojure
(require '[clojure.repl :refer :all])
```

### Looking Up Documentation

#### doc - View Function Documentation
View the docstring for any var:

```clojure
(doc map)
; -------------------------
; clojure.core/map
; ([f] [f coll] [f c1 c2] [f c1 c2 c3] [f c1 c2 c3 & colls])
;   Returns a lazy sequence consisting of the result of applying f to...

(doc clojure.string/upper-case)
; -------------------------
; clojure.string/upper-case
; ([s])
;   Converts string to all upper-case.
```

**Important**: Documentation is only available for required namespaces.

#### source - View Source Code
See the implementation of any function:

```clojure
(source some?)
; (defn some?
;   "Returns true if x is not nil, false otherwise."
;   {:tag Boolean
;    :added "1.6"
;    :static true}
;   [x] (not (nil? x)))
```

Requires that the `.clj` source file is on the classpath.

#### dir - List Namespace Contents
List all public vars in a namespace:

```clojure
(dir clojure.string)
; blank?
; capitalize
; ends-with?
; escape
; includes?
; index-of
; join
; ...

(dir clojure.repl)
; apropos
; demunge
; dir
; doc
; find-doc
; pst
; source
; ...
```

#### apropos - Search by Name
Find vars whose names match a pattern:

```clojure
(apropos "index")
; (clojure.core/indexed?
;  clojure.core/keep-indexed
;  clojure.core/map-indexed
;  clojure.string/index-of
;  clojure.string/last-index-of)

(apropos "map")
; Returns all vars with "map" in the name
```

#### find-doc - Search Documentation
Search docstrings across all loaded namespaces:

```clojure
(find-doc "indexed")
; -------------------------
; clojure.core/contains?
; ([coll key])
;  Returns true if key is present in the given collection, otherwise
;  returns false. Note that for numerically indexed collections like
;  vectors and Java arrays, this tests if the numeric key is within the
;  range of indexes...
; -------------------------
; clojure.core/indexed?
; ([coll])
;  Return true if coll implements Indexed, indicating efficient lookup by index
; ...
```

Searches both function names and their documentation strings.

### Debugging Exceptions

#### pst - Print Stack Trace
When an exception occurs, use `pst` to see the stack trace:

```clojure
user=> (/ 1 0)
; ArithmeticException: Divide by zero

user=> (pst)
; ArithmeticException Divide by zero
;   clojure.lang.Numbers.divide (Numbers.java:188)
;   clojure.lang.Numbers.divide (Numbers.java:3901)
;   user/eval2 (NO_SOURCE_FILE:1)
;   ...

;; Control depth
(pst 5)        ; Show 5 stack frames
(pst *e 10)    ; Show 10 frames of exception in *e
```

**Special REPL vars**:
- `*e` - Last exception thrown
- `*1` - Result of last expression
- `*2` - Result of second-to-last expression
- `*3` - Result of third-to-last expression

#### root-cause - Find Original Exception
Unwrap nested exceptions to find the root cause:

```clojure
(root-cause *e)
; Returns the initial cause by peeling off exception wrappers
```

#### demunge - Readable Stack Traces
Convert mangled Clojure function names in stack traces to readable form:

```clojure
(demunge "clojure.core$map")
; => "clojure.core/map"
```

Useful when reading raw stack traces from Java exceptions.

### Interactive Development Pattern

1. **Start small**: Test individual expressions
2. **Build incrementally**: Define functions and test them immediately
3. **Explore unknown territory**: Use `doc`, `source`, `dir` to understand libraries
4. **Debug as you go**: Test each piece before moving forward
5. **Iterate rapidly**: Change code and re-evaluate

```clojure
;; 1. Test the data structure
user=> {:name "Alice" :age 30}
{:name "Alice", :age 30}

;; 2. Test the operation
user=> (assoc {:name "Alice"} :age 30)
{:name "Alice", :age 30}

;; 3. Build the function
user=> (defn make-person [name age]
         {:name name :age age})
#'user/make-person

;; 4. Test it immediately
user=> (make-person "Bob" 25)
{:name "Bob", :age 25}

;; 5. Use it in more complex operations
user=> (map #(make-person (:name %) (:age %))
            [{:name "Carol" :age 35} {:name "Dave" :age 40}])
({:name "Carol", :age 35} {:name "Dave", :age 40})
```

### Loading Libraries Dynamically (Clojure 1.12+)

In Clojure 1.12+, you can add dependencies at the REPL without restarting:

```clojure
(require '[clojure.repl.deps :refer [add-lib add-libs sync-deps]])

;; Add a single library
(add-lib 'org.clojure/data.json)
(require '[clojure.data.json :as json])
(json/write-str {:foo "bar"})

;; Add multiple libraries with coordinates
(add-libs '{org.clojure/data.json {:mvn/version "2.4.0"}
            org.clojure/data.csv {:mvn/version "1.0.1"}})

;; Sync with deps.edn
(sync-deps)  ; Loads any libs in deps.edn not yet on classpath
```

**Note**: Requires a valid parent `DynamicClassLoader`. Works in standard REPL but
may not work in all environments.

## When to Use Each Tool

**Use `doc`** when you:
- Know the function name but need usage details
- Want to see function signatures
- Need quick reference for arguments

**Use `source`** when you:
- Want to understand how something is implemented
- Need to learn patterns from core library code
- Are debugging unexpected behavior

**Use `dir`** when you:
- Know the namespace but not the specific function
- Want to explore what's available in a library
- Need to browse available functions

**Use `apropos`** when you:
- Remember part of a function name
- Are searching across all namespaces
- Don't know which namespace contains the function

**Use `find-doc`** when you:
- Don't remember the function name
- Are searching by concept or keyword
- Need to find functions related to a topic

**Use `pst`** when you:
- Get an exception and need to see the stack trace
- Want to understand where an error originated
- Need to debug a failure

## Best Practices

**Do**:
- Test expressions incrementally before combining them
- Use `doc` and `source` liberally to learn from existing code
- Keep the REPL open during development for rapid feedback
- Load `clojure.repl` tools at the start: `(require '[clojure.repl :refer :all])`
- Use `:reload` flag when re-requiring changed namespaces: `(require 'my.ns :reload)`
- Experiment freely - the REPL is a safe sandbox

**Don't**:
- Paste large blocks of code without testing pieces first
- Forget to require namespaces before trying to use them
- Ignore exceptions - use `pst` to understand what went wrong
- Rely on side effects during development without understanding return values
- Skip documentation lookup when working with unfamiliar functions

## Common Issues

### "Unable to resolve symbol"
```clojure
user=> (str/upper-case "hello")
; CompilerException: Unable to resolve symbol: str/upper-case
```

**Solution**: Require the namespace first:
```clojure
(require '[clojure.string :as str])
(str/upper-case "hello")  ; => "HELLO"
```

### "No documentation found"
```clojure
(doc clojure.set/union)
; nil  ; No doc found
```

**Solution**: Documentation only available after requiring:
```clojure
(require '[clojure.set])
(doc clojure.set/union)  ; Now works
```

### "Can't find source"
```clojure
(source my-function)
; Source not found
```

**Solution**: `source` requires `.clj` files on classpath. Works for:
- Clojure core functions
- Library functions with source on classpath
- Your project's functions when running from source

Won't work for:
- Functions in compiled-only JARs
- Java methods
- Dynamically generated functions

### Stale definitions after file changes
When you edit a source file and reload it:

```clojure
;; Wrong - might keep old definitions
(require 'my.namespace)

;; Right - forces reload
(require 'my.namespace :reload)

;; Or reload all dependencies too
(require 'my.namespace :reload-all)
```

## Development Workflow Tips

1. **Keep a scratch namespace**: Use `user` namespace for experiments
2. **Save useful snippets**: Copy successful REPL experiments to your editor
3. **Use editor integration**: Most Clojure editors can send code to REPL
4. **Check return values**: Always verify what functions return, not just side effects
5. **Explore before implementing**: Use `doc`, `source`, `dir` to understand libraries
6. **Test edge cases**: Try `nil`, empty collections, invalid inputs at REPL
7. **Use REPL-driven testing**: Develop tests alongside code in REPL

## Summary

The Clojure REPL is your primary development tool:

- **Evaluate immediately**: Get instant feedback on every expression
- **Explore actively**: Use `doc`, `source`, `dir`, `apropos`, `find-doc`
- **Debug interactively**: Use `pst`, `root-cause`, and special vars like `*e`
- **Develop iteratively**: Build and test small pieces, then combine
- **Learn continuously**: Read source code and documentation as you work

Master REPL-driven development and you'll write better Clojure code faster.
