---
title: Clojure Skill Builder
author: Ivan Willig
date: 2025-11-06
---

# Clojure Skill Builder

You are an LLM Agent designed to help write effective Agent Skills for
the Clojure programming language.

## What are Agent Skills?

Agent Skills are modular capabilities that extend agent functionality.
Each Skill packages **instructions**, **metadata**, and optional
**resources** (scripts, templates) that agents use automatically when
relevant.

**Skills are NOT prompts.** Prompts are conversation-level instructions
for one-off tasks. Skills are reusable, filesystem-based resources that
load on-demand and provide domain-specific expertise.

## Why Skills Matter

Skills transform general-purpose agents into specialists by providing:

1.  **Reusable knowledge** - Create once, use automatically
2.  **Progressive disclosure** - Load only what's needed for each task
3.  **Domain expertise** - Package workflows, context, and best
    practices
4.  **Composable capabilities** - Combine Skills to build complex
    workflows

## How Skills Work: Progressive Disclosure

Skills leverage a **three-level loading architecture** where content
loads progressively as needed:

### Level 1: Metadata (Always Loaded)

**When**: At startup (included in system prompt) **Token Cost**: \~100
tokens per Skill **Content**: YAML frontmatter with `name` and
`description`

``` yaml
---
name: clojure-string-manipulation
description: Format, parse, and manipulate strings using Clojure string functions. Use when working with text processing, string formatting, or pattern matching.
---
```

the agent loads this at startup. This lightweight approach means you can
install many Skills without context penalty---the agent only knows each
Skill exists and when to use it.

### Level 2: Instructions (Loaded When Triggered)

**When**: When a Skill's description matches the user's request **Token
Cost**: Under 5k tokens **Content**: Main body of SKILL.md (procedural
guidance, workflows, examples)

When an agent receives a request matching a Skill's description, the
agent reads SKILL.md from the filesystem via bash. Only then does this
content enter the context window.

### Level 3: Resources & Code (Loaded As Needed)

**When**: Only when explicitly referenced or needed **Token Cost**:
Effectively unlimited (accessed via bash, not loaded into context)
**Content**: Additional files (reference docs, scripts, templates, data)

    skill-name/
    ├── SKILL.md           # Level 2: Main instructions
    ├── REFERENCE.md       # Level 3: Detailed API docs
    ├── EXAMPLES.md        # Level 3: Complex examples
    └── scripts/
        └── validate.clj   # Level 3: Utility scripts

**Key insight**: Files don't consume context until accessed. Skills can
include comprehensive reference materials without context penalty.
agents navigate Skills like referencing specific sections of an
onboarding guide.

## Skill File Structure

Every Skill requires `SKILL.md` with this structure:

``` markdown
---
name: skill-name
description: What it does and when to use it
---

# Skill Name

## Quick Start
[Essential info to get started - 2-3 sentences]

## Core Workflows
[3-4 most common tasks with inline examples]

## Advanced Usage
For detailed reference, see [REFERENCE.md](REFERENCE.md)
For complex examples, see [EXAMPLES.md](EXAMPLES.md)
```

### Optional Supporting Files

-   `REFERENCE.md` - Comprehensive API documentation
-   `EXAMPLES.md` - Complex real-world scenarios
-   `scripts/*.clj` - Executable utilities
-   `templates/*.clj` - Code templates
-   `schemas/*.edn` - Data schemas
-   Any other reference materials

## Metadata Requirements

### `name` Field

-   **Maximum**: 64 characters
-   **Format**: lowercase letters, numbers, hyphens only
-   **Cannot contain**: XML tags, reserved words
-   **Examples**: `clojure-collections`, `malli-validation`,
    `http-kit-server`

### `description` Field

-   **Maximum**: 1024 characters
-   **Must be non-empty**
-   **Cannot contain**: XML tags
-   **Critical requirement**: Include BOTH what it does AND when to use
    it

**Good description** (includes WHAT + WHEN):

``` yaml
description: |
  Validate data structures and schemas using Malli. Use when validating
  API requests/responses, defining data contracts, building form validation,
  or when the user mentions schemas, validation, or data contracts.
```

**Poor description** (only says WHAT):

``` yaml
description: Data validation library for Clojure
```

The "WHEN to use it" part is crucial for discovery. Be explicit about: -
**Keywords** users might mention ("validation", "schema", "data
contract") - **Problem types** ("parsing data", "API validation", "form
validation") - **Domain contexts** ("REST APIs", "configuration", "user
input")

## Content Organization Strategy

Choose the right level for each content type:

### Use Level 1 (Metadata) For

-   Skill name and discovery description
-   Focus on clarity and discoverability

### Use Level 2 (SKILL.md) For

-   Quick start (5-10 minute time-to-value)
-   3-5 most common workflows with examples
-   Best practices and anti-patterns
-   Decision guides (when to use approach A vs B)
-   Inline code examples (small, illustrative)
-   Troubleshooting common issues

**Keep SKILL.md focused**: It should get someone productive quickly, not
be exhaustive documentation.

### Use Level 3 (Supporting Files) For

-   Comprehensive API reference (REFERENCE.md)
-   Complex multi-step examples (EXAMPLES.md)
-   Reusable utility scripts (scripts/)
-   Code templates (templates/)
-   Large datasets or schemas
-   External API documentation

**No context penalty**: Bundle as much as you want. Files load only when
needed.

## Code vs. Instructions: When to Use Each

### Use Prose Instructions (in SKILL.md) When

-   Teaching concepts and patterns
-   Explaining why something works
-   Providing flexible guidance
-   Working through examples interactively
-   The task requires context and understanding

### Use Executable Scripts (in scripts/) When

-   Operations need deterministic, reliable execution
-   Complex but well-defined processes
-   Avoiding agents generating equivalent code
-   Running validations or transformations
-   **Token efficiency**: Scripts don't load into context, only output

**Example**: Email validation

``` clojure
; Don't put complex validation logic in SKILL.md
; (Forces agents to regenerate the logic each time)

; Do create scripts/validate-email.clj
(ns validate-email)

(defn valid-email? [email]
  (and (string? email)
       (re-matches #".+@.+\..+" email)))

; Then reference from SKILL.md:
; "To validate emails, use: `clojure scripts/validate-email.clj email@example.com`"
```

## Writing Effective SKILL.md

Structure your main Skill content like this:

``` markdown
# [Skill Name]

## Quick Start
[1-2 paragraphs showing the most basic usage. Goal: working example in 2 minutes]

```clojure
; Minimal working example
(require '[library.core :as lib])
(lib/basic-operation {:input "data"})
```

## Core Concepts

\[Explain 2-3 key ideas agents need to understand to use this
effectively\]

## Common Workflows

### Workflow 1: \[Name\]

\[Clear steps with inline example\]

``` clojure
; Example code
```

### Workflow 2: \[Name\]

\[Clear steps with inline example\]

### Workflow 3: \[Name\]

\[Clear steps with inline example\]

## When to Use Each Approach

\[Decision guide: when to use feature A vs feature B\]

## Best Practices

\[What to do and what to avoid\]

**Do**: - \[Good practice with brief explanation\]

**Don't**: - \[Anti-pattern with brief explanation\]

## Common Issues

\[2-3 most common problems and solutions\]

## Advanced Topics

For comprehensive API documentation, see [REFERENCE.md](REFERENCE.md)
For complex real-world examples, see [EXAMPLES.md](EXAMPLES.md)


    ## Discovery and Triggering

    Your Skill's `description` is the primary discovery mechanism. Make it specific and trigger-rich:

    **Good triggers**:
    ```yaml
    # Triggers on: "HTTP server", "web server", "API endpoint", "REST"
    description: |
      Build HTTP servers and REST APIs with http-kit. Use when creating web servers,
      handling HTTP requests, building REST endpoints, or when the user mentions
      servers, HTTP, web services, or APIs.

``` yaml
# Triggers on: "validation", "schema", "data contract", "validate"
description: |
  Validate data structures using Malli schemas. Use when validating API data,
  defining data contracts, building forms, or when the user mentions validation,
  schemas, data integrity, or type checking.
```

**Poor triggers**:

``` yaml
# Too vague - when would this trigger?
description: A Clojure HTTP library

# Missing context - doesn't say WHEN
description: Malli is a data validation library
```

## Validating Clojure Skills

Before publishing a Skill, **always validate with clojure_eval**:

### 1. Test All Code Examples

``` clojure
; Verify each example works
(require '[clojure.string :as str])
(str/upper-case "hello")  ; => "HELLO"

; Test edge cases
(str/upper-case "")       ; => ""
(str/upper-case nil)      ; What happens? Document it!
```

### 2. Verify Library Availability

``` clojure
; Check if the library exists
(clj-mcp.repl-tools/find-symbols "library-name")

; Explore available functions
(clj-mcp.repl-tools/list-vars 'library.core)

; Get documentation
(clj-mcp.repl-tools/doc-symbol 'library.core/function-name)
```

### 3. Test Scripts (if included)

``` bash
clojure scripts/your-script.clj "test input"
```

### 4. Verify Documentation Accuracy

-   Do examples produce stated results?
-   Are edge cases documented?
-   Is performance guidance accurate?

## Example: Well-Structured Clojure Skill

Here's a template showing good structure:

**File: SKILL.md**

``` markdown
---
name: malli-validation
description: |
  Validate data structures and schemas using Malli. Use when validating
  API requests/responses, defining data contracts, building form validation,
  or when the user mentions schemas, validation, or data integrity.
---

# Malli Data Validation

## Quick Start

Malli validates data against schemas. Schemas are just Clojure data:

```clojure
(require '[malli.core :as m])

; Define a schema
(def user-schema
  [:map
   [:name string?]
   [:email string?]
   [:age [:int {:min 0 :max 150}]]])

; Validate data
(m/validate user-schema {:name "Alice" :email "alice@example.com" :age 30})
; => true

(m/validate user-schema {:name "Bob" :age "thirty"})
; => false
```

## Core Concepts

### Schemas as Data

Schemas are Clojure data structures, not special objects. This makes
them composable and inspectable.

### Validation vs Coercion

-   **Validation**: Check if data matches schema (returns true/false)
-   **Coercion**: Transform data to match schema (e.g., string "123" →
    int 123)

## Common Workflows

### Validating API Requests

``` clojure
(def request-schema
  [:map
   [:user-id int?]
   [:action [:enum "create" "update" "delete"]]
   [:data map?]])

(m/validate request-schema
  {:user-id 42 :action "create" :data {:name "Test"}})
; => true
```

### Getting Detailed Errors

``` clojure
(m/explain user-schema {:name "Alice" :age "not-a-number"})
; => {:value {:name "Alice" :age "not-a-number"}
;     :errors [{:path [:age] :message "should be int"}]}
```

### Composing Schemas

``` clojure
(def address-schema [:map [:street string?] [:city string?]])
(def user-with-address
  [:map
   [:name string?]
   [:address address-schema]])  ; Reuse schema
```

## When to Use Each Approach

**Use Malli when**: - Validating external data (APIs, user input) - You
need detailed error messages - Schemas should be inspectable/modifiable
at runtime

**Use clojure.spec when**: - You need generative testing - Working with
existing spec-based libraries - Need instrumentation for development

**Use simple predicates when**: - Validation is trivial (`string?`,
`pos-int?`) - Performance is critical - No need for error messages

## Best Practices

**Do**: - Define schemas as constants for reuse - Use descriptive keys
in maps - Provide human-readable error messages - Test schemas with
valid and invalid data

**Don't**: - Recreate schemas inline (define once, reuse) - Make schemas
too strict (allow flexibility) - Ignore validation errors (always check
return values)

## Common Issues

### Schema Doesn't Match Expected Structure

``` clojure
; Wrong: expecting specific map keys
[:map [:name string?]]  ; Doesn't allow extra keys by default

; Right: allow extra keys
[:map {:closed false} [:name string?]]
```

### Performance with Large Data

Malli is fast, but validating huge nested structures can be slow.
Consider: - Validate at boundaries (API entry/exit) - Use sampling for
large collections - Cache compiled schemas

## Advanced Topics

For comprehensive schema reference, see [REFERENCE.md](REFERENCE.md) For
complex validation patterns, see [EXAMPLES.md](EXAMPLES.md)


    **File: REFERENCE.md** (Optional - detailed API)
    ```markdown
    # Malli Complete Reference

    ## Schema Types

    ### Primitive Types
    - `:int` - Integer
    - `:double` - Double
    - `:string` - String
    - `:boolean` - Boolean
    - `:keyword` - Keyword
    - `:symbol` - Symbol
    - `:uuid` - UUID

    [... comprehensive API documentation ...]

**File: EXAMPLES.md** (Optional - complex scenarios)

``` markdown
# Malli Advanced Examples

## Example 1: Multi-level Nested Validation

[... detailed real-world examples ...]

## Example 2: Custom Validators

[... detailed real-world examples ...]
```

## Multi-File Skill Example

For complex Skills with many topics:

**SKILL.md** - Quick start and core workflows (keeps context light)

``` markdown
# Complex Library

## Quick Start
[5-10 minutes to basic usage]

## Core Workflows
[3-5 common patterns with examples]

## Advanced Usage
- Comprehensive API: [REFERENCE.md](REFERENCE.md)
- Complex examples: [EXAMPLES.md](EXAMPLES.md)
- Utility scripts: [scripts/README.md](scripts/README.md)
```

**REFERENCE.md** - Exhaustive API documentation **EXAMPLES.md** -
Complex real-world scenarios **scripts/** - Executable utilities

This organization means: 1. Common tasks don't load extra files (fast,
efficient) 2. Advanced work pulls in reference materials on-demand 3. No
context penalty for bundled content that isn't used

## Best Practices for Clojure Skills

1.  **Test Everything**: Use `clojure_eval` to validate all code
    examples
2.  **Progressive Disclosure**: Quick start first, details in separate
    files
3.  **Show Alternatives**: When multiple approaches exist, explain when
    to use each
4.  **Include Edge Cases**: What happens with `nil`, empty collections,
    invalid types?
5.  **Provide Decision Guides**: Help agents choose between approaches
6.  **Link to Resources**: Reference official docs, GitHub repos
7.  **Keep SKILL.md Focused**: 3-5 core workflows, not exhaustive docs
8.  **Be Explicit About Performance**: When does it get slow? When is it
    fast?
9.  **Document Gotchas**: Common mistakes and how to avoid them
10. **Use Scripts for Complexity**: Don't make agents recreate complex
    logic

## Security Considerations

**Only create Skills from trusted sources**. Skills can execute code and
invoke tools, so:

-   Audit all code thoroughly
-   Be cautious with Skills fetching external data
-   Avoid Skills with unexpected network calls
-   Treat Skill creation like installing software
-   Never use untrusted Skills in production

## Checklist: Before Publishing a Skill

-   [ ] Metadata uses proper name format (lowercase, hyphens, max 64
    chars)
-   [ ] Description includes BOTH what it does AND when to use it
-   [ ] Description includes trigger keywords for discovery
-   [ ] All code examples tested with `clojure_eval`
-   [ ] SKILL.md has Quick Start section (5-10 minute time-to-value)
-   [ ] SKILL.md has 3-5 core workflows with inline examples
-   [ ] Advanced content moved to REFERENCE.md or EXAMPLES.md
-   [ ] Scripts tested and documented (if included)
-   [ ] Edge cases and error handling covered
-   [ ] Best practices and anti-patterns documented
-   [ ] Decision guides provided (when to use X vs Y)
-   [ ] Cross-references between files use correct markdown links
-   [ ] No XML tags in frontmatter fields
-   [ ] Performance considerations documented
-   [ ] Common issues and solutions included

## Tools Available for Skill Creation

Use these tools to understand Clojure features deeply before
documenting:

-   **`clojure_eval`**: Evaluate Clojure code to validate examples
-   **`clojure-mcp_read_file`**: Read and explore existing code
-   **`clojure-mcp_grep`**: Search for functions and patterns
-   **`clj-mcp.repl-tools/list-ns`**: Discover available namespaces
-   **`clj-mcp.repl-tools/list-vars`**: List functions in a namespace
-   **`clj-mcp.repl-tools/doc-symbol`**: Get function documentation
-   **`clj-mcp.repl-tools/source-symbol`**: View function source
-   **`clj-mcp.repl-tools/find-symbols`**: Search for symbols by pattern
-   **`clojure.repl.deps/add-lib`**: Load libraries dynamically into the
    REPL
-   **File tools**: Write and edit Skill files

### Loading Libraries Dynamically

When creating Skills for libraries not already on the classpath, you can
load them dynamically using `clojure.repl.deps/add-lib`:

``` clojure
;; Load a library at the REPL
(require '[clojure.repl.deps :refer [add-lib add-libs]])

;; Add a single library
(add-lib 'org.clojure/data.csv {:mvn/version "1.0.1"})

;; Now require and use it
(require '[clojure.data.csv :as csv])
(csv/write-csv *out* [["a" "b" "c"]])

;; Add multiple libraries at once (more efficient)
(add-libs '{org.clojure/data.json {:mvn/version "2.5.0"}
            metosin/malli {:mvn/version "0.16.0"}})
```

**When to use `add-lib`:** - Testing libraries before creating Skills -
Exploring API surfaces and function signatures - Validating code
examples with actual library behavior - Checking library compatibility
with current Clojure version

**Important notes:** - Libraries are added to the REPL session, not
permanently to project - Once added, libraries persist for the REPL
session - You still need to `require` the namespace after loading -
Requires Clojure 1.12+ and a valid parent DynamicClassLoader

**Example workflow for creating a Skill:**

``` clojure
;; 1. Load the library
(require '[clojure.repl.deps :refer [add-lib]])
(add-lib 'buddy/buddy-core {:mvn/version "1.11.0"})

;; 2. Explore what's available
(require '[buddy.core.hash :as hash])
(clj-mcp.repl-tools/list-vars 'buddy.core.hash)

;; 3. Test examples
(hash/sha256 "hello world")
;; => #object["[B" 0x1234abcd "[B@1234abcd"]

;; 4. Document the behavior in your Skill
;; Now you know exactly how it works!
```

This allows you to create accurate, tested Skills for any Clojure
library without needing to add it to the project dependencies.

## Summary: What Makes an Effective Clojure Skill

1.  **Clear, discoverable metadata** with trigger-rich descriptions
2.  **Focused instructions** that provide quick time-to-value
3.  **Progressive disclosure** - common tasks first, advanced topics in
    separate files
4.  **Thoroughly validated** with real Clojure evaluation
5.  **Decision guides** that help agents choose the right approach
6.  **Practical examples** with edge cases documented
7.  **Scripts for complexity** - don't make agents recreate logic
8.  **Composable design** - Skills work together to build capabilities

The goal: Transform agents into a Clojure specialist who can work
effectively with libraries, patterns, and best practices without needing
to repeatedly explain the same concepts.

Now go create excellent Clojure Skills!

# Clojure Introduction

Clojure is a functional Lisp for the JVM combining immutable data
structures, first-class functions, and practical concurrency support.

## Core Language Features

**Data Structures** (all immutable by default): - `{}` - Maps (key-value
pairs) - `[]` - Vectors (indexed sequences) - `#{}` - Sets (unique
values) - `'()` - Lists (linked lists)

**Functions**: Defined with `defn`. Functions are first-class and
support variadic arguments, destructuring, and composition.

**No OOP**: Use functions and data structures instead of classes.
Polymorphism via `multimethods` and `protocols`, not inheritance.

## How Immutability Works

All data structures are immutable---operations return new copies rather
than modifying existing data. This enables:

-   Safe concurrent access without locks
-   Easier testing and reasoning about code
-   Efficient structural sharing (new versions don't copy everything)

**Pattern**: Use `assoc`, `conj`, `update`, etc. to create modified
versions of data.

``` clojure
(def person {:name "Alice" :age 30})
(assoc person :age 31)  ; Returns new map, original unchanged
```

## State Management

When mutation is needed: - **`atom`** - Simple, synchronous updates:
`(swap! my-atom update-fn)` - **`ref`** - Coordinated updates in
transactions: `(dosync (alter my-ref update-fn))` - **`agent`** -
Asynchronous updates: `(send my-agent update-fn)`

## Key Functions

Most operations work on sequences. Common patterns: - `map`, `filter`,
`reduce` - Transform sequences - `into`, `conj` - Build collections -
`get`, `assoc`, `dissoc` - Access/modify maps - `->`, `->>` - Threading
macros for readable pipelines

## Code as Data

Clojure programs are data structures. This enables: - **Macros** - Write
code that writes code - **Easy metaprogramming** - Inspect and transform
code at runtime - **REPL-driven development** - Test functions
interactively

## Java Interop

Call Java directly: `(ClassName/staticMethod)` or `(.method object)`.
Access Java libraries seamlessly.

## Why Clojure

-   **Pragmatic** - Runs on stable JVM infrastructure
-   **Concurrency-first** - Immutability + agents/STM handle multi-core
    safely
-   **Expressive** - Less boilerplate than Java, more powerful
    abstractions
-   **Dynamic** - REPL feedback, no compile-test-deploy cycle needed

# Clojure REPL

## Quick Start

The REPL (Read-Eval-Print Loop) is Clojure's interactive programming
environment. It reads expressions, evaluates them, prints results, and
loops. The REPL provides the full power of Clojure - you can run any
program by typing it at the REPL.

``` clojure
user=> (+ 2 3)
5
user=> (defn greet [name] (str "Hello, " name))
#'user/greet
user=> (greet "World")
"Hello, World"
```

## Core Concepts

### Read-Eval-Print Loop

The REPL **R**eads your expression, **E**valuates it, **P**rints the
result, and **L**oops to repeat. Every expression you type produces a
result that is printed back to you.

### Side Effects vs Return Values

Understanding the difference between side effects and return values is
crucial:

``` clojure
user=> (println "Hello World")
Hello World    ; <- Side effect: printed by your code
nil            ; <- Return value: printed by the REPL
```

-   `Hello World` is a **side effect** - output printed by `println`
-   `nil` is the **return value** - what `println` returns (printed by
    REPL)

### Namespace Management

Libraries must be loaded before you can use them or query their
documentation:

``` clojure
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

### Exploring with clj-mcp.repl-tools (Recommended for Agents)

The `clj-mcp.repl-tools` namespace provides enhanced REPL utilities
optimized for programmatic access and agent workflows. These functions
return structured data instead of printing, making them more suitable
for automated code exploration.

#### Getting Started

``` clojure
;; See all available functions
(clj-mcp.repl-tools/help)

;; Or use an alias for convenience
(require '[clj-mcp.repl-tools :as rt])
```

#### list-ns - List All Namespaces

Discover what namespaces are loaded:

``` clojure
(clj-mcp.repl-tools/list-ns)
; Returns a seq of all loaded namespace symbols
; => (clojure.core clojure.string clojure.set ...)
```

**Use when**: You need to see what's available in the current
environment.

#### list-vars - List Functions in a Namespace

Explore the contents of a namespace:

``` clojure
(clj-mcp.repl-tools/list-vars 'clojure.string)
; Returns formatted documentation for all public vars:
;
; Vars in clojure.string:
; -------------------------------------------
; blank?
;   ([s])
;   True if s is nil, empty, or contains only whitespace.
;
; capitalize
;   ([s])
;   Converts first character of the string to upper-case...
; ...
```

**Use when**: You know the namespace but need to discover available
functions.

#### doc-symbol - View Function Documentation

Get documentation for a specific symbol:

``` clojure
(clj-mcp.repl-tools/doc-symbol 'map)
; -------------------------
; map - Returns a lazy sequence consisting of the result of applying f to...
;   Defined in: clojure.core
;   Arguments: ([f] [f coll] [f c1 c2] [f c1 c2 c3] [f c1 c2 c3 & colls])
;   Added in: 1.0
; -------------------------

(clj-mcp.repl-tools/doc-symbol 'clojure.string/upper-case)
; -------------------------
; upper-case - Converts string to all upper-case.
;   Defined in: clojure.string
;   Arguments: ([s])
;   Added in: 1.2
; -------------------------
```

**Use when**: You need to understand how to use a specific function.

#### doc-namespace - Document an Entire Namespace

View namespace-level documentation:

``` clojure
(clj-mcp.repl-tools/doc-namespace 'clojure.string)
; Shows namespace docstring and overview
```

**Use when**: You need to understand the purpose and scope of a
namespace.

#### source-symbol - View Source Code

See the actual implementation:

``` clojure
(clj-mcp.repl-tools/source-symbol 'some?)
; Returns the source code as a string
```

**Use when**: You need to understand how something is implemented or
learn from existing code patterns.

#### find-symbols - Search for Symbols

Find symbols by name pattern:

``` clojure
;; Search by substring
(clj-mcp.repl-tools/find-symbols "map")
; Symbols matching 'map':
;   clojure.core/map
;   clojure.core/map-indexed
;   clojure.core/mapv
;   clojure.core/mapcat
;   clojure.set/map-invert
;   ...

;; Search by regex
(clj-mcp.repl-tools/find-symbols #".*index.*")
; Returns all symbols containing "index"
```

**Use when**: You remember part of a function name or want to find
related functions.

#### complete - Autocomplete Symbol Names

Get completions for a prefix:

``` clojure
(clj-mcp.repl-tools/complete "clojure.string/u")
; Completions for 'clojure.string/u':
;   clojure.string/upper-case
```

**Use when**: You know the beginning of a function name and want to see
matches.

#### describe-spec - Explore Clojure Specs

View detailed spec information:

``` clojure
(clj-mcp.repl-tools/describe-spec :my/spec)
; Shows spec details, form, and examples
```

**Use when**: Working with clojure.spec and need to understand spec
definitions.

### Exploring with clojure.repl (Standard Library)

The `clojure.repl` namespace provides standard REPL utilities. These
print directly to stdout, which is suitable for interactive use but less
convenient for programmatic access.

**Load it first**:

``` clojure
(require '[clojure.repl :refer :all])
```

#### doc - View Function Documentation

``` clojure
(doc map)
; -------------------------
; clojure.core/map
; ([f] [f coll] [f c1 c2] [f c1 c2 c3] [f c1 c2 c3 & colls])
;   Returns a lazy sequence consisting of the result of applying f to...
```

**Note**: Prints to stdout. Use `clj-mcp.repl-tools/doc-symbol` for
programmatic access.

#### source - View Source Code

``` clojure
(source some?)
; (defn some?
;   "Returns true if x is not nil, false otherwise."
;   {:tag Boolean :added "1.6" :static true}
;   [x] (not (nil? x)))
```

Requires `.clj` source files on classpath.

#### dir - List Namespace Contents

``` clojure
(dir clojure.string)
; blank?
; capitalize
; ends-with?
; ...
```

#### apropos - Search by Name

``` clojure
(apropos "index")
; (clojure.core/indexed?
;  clojure.core/keep-indexed
;  clojure.string/index-of
;  ...)
```

#### find-doc - Search Documentation

``` clojure
(find-doc "indexed")
; Searches docstrings across all loaded namespaces
```

### Debugging Exceptions

#### Using clojure.repl for Stack Traces

**pst - Print Stack Trace**:

``` clojure
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

**Special REPL vars**: - `*e` - Last exception thrown - `*1` - Result of
last expression - `*2` - Result of second-to-last expression - `*3` -
Result of third-to-last expression

**root-cause - Find Original Exception**:

``` clojure
(root-cause *e)
; Returns the initial cause by peeling off exception wrappers
```

**demunge - Readable Stack Traces**:

``` clojure
(demunge "clojure.core$map")
; => "clojure.core/map"
```

Useful when reading raw stack traces from Java exceptions.

### Interactive Development Pattern

1.  **Start small**: Test individual expressions
2.  **Build incrementally**: Define functions and test them immediately
3.  **Explore unknown territory**: Use `clj-mcp.repl-tools` or
    `clojure.repl` to understand libraries
4.  **Debug as you go**: Test each piece before moving forward
5.  **Iterate rapidly**: Change code and re-evaluate

``` clojure
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

In Clojure 1.12+, you can add dependencies at the REPL without
restarting:

``` clojure
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

**Note**: Requires a valid parent `DynamicClassLoader`. Works in
standard REPL but may not work in all environments.

## When to Use Each Tool

### clj-mcp.repl-tools vs clojure.repl

**Use clj-mcp.repl-tools when**: - Building automated workflows or
agent-driven code exploration - You need structured data instead of
printed output - Working programmatically with REPL information - You
want consistent, parseable output formats - You need enhanced features
like `list-ns`, `complete`, `describe-spec`

**Use clojure.repl when**: - Working interactively at a human REPL - You
prefer traditional Clojure REPL tools - Output directly to console is
desired - Working in environments without clj-mcp.repl-tools

### Function Comparison

  Task                 clj-mcp.repl-tools   clojure.repl
  -------------------- -------------------- --------------
  List namespaces      `list-ns`            N/A
  List vars            `list-vars`          `dir`
  Show documentation   `doc-symbol`         `doc`
  Show source          `source-symbol`      `source`
  Search symbols       `find-symbols`       `apropos`
  Search docs          `find-symbols`       `find-doc`
  Autocomplete         `complete`           N/A
  Namespace docs       `doc-namespace`      N/A
  Spec info            `describe-spec`      N/A

**For agents**: Prefer `clj-mcp.repl-tools` as it's designed for
programmatic use.

**For humans**: Either works, but `clojure.repl` is the standard
approach.

## Best Practices

**Do**: - **Use `clj-mcp.repl-tools` for agent workflows** - Returns
structured data - Test expressions incrementally before combining them -
Use `doc-symbol` or `doc` liberally to learn from existing code - Keep
the REPL open during development for rapid feedback - Use `:reload` flag
when re-requiring changed namespaces: `(require 'my.ns :reload)` -
Experiment freely - the REPL is a safe sandbox - Start with `list-ns` to
discover available namespaces - Use `list-vars` to explore namespace
contents

**Don't**: - Paste large blocks of code without testing pieces first -
Forget to require namespaces before trying to use them - Ignore
exceptions - use `pst` to understand what went wrong - Rely on side
effects during development without understanding return values - Skip
documentation lookup when working with unfamiliar functions

## Common Issues

### "Unable to resolve symbol"

``` clojure
user=> (str/upper-case "hello")
; CompilerException: Unable to resolve symbol: str/upper-case
```

**Solution**: Require the namespace first:

``` clojure
(require '[clojure.string :as str])
(str/upper-case "hello")  ; => "HELLO"
```

### "No documentation found" with clojure.repl/doc

``` clojure
(doc clojure.set/union)
; nil  ; No doc found
```

**Solution**: Documentation only available after requiring:

``` clojure
(require '[clojure.set])
(doc clojure.set/union)  ; Now works
```

**Or use clj-mcp.repl-tools** which can find symbols across namespaces:

``` clojure
(clj-mcp.repl-tools/doc-symbol 'clojure.set/union)
; Works even if namespace not required
```

### "Can't find source"

``` clojure
(source my-function)
; Source not found
```

**Solution**: `source` requires `.clj` files on classpath. Works for: -
Clojure core functions - Library functions with source on classpath -
Your project's functions when running from source

Won't work for: - Functions in compiled-only JARs - Java methods -
Dynamically generated functions

### Stale definitions after file changes

When you edit a source file and reload it:

``` clojure
;; Wrong - might keep old definitions
(require 'my.namespace)

;; Right - forces reload
(require 'my.namespace :reload)

;; Or reload all dependencies too
(require 'my.namespace :reload-all)
```

## Development Workflow Tips

1.  **Start with exploration**: Use `list-ns` and `list-vars` to
    discover what's available
2.  **Keep a scratch namespace**: Use `user` namespace for experiments
3.  **Save useful snippets**: Copy successful REPL experiments to your
    editor
4.  **Use editor integration**: Most Clojure editors can send code to
    REPL
5.  **Check return values**: Always verify what functions return, not
    just side effects
6.  **Explore before implementing**: Use `doc-symbol`, `source-symbol`
    to understand libraries
7.  **Test edge cases**: Try `nil`, empty collections, invalid inputs at
    REPL
8.  **Use REPL-driven testing**: Develop tests alongside code in REPL
9.  **Leverage autocomplete**: Use `complete` to discover function names
10. **Search intelligently**: Use `find-symbols` with patterns to locate
    related functions

## Example: Exploring an Unknown Namespace

``` clojure
;; 1. Discover available namespaces
(clj-mcp.repl-tools/list-ns)
; See clojure.string in the list

;; 2. Explore the namespace
(clj-mcp.repl-tools/list-vars 'clojure.string)
; See all available functions with documentation

;; 3. Find relevant functions
(clj-mcp.repl-tools/find-symbols "upper")
; => clojure.string/upper-case

;; 4. Get detailed documentation
(clj-mcp.repl-tools/doc-symbol 'clojure.string/upper-case)
; See parameters and usage

;; 5. View implementation if needed
(clj-mcp.repl-tools/source-symbol 'clojure.string/upper-case)

;; 6. Test it
(require '[clojure.string :as str])
(str/upper-case "hello")
; => "HELLO"
```

## Summary

The Clojure REPL is your primary development tool:

### For Agent Workflows (Recommended):

-   **Explore namespaces**: `(clj-mcp.repl-tools/list-ns)`
-   **List functions**: `(clj-mcp.repl-tools/list-vars 'namespace)`
-   **Get documentation**: `(clj-mcp.repl-tools/doc-symbol 'function)`
-   **Search symbols**: `(clj-mcp.repl-tools/find-symbols "pattern")`
-   **Autocomplete**: `(clj-mcp.repl-tools/complete "prefix")`
-   **View source**: `(clj-mcp.repl-tools/source-symbol 'function)`

### For Interactive Development:

-   **Evaluate immediately**: Get instant feedback on every expression
-   **Explore actively**: Use `doc`, `source`, `dir`, `apropos`,
    `find-doc`
-   **Debug interactively**: Use `pst`, `root-cause`, and special vars
    like `*e`
-   **Develop iteratively**: Build and test small pieces, then combine
-   **Learn continuously**: Read source code and documentation as you
    work

Master REPL-driven development and you'll write better Clojure code
faster.

# Clojure REPL Evaluation

## Quick Start

The `clojure_eval` tool evaluates Clojure code instantly, giving you
immediate feedback. This is your primary way to test ideas, validate
code, and explore libraries.

``` clojure
; Simple evaluation
(+ 1 2 3)
; => 6

; Test a function
(defn greet [name]
  (str "Hello, " name "!"))

(greet "Alice")
; => "Hello, Alice!"

; Multiple expressions evaluated in sequence
(def x 10)
(* x 2)
(+ x 5)
; => 10, 20, 15
```

**Key benefits:** - **Instant feedback** - Know if code works
immediately - **Safe experimentation** - Test without modifying files -
**Auto-linting** - Syntax errors caught before evaluation -
**Auto-balancing** - Parentheses fixed automatically when possible

## Core Workflows

### Workflow 1: Test Before You Commit to Files

Always validate logic in the REPL before using `clojure_edit` to modify
files:

``` clojure
; 1. Develop and test in REPL
(defn valid-email? [email]
  (and (string? email)
       (re-matches #".+@.+\..+" email)))

; 2. Test with various inputs
(valid-email? "alice@example.com")  ; => true
(valid-email? "invalid")            ; => false
(valid-email? nil)                  ; => false

; 3. Once validated, use clojure_edit to add to files
; 4. Reload and verify
(require '[my.namespace :reload])
(my.namespace/valid-email? "test@example.com")
```

### Workflow 2: Explore Libraries and Namespaces

Use built-in helper functions to discover what's available:

``` clojure
; Find all namespaces
(clj-mcp.repl-tools/list-ns)

; List functions in a namespace
(clj-mcp.repl-tools/list-vars 'clojure.string)

; Get documentation
(clj-mcp.repl-tools/doc-symbol 'map)

; View source code
(clj-mcp.repl-tools/source-symbol 'clojure.string/join)

; Find functions by pattern
(clj-mcp.repl-tools/find-symbols "seq")

; Get completions
(clj-mcp.repl-tools/complete "clojure.string/j")

; Show all available helpers
(clj-mcp.repl-tools/help)
```

**When to use each helper:** - `list-ns` - "What namespaces are
available?" - `list-vars` - "What functions does this namespace have?" -
`doc-symbol` - "How do I use this function?" - `source-symbol` - "How is
this implemented?" - `find-symbols` - "What functions match this
pattern?" - `complete` - "I know part of the function name..."

### Workflow 3: Debug with Incremental Testing

Break complex problems into small, testable steps:

``` clojure
; Start with sample data
(def users [{:name "Alice" :age 30}
            {:name "Bob" :age 25}
            {:name "Charlie" :age 35}])

; Test each transformation step
(filter #(> (:age %) 26) users)
; => ({:name "Alice" :age 30} {:name "Charlie" :age 35})

(map :name (filter #(> (:age %) 26) users))
; => ("Alice" "Charlie")

(clojure.string/join ", " (map :name (filter #(> (:age %) 26) users)))
; => "Alice, Charlie"
```

Each step is validated before adding the next transformation.

### Workflow 4: Reload After File Changes

After modifying files with `clojure_edit`, always reload and test:

``` clojure
; Reload the namespace to pick up file changes
(require '[my.app.core :reload])

; Test the updated function
(my.app.core/my-new-function "test input")

; If there's an error, debug in the REPL
(my.app.core/helper-function "debug this")
```

**Important:** The `:reload` flag is required to force recompilation
from disk.

## When to Use Each Approach

### Use `clojure_eval` When:

-   Testing if code works before committing to files
-   Exploring libraries and discovering functions
-   Debugging issues with small test cases
-   Validating assumptions about data
-   Prototyping solutions quickly
-   Learning how functions behave

### Use `clojure_edit` When:

-   You've validated code works in the REPL
-   Making permanent changes to source files
-   Adding new functions or modifying existing ones
-   Code is ready to be part of the codebase

### Combined Workflow:

1.  **Explore** with `clojure_eval` and helper functions
2.  **Prototype** solution in REPL
3.  **Validate** it works with test cases
4.  **Edit files** with `clojure_edit`
5.  **Reload and verify** with `clojure_eval`

## Best Practices

**Do:** - Test small expressions incrementally - Validate each step
before adding complexity - Use helper functions to explore before
coding - Reload namespaces after file changes with `:reload` - Test edge
cases (nil, empty collections, invalid inputs) - Keep experiments
focused and small

**Don't:** - Skip validation - always test before committing to files -
Build complex logic all at once without testing steps - Assume cached
definitions match file contents - reload first - Use REPL for
long-running operations (use files/tests instead) - Forget to test error
cases

## Common Issues

### Issue: "Undefined symbol or namespace"

``` clojure
; Problem
(clojure.string/upper-case "hello")
; => Error: Could not resolve symbol: clojure.string/upper-case

; Solution: Require the namespace first
(require '[clojure.string :as str])
(str/upper-case "hello")
; => "HELLO"
```

### Issue: "Changes not appearing after file edit"

``` clojure
; Problem: Modified file but function still has old behavior

; Solution: Use :reload to force recompilation
(require '[my.namespace :reload])

; Now test the updated function
(my.namespace/my-function)
```

### Issue: "NullPointerException"

``` clojure
; Problem: Calling method on nil
(.method nil)

; Solution: Test for nil first or use safe navigation
(when-let [obj (get-object)]
  (.method obj))

; Or provide a default
(-> obj (or {}) :field)
```

## Advanced Topics

For comprehensive documentation on all REPL helper functions, see
[REFERENCE.md](REFERENCE.md)

For complex real-world development scenarios and patterns, see
[EXAMPLES.md](EXAMPLES.md)

## Summary

`clojure_eval` is your feedback loop for REPL-driven development:

1.  **Test before committing** - Validate in REPL, then use
    `clojure_edit`
2.  **Explore intelligently** - Use helper functions to discover
3.  **Debug incrementally** - Break problems into small testable steps
4.  **Always reload** - Use `:reload` after file changes
5.  **Validate everything** - Never skip testing, even simple code

Master the REPL workflow and you'll write better code faster.
