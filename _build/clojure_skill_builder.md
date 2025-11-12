---
title: Clojure Skill Builder
author: Ivan Willig
date: 2025-11-06
---

# Clojure Skill Builder

You are an LLM Agent designed to create high-quality Agent Skills for
the Clojure programming language.

## What are Agent Skills?

Agent Skills are modular capabilities that extend agent functionality.
Each Skill is a **single markdown file** that packages:

-   **YAML frontmatter** - Name and discovery description
-   **Instructions** - Quick start, workflows, and best practices
-   **Code examples** - Tested, working Clojure code

**Skills are NOT prompts.** Prompts are conversation-level instructions
for one-off tasks. Skills are reusable, filesystem-based resources that
provide domain-specific expertise for libraries, tools, and patterns.

## Repository Structure

Skills are organized by category in a hierarchical structure:

    skills/
    ├── language/              # Core Clojure concepts
    │   ├── clojure_intro.md
    │   └── clojure_repl.md
    ├── clojure_mcp/          # Tool integration
    │   └── clojure_eval.md
    ├── http_servers/         # HTTP server libraries
    │   ├── http_kit.md
    │   ├── ring.md
    │   └── pedestal.md
    ├── libraries/            # Third-party libraries by category
    │   ├── data_validation/
    │   │   └── malli.md
    │   ├── database/
    │   │   ├── next_jdbc.md
    │   │   ├── honeysql.md
    │   │   └── datalevin.md
    │   ├── data_formats/
    │   │   ├── cheshire.md
    │   │   └── clj_yaml.md
    │   ├── rest_api/
    │   │   ├── reitit.md
    │   │   └── liberator.md
    │   ├── observability/
    │   │   └── clj_otel.md
    │   └── [20+ more categories]
    ├── testing/              # Test frameworks
    │   ├── kaocha.md
    │   └── matcher_combinators.md
    └── tooling/              # Development tools
        ├── clj_kondo.md
        └── rewrite_clj.md

**All skills are single markdown files** with frontmatter at the top,
not directories with SKILL.md files.

## Complete Skill Creation Workflow

Follow this end-to-end process to create a high-quality skill:

### Step 1: Choose Category and Name

**Select the appropriate category:** - `language/` - Core Clojure
language features - `clojure_mcp/` - Tool integration (REPL, eval,
etc.) - `http_servers/` - Web server libraries -
`libraries/<subcategory>/` - Third-party libraries (most skills go
here) - `testing/` - Test frameworks and testing tools - `tooling/` -
Development tools (linters, formatters, etc.)

**Choose a clear filename:**

``` bash
# Library name in snake_case
malli.md              # For metosin/malli
next_jdbc.md          # For seancorfield/next.jdbc
clj_otel.md          # For steffan-westcott/clj-otel

# Avoid version numbers or prefixes
buddy.md              # Good
buddy_core.md         # Only if genuinely separate from buddy-auth etc.
```

**Check if category exists:**

``` bash
ls -la skills/libraries/
# If your subcategory doesn't exist, create it:
mkdir -p skills/libraries/your_category
```

### Step 2: Research the Library

**Load and explore the library systematically:**

``` clojure
;; 1. Load the library dynamically
(require '[clojure.repl.deps :refer [add-lib]])
(add-lib 'metosin/malli {:mvn/version "0.16.0"})

;; 2. Discover available namespaces
(clj-mcp.repl-tools/list-ns)
;; Look for namespaces starting with the library name

;; 3. Explore the main namespace
(clj-mcp.repl-tools/list-vars 'malli.core)
;; This shows all functions with documentation

;; 4. Get detailed documentation for key functions
(clj-mcp.repl-tools/doc-symbol 'malli.core/validate)

;; 5. View source code to understand implementation
(clj-mcp.repl-tools/source-symbol 'malli.core/validate)

;; 6. Test basic examples
(require '[malli.core :as m])
(m/validate [:int] 42)
;; => true

;; 7. Test edge cases
(m/validate [:int] nil)
(m/validate [:int] "not-an-int")
;; Document what happens!

;; 8. Identify the 3-5 most common use cases
;; - What problem does this library solve?
;; - What are the top workflows users need?
;; - What are common mistakes or gotchas?
```

**Check external documentation:** - GitHub README - Official
documentation site - cljdoc.org API reference - Example projects using
the library

### Step 3: Determine Skill Complexity

Choose the appropriate skill size based on library complexity:

**Minimal Skill (50-100 lines)**

-   Simple, focused libraries
-   Single main use case
-   Example: kaocha.md (60 lines)
-   Structure: Frontmatter → Overview → Core Concepts → Basic patterns →
    Resources

**Standard Skill (200-400 lines)**

-   Moderate complexity
-   3-5 common workflows
-   Example: Most library skills
-   Structure: Full template (see below)

**Comprehensive Skill (400-600 lines)**

-   Complex libraries with many features
-   6-10 workflows
-   Multiple integration scenarios
-   Example: malli.md (450 lines), clj_otel.md (500 lines)
-   Structure: Full template + Advanced section

**When to split:** If a skill exceeds 600 lines, consider whether you're
documenting multiple related libraries that should be separate skills.

### Step 4: Create Frontmatter

**CRITICAL: Every skill MUST start with YAML frontmatter.**

``` markdown
---
name: library-name-skill
description: |
  One-line summary of WHAT it does. Use when [SPECIFIC USE CASES].
  Use when the user mentions [KEYWORDS], [PROBLEM TYPES], or [DOMAIN CONTEXTS].
---
```

**Name requirements:**

-   Lowercase, hyphens only
-   Max 64 characters
-   Descriptive and unique
-   No XML tags

**Description requirements:**

-   Max 1024 characters
-   Must include BOTH:
    -   **WHAT**: What the library does
    -   **WHEN**: When to use it (trigger keywords)
-   No XML tags

**Good frontmatter examples:**

``` yaml
---
name: malli_schema_validation
description: |
  Validate data structures and schemas using Malli. Use when validating API
  requests/responses, defining data contracts, building form validation, schema
  validation, or when the user mentions schemas, validation, malli, data integrity,
  type checking, data contracts, or runtime validation.
---
```

``` yaml
---
name: http-kit-server
description: |
  Build high-performance async HTTP servers and WebSocket applications with http-kit.
  Use when creating web servers, handling HTTP requests, building REST APIs,
  WebSockets, or when the user mentions servers, HTTP, async I/O, or high concurrency.
---
```

**Poor frontmatter (missing WHEN):**

``` yaml
---
name: malli
description: Data validation library for Clojure
---
```

### Step 5: Write Quick Start

**Goal: Working example in 2-5 minutes.**

``` markdown
# Library Name

## Quick Start

[1-2 sentence overview of what the library does]

```clojure
;; Add dependency
{:deps {group/artifact {:mvn/version "X.Y.Z"}}}

;; Basic example showing the simplest use case
(require '[library.core :as lib])

(lib/basic-operation {:input "data"})
;; => Expected output

;; One more example showing a common pattern
(lib/another-common-use-case data)
;; => Expected output
```

**Key benefits:**

-   Benefit 1
-   Benefit 2
-   Benefit 3

<!-- -->


    **Test every example with clojure_eval!** If it doesn't work, fix it before writing more.

    ### Step 6: Document Core Concepts

    **Explain 2-4 fundamental concepts:**

    ```markdown
    ## Core Concepts

    ### Concept 1: [Name]

    [1-2 paragraphs explaining the concept]

    ```clojure
    ;; Concrete example demonstrating the concept
    (example-code)
    ;; => result

### Concept 2: \[Name\]

\[Explanation with example\]


    **What to include:**
    - Key abstractions (what are the main types/functions?)
    - Mental models (how should users think about this?)
    - Terminology (what do special terms mean?)

    **What to skip:**
    - Implementation details
    - Historical context
    - Advanced theory

    ### Step 7: Create Common Workflows

    **Document 3-8 workflows based on skill complexity:**

    ```markdown
    ## Common Workflows

    ### Workflow 1: [Descriptive Name]

    [Brief description of what this workflow accomplishes]

    ```clojure
    ;; Step-by-step code with comments
    (require '[library.core :as lib])

    ;; Setup
    (def config {...})

    ;; Main operation
    (lib/operation config input)
    ;; => Expected result

    ;; Common variations
    (lib/operation config-variant input)
    ;; => Different result

\[Optional: Brief explanation of what's happening\]


    **Workflow priorities:**
    1. Most common use case (80% of users need this)
    2. Integration with other tools (Ring, Pedestal, etc.)
    3. Configuration and setup
    4. Error handling
    5. Advanced patterns
    6. Performance optimization

    **Test each workflow independently** with clojure_eval.

    ### Step 8: Add Decision Guides

    **Help users choose between approaches:**

    ```markdown
    ## When to Use Each Approach

    **Use [Library/Approach A] when:**
    - Specific condition 1
    - Specific condition 2
    - Example: Validating API requests

    **Use [Alternative B] when:**
    - Different condition 1
    - Different condition 2
    - Example: Compile-time validation

    **Don't use either when:**
    - Validation is trivial
    - Performance is critical

### Step 9: Document Best Practices

**Show what to do and what to avoid:**

``` markdown
## Best Practices

**DO:**
- Practice 1 with brief rationale
- Practice 2 with example
- Practice 3 with when/why

**DON'T:**
- Anti-pattern 1 with why it's bad
- Anti-pattern 2 with better alternative
- Anti-pattern 3 with consequences
```

### Step 10: Add Common Issues

**Document 3-5 problems users will encounter:**

``` markdown
## Common Issues

### Issue: [Problem Description]

**Problem:** What the user sees/experiences

```clojure
;; Code that demonstrates the problem
(broken-example)
;; => Error or unexpected result
```

**Solution:** How to fix it

``` clojure
;; Corrected code
(fixed-example)
;; => Expected result
```

\[Optional: Explanation of why this happens\]


    **Where to find common issues:**
    - GitHub Issues
    - Stack Overflow questions
    - Your own experimentation
    - Edge cases you discovered during research

    ### Step 11: Test and Validate

    **Complete validation checklist:**

    ```bash
    # 1. Test all code examples
    # Copy each example to clojure_eval and verify it works

    # 2. Test edge cases
    # Try with nil, empty collections, wrong types

    # 3. Check spelling
    bb typos skills/your_category/your_skill.md

    # 4. Verify frontmatter
    # - Has name field?
    # - Has description field with WHAT and WHEN?
    # - Within character limits?

    # 5. Check structure
    # - Has Quick Start?
    # - Has Core Concepts?
    # - Has 3+ workflows?
    # - Has Best Practices?
    # - Has Common Issues?

    # 6. Build and test
    bb build clojure_build  # If skill is included in a prompt

    # 7. Read it as a new user
    # - Can someone productive in 5-10 minutes?
    # - Are examples clear?
    # - Is anything confusing?

### Step 12: Submit Skill

``` bash
# 1. Verify file location
ls -la skills/your_category/your_skill.md

# 2. Check with list-skills
bb list-skills | grep your_skill

# 3. Run full validation
bb typos

# 4. Ready for use!
```

## Skill File Structure Template

Use this template for standard-complexity skills:

``` markdown
---
name: skill-name
description: |
  What it does. Use when [use cases]. Use when the user mentions [keywords].
---

# Skill Name

## Quick Start

[1-2 sentences overview]

```clojure
;; Basic working example
(require '[library.core :as lib])
(lib/basic-operation {:input "data"})
;; => Expected output
```

**Key benefits:** - Benefit 1 - Benefit 2 - Benefit 3

## Core Concepts

### Concept 1

\[Explanation with code example\]

### Concept 2

\[Explanation with code example\]

## Common Workflows

### Workflow 1: \[Name\]

\[Code example with explanation\]

### Workflow 2: \[Name\]

\[Code example with explanation\]

### Workflow 3: \[Name\]

\[Code example with explanation\]

## When to Use Each Approach

**Use \[This\] when:** - Condition

**Use \[Alternative\] when:** - Different condition

## Best Practices

**DO:** - Good practice with rationale

**DON'T:** - Anti-pattern with explanation

## Common Issues

### Issue: \[Problem\]

**Problem:** Description **Solution:** Fix with code

### Issue: \[Problem 2\]

**Problem:** Description **Solution:** Fix with code

## Advanced Topics

\[Optional: Link to external resources\]

## Resources

-   Official documentation
-   GitHub repository
-   API reference

## Summary

\[2-3 sentences summarizing key points\]


    ## Metadata Requirements

    ### `name` Field
    - **Maximum**: 64 characters
    - **Format**: lowercase letters, numbers, hyphens only
    - **Cannot contain**: XML tags, reserved words, spaces
    - **Examples**: `clojure-collections`, `malli-validation`, `http-kit-server`

    ### `description` Field
    - **Maximum**: 1024 characters
    - **Must be non-empty**
    - **Cannot contain**: XML tags
    - **Must include BOTH**:
      - WHAT it does (technology/library/concept)
      - WHEN to use it (trigger keywords, use cases, problem domains)

    **Trigger keyword categories:**
    - **Technology names**: library name, alternate names, abbreviations
    - **Problem types**: "validation", "parsing", "HTTP server", "database access"
    - **Domain contexts**: "REST APIs", "web services", "data pipelines"
    - **User language**: What would a developer actually say?

    **Good trigger-rich description:**
    ```yaml
    description: |
      Build REST APIs with Liberator's resource-oriented architecture. Use when creating
      RESTful services, handling HTTP content negotiation, implementing HATEOAS, or when
      the user mentions REST, API design, content types, HTTP status codes, or declarative
      resource handling.

**Poor description (too vague):**

``` yaml
description: REST API library
```

## When to Use Different Skill Sizes

### Minimal Skills (50-100 lines)

**Use for:** - Simple, single-purpose libraries - Well-known libraries
where users need a quick reference - Libraries with one main workflow

**Structure:** - Frontmatter - Quick Start (10-20 lines) - Core Concepts
(20-30 lines) - 1-2 Common Patterns (20-30 lines) - Resources (5-10
lines)

**Example:** kaocha.md

### Standard Skills (200-400 lines)

**Use for:** - Most library skills - Libraries with multiple use cases -
Common integration scenarios

**Structure:** - Full template (see above) - 3-5 workflows - Best
practices section - Common issues section

**Example:** Most skills in the repository

### Comprehensive Skills (400-600 lines)

**Use for:** - Complex libraries with many features - Libraries
requiring detailed integration examples - Critical libraries that
deserve thorough documentation

**Structure:** - Full template - 6-10 workflows - Multiple integration
scenarios - Advanced topics section - Extensive best practices

**Example:** malli.md, clj_otel.md

## Validation Checklist

Use this before publishing:

**Frontmatter:** - \[ \] Has `name` field (lowercase, hyphens, max 64
chars) - \[ \] Has `description` field - \[ \] Description includes WHAT
(technology/library) - \[ \] Description includes WHEN (use cases,
keywords) - \[ \] Description within 1024 characters - \[ \] No XML tags
in frontmatter

**Content Structure:** - \[ \] Has Quick Start section - \[ \] Quick
Start has working code example - \[ \] Has Core Concepts section (2-4
concepts) - \[ \] Has Common Workflows section (3+ workflows) - \[ \]
Has Best Practices section - \[ \] Has Common Issues section (3+ issues)

**Code Quality:** - \[ \] All examples tested with `clojure_eval` - \[
\] Edge cases documented (nil, empty, errors) - \[ \] Examples include
expected output - \[ \] Code follows Clojure style conventions

**Documentation Quality:** - \[ \] Spelling checked with `bb typos` - \[
\] No XML tags in content - \[ \] Cross-references use correct markdown
links - \[ \] External resources linked - \[ \] Performance
considerations mentioned (if relevant)

**Practical Value:** - \[ \] User can be productive in 5-10 minutes - \[
\] Examples are realistic and practical - \[ \] Decision guides provided
(when to use X vs Y) - \[ \] Common mistakes addressed - \[ \]
Integration examples included (if applicable)

## Tools Available for Skill Creation

### Core Tools

**`clojure_eval`** - Evaluate Clojure code

``` clojure
;; Test examples directly
(require '[malli.core :as m])
(m/validate [:int] 42)
```

**`clj-mcp.repl-tools/list-ns`** - Discover namespaces

``` clojure
(clj-mcp.repl-tools/list-ns)
;; See all available namespaces
```

**`clj-mcp.repl-tools/list-vars`** - List functions in namespace

``` clojure
(clj-mcp.repl-tools/list-vars 'malli.core)
;; See all functions with documentation
```

**`clj-mcp.repl-tools/doc-symbol`** - Get function documentation

``` clojure
(clj-mcp.repl-tools/doc-symbol 'malli.core/validate)
```

**`clj-mcp.repl-tools/source-symbol`** - View source code

``` clojure
(clj-mcp.repl-tools/source-symbol 'malli.core/validate)
```

**`clj-mcp.repl-tools/find-symbols`** - Search for symbols

``` clojure
(clj-mcp.repl-tools/find-symbols "validate")
```

**`clojure.repl.deps/add-lib`** - Load libraries dynamically

``` clojure
(require '[clojure.repl.deps :refer [add-lib]])
(add-lib 'metosin/malli {:mvn/version "0.16.0"})
```

### Research Workflow

**Complete workflow for researching a new library:**

``` clojure
;; 1. Load the library
(require '[clojure.repl.deps :refer [add-lib]])
(add-lib 'group/artifact {:mvn/version "X.Y.Z"})

;; 2. Find library namespaces
(clj-mcp.repl-tools/list-ns)
;; Look for namespaces matching the library name

;; 3. Explore main namespace
(clj-mcp.repl-tools/list-vars 'library.core)
;; Read through all available functions

;; 4. Check documentation for key functions
(clj-mcp.repl-tools/doc-symbol 'library.core/main-function)

;; 5. View implementations
(clj-mcp.repl-tools/source-symbol 'library.core/main-function)

;; 6. Test basic usage
(require '[library.core :as lib])
(lib/main-function example-input)
;; => See what happens

;; 7. Test edge cases
(lib/main-function nil)
(lib/main-function [])
(lib/main-function "wrong type")
;; => Document errors and behavior

;; 8. Identify patterns
;; - What's the main use case?
;; - What are common variations?
;; - What integrations are typical?

;; 9. Check for gotchas
;; - Performance characteristics
;; - Configuration requirements
;; - Common mistakes
```

## Best Practices for Skill Creation

**DO:** - Test every single code example with `clojure_eval` - Include
expected output in comments - Document edge cases (nil, empty, errors) -
Show realistic, practical examples - Provide decision guides for
alternatives - Link to official documentation - Keep Quick Start minimal
and focused - Use clear, descriptive workflow names - Add rationale for
best practices - Document common issues you discover

**DON'T:** - Copy examples without testing them - Skip edge case
documentation - Use toy examples that aren't practical - Forget to
include WHEN in description - Create skills longer than 600 lines - Mix
multiple unrelated libraries - Use overly complex examples - Skip the
Common Issues section - Forget to check spelling - Ignore user's likely
mental model

## Common Skill Creation Problems

### Problem: Code Examples Don't Work

**Symptoms:** - Examples throw errors when tested - Output doesn't match
what's documented - Required namespaces not loaded

**Solution:**

``` clojure
;; Always test in clojure_eval FIRST
(require '[library.core :as lib])
;; Did this work? If not, library might not be loaded

;; Load it dynamically
(require '[clojure.repl.deps :refer [add-lib]])
(add-lib 'group/artifact {:mvn/version "X.Y.Z"})

;; Now test your example
(lib/function arg)
;; Verify output matches documentation
```

### Problem: Can't Find Main Namespace

**Symptoms:** - Don't know which namespace to require - Multiple
namespaces, unclear which is main

**Solution:**

``` clojure
;; List all namespaces
(clj-mcp.repl-tools/list-ns)

;; Look for patterns like:
;; - library.core (often main)
;; - library.api (public API)
;; - library.alpha (new API)

;; Check each one
(clj-mcp.repl-tools/list-vars 'library.core)
(clj-mcp.repl-tools/list-vars 'library.api)

;; The one with the most important functions is usually main
```

### Problem: Skill Too Long

**Symptoms:** - Skill exceeds 600 lines - Trying to document
everything - Multiple related but separate concepts

**Solution:** - Ask: Are these actually multiple libraries that need
separate skills? - Focus on 3-5 most common workflows - Move advanced
topics to "Resources" section - Link to external documentation for
comprehensive details - Consider if you're documenting the library or
writing a tutorial

### Problem: Skill Too Short

**Symptoms:** - Skill under 50 lines - Just a Quick Start with no
workflows - Missing practical value

**Solution:** - Add 2-3 more workflows showing common use cases -
Include integration examples (Ring, Pedestal, etc.) - Add Common Issues
section - Document edge cases and error handling - Show before/after
examples

### Problem: Unclear When to Use

**Symptoms:** - Description doesn't mention specific use cases - No
decision guide comparing alternatives - Users won't know when to choose
this library

**Solution:**

``` markdown
## When to Use Each Approach

**Use [This Library] when:**
- Specific technical requirement (e.g., "Need async HTTP")
- Specific use case (e.g., "Building REST APIs")
- Specific context (e.g., "High concurrency required")

**Use [Alternative] when:**
- Different requirement
- Different use case
- Different context

**Don't use either when:**
- Simple case that doesn't need a library
- Performance/simplicity is critical
```

### Problem: Examples Too Simple or Too Complex

**Symptoms:** - Examples are toy code (foo, bar, baz) - Examples are
full applications - Users can't translate to real use

**Solution:** - Use realistic domain examples (users, orders,
requests) - Keep examples focused on ONE concept - Show progression:
simple → realistic → complex - Include just enough context to
understand - Balance between "Hello World" and production code

## Building and Testing Skills

### Build Commands

``` bash
# List all available tasks
bb tasks

# Build a specific prompt
bb build clojure_skill_builder

# Build all prompts
bb build-all

# Build and compress (10x compression)
bb build-compressed clojure_skill_builder --ratio 10

# Clean build artifacts
bb clean-build

# List all skills with metadata
bb list-skills
```

### Testing Skills

``` bash
# Check spelling
bb typos

# Auto-fix typos
bb typos-fix

# Run tests (if applicable)
bb test

# Lint code
bb lint

# Format code
bb fmt
```

### Prompt Compression (Optional)

Reduce token usage by 10-20x using LLMLingua:

``` bash
# One-time setup (downloads ~500MB model)
bb setup-python

# Build and compress in one step
bb build-compressed clojure_skill_builder --ratio 10

# Compress individual skills
bb compress-skill skills/libraries/data_validation/malli.md --ratio 10
```

**Compression ratios:** - 3-5x: Minimal quality impact, most detail
preserved - 5-10x: Balanced approach, good default - 10-20x: Aggressive
saving, semantic meaning intact

## Security Considerations

**Only create Skills from trusted sources.** Skills document code that
agents will execute, so:

-   Audit examples thoroughly
-   Be cautious documenting libraries with security implications
-   Avoid documenting libraries with unexpected network calls
-   Treat Skill creation like code review
-   Never document untrusted or malicious libraries

## Example: Real Skills from Repository

### Minimal Skill Example (kaocha.md)

``` markdown
---
name: kaocha_test_runner
description: |
  Full-featured test runner for Clojure with watch mode, coverage, and plugins.
  Use when running tests, test-driven development (TDD), CI/CD pipelines, coverage
  reporting, or when the user mentions testing, test runner, kaocha, watch mode,
  continuous testing, or test automation.
---

# Kaocha

A comprehensive test runner for Clojure with support for multiple test libraries and powerful plugin system.

## Overview

Kaocha is a modern test runner that works with clojure.test, specs, and other testing frameworks. It provides detailed reporting, watch mode, and extensibility through plugins.

## Core Concepts

**Running Tests**: Execute tests with Kaocha.

```clojure
(require '[kaocha.runner :as runner])

; In terminal:
; bb test                      # Run all tests
; bb test --watch             # Watch mode
; bb test --focus my.test     # Run specific test
```

\[... 60 total lines\]


    ### Standard Skill Example (malli.md)

    ```markdown
    ---
    name: malli_schema_validation
    description: |
      Validate data structures and schemas using Malli. Use when validating API
      requests/responses, defining data contracts, building form validation, schema
      validation, or when the user mentions schemas, validation, malli, data integrity,
      type checking, data contracts, or runtime validation.
    ---

    # Malli Data Validation

    ## Quick Start

    Malli validates data against schemas. Schemas are just Clojure data structures:

    ```clojure
    (require '[malli.core :as m])

    ;; Define a schema
    (def user-schema
      [:map
       [:name string?]
       [:email string?]
       [:age int?]])

    ;; Validate data
    (m/validate user-schema {:name "Alice" :email "alice@example.com" :age 30})
    ;; => true

## Core Concepts

### Schemas as Data

\[Explanation\]

### Validation vs Coercion

\[Explanation\]

## Common Workflows

### Workflow 1: API Request Validation

\[Code example\]

### Workflow 2: Composing Schemas

\[Code example\]

### Workflow 3: Optional and Default Values

\[Code example\]

\[... continues for 450 lines\]


    ## Summary: What Makes an Effective Clojure Skill

    1. **Clear frontmatter** with trigger-rich description (WHAT + WHEN)
    2. **Quick working example** - productive in 2-5 minutes
    3. **Core concepts** - 2-4 fundamental ideas explained clearly
    4. **Practical workflows** - 3-8 realistic, tested examples
    5. **Decision guides** - when to use this vs alternatives
    6. **Best practices** - DOs and DON'Ts with rationale
    7. **Common issues** - problems users WILL encounter with solutions
    8. **Validated code** - every example tested with clojure_eval
    9. **Appropriate size** - 50-600 lines based on complexity
    10. **Single file** - everything in one markdown file with frontmatter

    **The goal:** Transform agents into Clojure specialists who can work
    effectively with libraries, patterns, and best practices without
    needing repeated explanation.

    Now go create excellent Clojure Skills!

    ---
    name: clojure_introduction
    description: |
      Introduction to Clojure fundamentals, immutability, and functional programming concepts. 
      Use when learning Clojure basics, understanding core language features, data structures, 
      functional programming, or when the user asks about Clojure introduction, getting started, 
      language overview, immutability, REPL-driven development, or JVM functional programming.
    ---

    # Clojure Introduction

    Clojure is a functional Lisp for the JVM combining immutable data
    structures, first-class functions, and practical concurrency support.

    ## Core Language Features

    **Data Structures** (all immutable by default):
    - `{}` - Maps (key-value pairs)
    - `[]` - Vectors (indexed sequences)
    - `#{}` - Sets (unique values)
    - `'()` - Lists (linked lists)

    **Functions**: Defined with `defn`. Functions are first-class and
    support variadic arguments, destructuring, and composition.

    **No OOP**: Use functions and data structures instead of
    classes. Polymorphism via `multimethods` and `protocols`, not
    inheritance.

    ## How Immutability Works

    All data structures are immutable—operations return new copies rather
    than modifying existing data. This enables:

    - Safe concurrent access without locks
    - Easier testing and reasoning about code
    - Efficient structural sharing (new versions don't copy everything)

    **Pattern**: Use `assoc`, `conj`, `update`, etc. to create modified
    versions of data.

    ```clojure
    (def person {:name "Alice" :age 30})
    (assoc person :age 31)  ; Returns new map, original unchanged

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

# clojure.test

Clojure's built-in unit testing framework providing assertions, test
organization, fixtures, and reporting.

## Quick Start

clojure.test is part of Clojure core - no additional dependencies
needed.

``` clojure
(require '[clojure.test :refer [deftest is testing]])

;; Define a simple test
(deftest addition-test
  (is (= 4 (+ 2 2)))
  (is (= 7 (+ 3 4))))

;; Run the test
(addition-test)
;; => nil (passes silently)

;; Run all tests in namespace
(require '[clojure.test :refer [run-tests]])
(run-tests)
;; Prints summary and returns {:test 1, :pass 2, :fail 0, :error 0, :type :summary}
```

**Key benefits:** - Built into Clojure - no external dependencies -
Simple, idiomatic assertion syntax - Composable tests and fixtures -
Extensible reporting system - Works with REPL-driven development

## Core Concepts

### Assertions with `is`

The `is` macro is the foundation of clojure.test. It evaluates an
expression and reports success or failure.

``` clojure
(require '[clojure.test :refer [is]])

;; Basic assertion
(is (= 4 (+ 2 2)))
;; => true

;; Failed assertion prints a report
(is (= 5 (+ 2 2)))
;; FAIL in () (NO_SOURCE_FILE:1)
;; expected: (= 5 (+ 2 2))
;;   actual: (not (= 5 4))
;; => false

;; Add descriptive message
(is (= 4 (+ 2 2)) "Two plus two equals four")

;; Any expression that returns truthy/falsy
(is (pos? 42))
(is (string? "hello"))
(is (.startsWith "hello" "hell"))
```

### Exception Testing

Test that code throws expected exceptions:

``` clojure
;; Test that exception is thrown
(is (thrown? ArithmeticException (/ 1 0)))

;; Test exception type AND message
(is (thrown-with-msg? ArithmeticException #"Divide by zero" (/ 1 0)))

;; Common use case: testing validation
(defn parse-age [s]
  (let [n (Integer/parseInt s)]
    (when (neg? n)
      (throw (IllegalArgumentException. "Age must be positive")))
    n))

(is (thrown-with-msg? IllegalArgumentException #"positive" (parse-age "-5")))
```

### Test Organization with `deftest`

Group related assertions into named test functions:

``` clojure
(require '[clojure.test :refer [deftest is]])

(deftest arithmetic-test
  (is (= 4 (+ 2 2)))
  (is (= 7 (+ 3 4)))
  (is (= 1 (- 4 3))))

;; Tests can call other tests (composition)
(deftest addition-test
  (is (= 4 (+ 2 2))))

(deftest subtraction-test
  (is (= 1 (- 4 3))))

(deftest all-arithmetic
  (addition-test)
  (subtraction-test))
```

### Contextual Testing with `testing`

Add descriptive context to groups of assertions:

``` clojure
(require '[clojure.test :refer [deftest is testing]])

(deftest arithmetic-test
  (testing "Addition"
    (is (= 4 (+ 2 2)))
    (is (= 7 (+ 3 4))))
  
  (testing "Subtraction"
    (is (= 1 (- 4 3)))
    (is (= 3 (- 7 4))))
  
  (testing "Edge cases"
    (testing "with zero"
      (is (= 0 (+ 0 0)))
      (is (= 5 (+ 5 0))))
    (testing "with negatives"
      (is (= -1 (+ 1 -2))))))

;; Failed assertions include the testing context:
;; FAIL in (arithmetic-test) (file.clj:10)
;; Edge cases with zero
;; expected: (= 1 (+ 0 0))
;;   actual: (not (= 1 0))
```

## Common Workflows

### Workflow 1: Basic Test File Structure

Typical test namespace organization:

``` clojure
(ns myapp.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [myapp.core :as core]))

(deftest basic-functionality-test
  (testing "Core function works correctly"
    (is (= expected-result (core/my-function input)))))

(deftest edge-cases-test
  (testing "Handles nil input"
    (is (nil? (core/my-function nil))))
  
  (testing "Handles empty collection"
    (is (= [] (core/my-function [])))))

(deftest error-conditions-test
  (testing "Throws on invalid input"
    (is (thrown? IllegalArgumentException 
                 (core/my-function "invalid")))))
```

### Workflow 2: Multiple Assertions with `are`

Test the same logic with multiple inputs using the `are` macro:

``` clojure
(require '[clojure.test :refer [deftest is are]])

;; Without are - repetitive
(deftest addition-verbose
  (is (= 2 (+ 1 1)))
  (is (= 4 (+ 2 2)))
  (is (= 6 (+ 3 3)))
  (is (= 8 (+ 4 4))))

;; With are - concise template
(deftest addition-concise
  (are [x y] (= x y)
    2 (+ 1 1)
    4 (+ 2 2)
    6 (+ 3 3)
    8 (+ 4 4)))

;; More complex example
(deftest string-operations
  (are [expected input] (= expected (clojure.string/upper-case input))
    "HELLO" "hello"
    "WORLD" "world"
    "FOO"   "foo"
    "BAR"   "bar"))

;; Multiple arguments
(deftest math-operations
  (are [result op x y] (= result (op x y))
    4  +  2 2
    0  -  2 2
    4  *  2 2
    1  /  2 2))
```

**Note**: `are` breaks some reporting features like line numbers, so use
it for straightforward cases.

### Workflow 3: Setup and Teardown with Fixtures

Fixtures run setup/teardown code around tests:

``` clojure
(require '[clojure.test :refer [deftest is use-fixtures]])

;; Define fixture function
(defn database-fixture [f]
  ;; Setup: create test database
  (println "Setting up test database")
  (def test-db (create-test-db))
  
  ;; Run the test(s)
  (f)
  
  ;; Teardown: clean up
  (println "Tearing down test database")
  (cleanup-test-db test-db))

;; Apply fixture to each test
(use-fixtures :each database-fixture)

;; Apply fixture once for entire namespace
(use-fixtures :once database-fixture)

;; Compose multiple fixtures
(defn logging-fixture [f]
  (println "Test starting")
  (f)
  (println "Test finished"))

(use-fixtures :each database-fixture logging-fixture)

(deftest query-test
  ;; database-fixture runs around this test
  (is (= expected-result (query test-db "SELECT ..."))))
```

**Fixture types:** - `:each` - Runs around every `deftest`
individually - `:once` - Runs once around all tests in namespace

### Workflow 4: Running Tests

Multiple ways to run tests:

``` clojure
(require '[clojure.test :refer [run-tests run-all-tests test-var]])

;; Run all tests in current namespace
(run-tests)
;; => {:test 5, :pass 12, :fail 0, :error 0, :type :summary}

;; Run tests in specific namespaces
(run-tests 'myapp.core-test 'myapp.util-test)

;; Run all tests in all loaded namespaces
(run-all-tests)

;; Run tests matching a regex
(run-all-tests #"myapp\..*-test")

;; Run a single test function
(require '[clojure.test :refer [run-test]])
(run-test my-specific-test)

;; Run a single test var
(test-var #'my-specific-test)

;; From the command line
;; clojure -M:test -m clojure.test.runner
```

### Workflow 5: Testing with Dynamic Bindings

Use dynamic vars for test state:

``` clojure
(require '[clojure.test :refer [deftest is use-fixtures]])

;; Define dynamic var for test state
(def ^:dynamic *test-config* nil)

;; Fixture to bind test config
(defn with-test-config [f]
  (binding [*test-config* {:db-url "jdbc:test://localhost"
                           :timeout 1000}]
    (f)))

(use-fixtures :each with-test-config)

(deftest config-dependent-test
  (is (= "jdbc:test://localhost" (:db-url *test-config*)))
  (is (= 1000 (:timeout *test-config*))))

;; Common pattern for database testing
(def ^:dynamic *db-conn* nil)

(defn with-db-connection [f]
  (binding [*db-conn* (connect-to-test-db)]
    (try
      (f)
      (finally
        (disconnect *db-conn*)))))
```

### Workflow 6: Inline Tests with `with-test`

Attach tests directly to function definitions:

``` clojure
(require '[clojure.test :refer [with-test is]])

(with-test
  (defn add [x y]
    (+ x y))
  
  (is (= 4 (add 2 2)))
  (is (= 7 (add 3 4)))
  (is (= 0 (add 0 0))))

;; Test is stored in metadata
(:test (meta #'add))
;; => #function[...]

;; Run the inline test
((:test (meta #'add)))

;; Or use test-var
(require '[clojure.test :refer [test-var]])
(test-var #'add)
```

**Note**: `with-test` doesn't work with `defmacro` (use `deftest`
instead).

### Workflow 7: Custom Test Hooks

Control test execution order with `test-ns-hook`:

``` clojure
(require '[clojure.test :refer [deftest is]])

(deftest setup-test
  (is (= :setup :done)))

(deftest test-a
  (is (= 1 1)))

(deftest test-b
  (is (= 2 2)))

(deftest teardown-test
  (is (= :cleanup :done)))

;; Define custom execution order
(defn test-ns-hook []
  (setup-test)
  (test-a)
  (test-b)
  (teardown-test))

;; When test-ns-hook exists, run-tests calls it instead of
;; running all tests. This gives you full control over execution.
```

**Note**: `test-ns-hook` and fixtures are mutually incompatible. Choose
one approach.

## When to Use Each Approach

### Use `is` when:

-   Writing simple assertions
-   Testing individual expressions
-   You need immediate REPL feedback

### Use `deftest` when:

-   Organizing related assertions
-   Creating reusable test suites
-   Writing tests in separate test namespaces
-   Following standard Clojure testing conventions

### Use `testing` when:

-   Adding context to groups of assertions
-   Nested test organization is helpful
-   You want better error reporting

### Use `are` when:

-   Testing the same logic with multiple inputs
-   You have many similar assertions
-   Code clarity is more important than precise line numbers

### Use fixtures when:

-   Setup/teardown is needed for tests
-   Managing shared test state
-   Database connections or external resources
-   Isolating test side effects

### Use `with-test` when:

-   Writing quick inline tests
-   Tests are tightly coupled to implementation
-   Prototyping or experimenting

### Use `test-ns-hook` when:

-   You need precise test execution order
-   Tests have dependencies on each other
-   Running composed test suites
-   Don't use fixtures

## Best Practices

**DO:** - Write focused tests that test one thing - Use descriptive test
names: `test-handles-empty-input-correctly` - Add `testing` contexts for
clear failure messages - Use `are` for parameterized tests - Keep tests
independent - no shared mutable state - Test edge cases: nil, empty
collections, boundary values - Use fixtures for setup/teardown, not
manual code - Make tests readable - clarity over cleverness - Test
behavior, not implementation details

**DON'T:** - Mix `test-ns-hook` and fixtures (they're incompatible) -
Write tests that depend on execution order (unless using
`test-ns-hook`) - Share mutable state between tests - Test private
implementation details extensively - Write overly complex test logic
(tests should be simple) - Forget to test exception cases - Skip testing
edge cases - Use `are` when you need precise line numbers for failures

## Common Issues

### Issue: Tests Pass Individually but Fail Together

**Problem:** Tests work when run alone but fail when run with other
tests.

``` clojure
(def shared-state (atom []))

(deftest test-a
  (reset! shared-state [1 2 3])
  (is (= 3 (count @shared-state))))

(deftest test-b
  ;; Assumes shared-state is empty - fails if test-a runs first
  (is (empty? @shared-state)))
```

**Solution:** Use fixtures to reset shared state or avoid shared mutable
state:

``` clojure
(def shared-state (atom []))

(defn reset-state-fixture [f]
  (reset! shared-state [])
  (f))

(use-fixtures :each reset-state-fixture)

(deftest test-a
  (swap! shared-state conj 1 2 3)
  (is (= 3 (count @shared-state))))

(deftest test-b
  ;; Now guaranteed to start with empty state
  (is (empty? @shared-state)))
```

### Issue: Fixtures Not Running

**Problem:** Defined fixtures but they're not executing.

``` clojure
(defn my-fixture [f]
  (println "Setting up")
  (f))

(use-fixtures :each my-fixture)

(defn test-ns-hook []  ;; This prevents fixtures from running!
  (my-test))

(deftest my-test
  (is (= 1 1)))
```

**Solution:** Remove `test-ns-hook` or manually call fixtures:

``` clojure
;; Option 1: Remove test-ns-hook
;; (defn test-ns-hook [] ...) ;; Delete this

;; Option 2: Manually run fixtures in test-ns-hook
(defn test-ns-hook []
  (my-fixture #(my-test)))
```

### Issue: `is` Returns False but Test Appears to Pass

**Problem:** `is` returns false at REPL but test function seems to
succeed.

``` clojure
(deftest my-test
  (is (= 5 (+ 2 2))))  ;; Returns false, but...

(my-test)  ;; Prints failure report but returns nil
;; => nil
```

**Solution:** This is expected behavior. `is` returns the test result,
but `deftest` always returns nil. Check the printed output or use
`run-tests` to see the summary:

``` clojure
(run-tests)
;; Shows: 1 failures, 0 errors
;; => {:test 1, :pass 0, :fail 1, :error 0, :type :summary}
```

### Issue: Can't See Test Output

**Problem:** Test output not appearing in expected location.

``` clojure
(deftest my-test
  (println "Debug info")  ;; Where does this go?
  (is (= 4 (+ 2 2))))
```

**Solution:** Test output goes to `*test-out*` (default: `*out*`). Wrap
in `with-test-out`:

``` clojure
(require '[clojure.test :refer [deftest is with-test-out]])

(deftest my-test
  (with-test-out
    (println "This goes to *test-out*"))
  (is (= 4 (+ 2 2))))

;; Or redirect *test-out* to a file
(require '[clojure.java.io :as io])

(binding [clojure.test/*test-out* (io/writer "test-output.txt")]
  (run-tests))
```

### Issue: Exception Stack Traces Too Long

**Problem:** Stack traces make test output unreadable.

``` clojure
(deftest my-test
  (is (= 1 (throw (Exception. "Oops")))))
;; Prints 50+ lines of stack trace
```

**Solution:** Limit stack trace depth with `*stack-trace-depth*`:

``` clojure
(require '[clojure.test :as t])

(binding [t/*stack-trace-depth* 5]
  (run-tests))
;; Only shows first 5 stack frames
```

### Issue: `are` Not Showing Which Row Failed

**Problem:** When `are` assertions fail, line numbers aren't helpful.

``` clojure
(deftest math-test
  (are [x y] (= x y)
    2 (+ 1 1)
    4 (+ 2 2)
    7 (+ 3 3)  ;; This fails but report just says "line 2"
    8 (+ 4 4)))
```

**Solution:** Use explicit `is` assertions when debugging, or add
descriptive values:

``` clojure
;; Option 1: Expand to explicit is forms for debugging
(deftest math-test
  (is (= 2 (+ 1 1)))
  (is (= 4 (+ 2 2)))
  (is (= 7 (+ 3 3)))  ;; Now shows correct line
  (is (= 8 (+ 4 4))))

;; Option 2: Add descriptive labels to are
(deftest math-test
  (are [label x y] (is (= x y) label)
    "1+1" 2 (+ 1 1)
    "2+2" 4 (+ 2 2)
    "3+3" 7 (+ 3 3)
    "4+4" 8 (+ 4 4)))
```

## Advanced Topics

### Custom Assertion Expressions

Extend `is` with custom assertion types:

``` clojure
(require '[clojure.test :refer [deftest is assert-expr]])

;; Define custom assertion
(defmethod assert-expr 'approx= [msg form]
  (let [[_ expected actual tolerance] form]
    `(let [expected# ~expected
           actual# ~actual
           tolerance# ~tolerance
           diff# (Math/abs (- expected# actual#))]
       (if (<= diff# tolerance#)
         (do-report {:type :pass
                     :message ~msg
                     :expected '~form
                     :actual actual#})
         (do-report {:type :fail
                     :message ~msg
                     :expected '~form
                     :actual (list '~'not (list 'approx= expected# actual# tolerance#))})))))

;; Use custom assertion
(deftest float-math-test
  (is (approx= 0.333 (/ 1.0 3.0) 0.001)))
```

### Custom Reporters

Customize test output format:

``` clojure
(require '[clojure.test :refer [report]])

;; Override default reporter
(defmethod report :fail [m]
  (println "FAILED:" (:message m))
  (println "Expected:" (:expected m))
  (println "Got:" (:actual m)))

;; Or use built-in alternative formats
(require '[clojure.test.junit :refer [with-junit-output]])
(require '[clojure.test.tap :refer [with-tap-output]])

;; JUnit XML output
(with-junit-output
  (run-tests))

;; TAP (Test Anything Protocol) output
(with-tap-output
  (run-tests))
```

## Integration with Test Runners

clojure.test works with all major Clojure test runners:

-   **Kaocha** - Modern, feature-rich test runner (recommended)
-   **Lazytest** - Fast, watch-mode runner
-   **Cognitect test-runner** - Minimal CLI runner
-   **Leiningen** - `lein test`
-   **tools.build** - Build tool integration

All runners execute `clojure.test` tests - they add features like: -
Watch mode - Parallel execution - Filtering - Better reporting -
Coverage analysis

## Resources

-   [Official clojure.test
    API](https://clojure.github.io/clojure/clojure.test-api.html)
-   [Clojure Testing Guide](https://clojure.org/guides/testing)
-   [clojure.test
    Source](https://github.com/clojure/clojure/blob/master/src/clj/clojure/test.clj)

## Summary

clojure.test is Clojure's built-in testing framework providing:

1.  **Simple assertions** - `is` macro for any predicate
2.  **Test organization** - `deftest` and `testing` for structure
3.  **Fixtures** - Setup/teardown with `use-fixtures`
4.  **Composability** - Tests can call other tests
5.  **Extensibility** - Custom assertions and reporters
6.  **REPL integration** - Test interactively as you develop

**Core workflow:** - Write tests with `deftest` and `is` - Add context
with `testing` - Use `are` for parameterized tests - Add fixtures for
setup/teardown - Run with `run-tests` or test runners

Master clojure.test and you have a solid foundation for testing any
Clojure code.

# Babashka

A fast-starting Clojure interpreter for scripting built with GraalVM
native image. Babashka (bb) provides instant startup time, making
Clojure practical for shell scripts, build tasks, and CLI tools.

## Quick Start

Babashka scripts start instantly and can use most Clojure features:

``` clojure
#!/usr/bin/env bb

;; Simple script - save as script.clj
(println "Hello from Babashka!")
(println "Arguments:" *command-line-args*)

;; Run it
;; bb script.clj arg1 arg2
;; => Hello from Babashka!
;; => Arguments: (arg1 arg2)

;; Or use shebang and make executable
;; chmod +x script.clj
;; ./script.clj arg1 arg2

;; One-liner evaluation
;; bb -e '(+ 1 2 3)'
;; => 6

;; Process piped input
;; echo '{"name": "Alice"}' | bb -e '(-> *input* slurp (json/parse-string true) :name)'
;; => Alice
```

**Key benefits:** - **Instant startup** - \~10ms vs 1-2s for JVM
Clojure - **Native executable** - No JVM installation required - **Task
runner** - Built-in task system via bb.edn - **Rich standard library** -
Including shell, http, file system, JSON, YAML - **Script-friendly** -
Process args, stdin/stdout, exit codes - **Pod system** - Extend with
native binaries

## Core Concepts

### Fast Startup via GraalVM Native Image

Babashka is compiled to a native executable using GraalVM, eliminating
JVM startup time:

``` bash
# Compare startup times
time bb -e '(println "Ready")'
# ~0.01s

time clojure -M -e '(println "Ready")'
# ~1-2s
```

**Trade-offs:** - Instant startup vs slower runtime performance - Fixed
set of classes vs dynamic classloading - Lower memory usage vs smaller
heap - Perfect for scripts, not for long-running servers

### Built-in Libraries

Babashka includes many useful libraries by default:

``` clojure
;; File system (babashka.fs)
(require '[babashka.fs :as fs])
(fs/list-dir ".")
(fs/glob "." "**/*.clj")
(fs/create-dirs "target/build")

;; Shell (babashka.process)
(require '[babashka.process :as p])
(-> (p/shell {:out :string} "git status --short")
    :out)

;; HTTP client (babashka.http-client)
(require '[babashka.http-client :as http])
(http/get "https://api.github.com/users/babashka")

;; JSON (cheshire)
(require '[cheshire.core :as json])
(json/parse-string "{\"name\": \"Alice\"}" true)

;; YAML (clj-commons/clj-yaml)
(require '[clj-yaml.core :as yaml])
(yaml/parse-string "name: Alice\nage: 30")

;; Data manipulation (medley)
(require '[medley.core :as m])
(m/map-vals inc {:a 1 :b 2})

;; And many more...
```

### Task System

Define reusable tasks in `bb.edn`:

``` clojure
;; bb.edn
{:tasks
 {:requires ([babashka.fs :as fs])
  
  ;; Simple task
  clean
  {:doc "Remove build artifacts"
   :task (fs/delete-tree "target")}
  
  ;; Task with dependencies
  test
  {:doc "Run tests"
   :depends [clean]
   :task (shell "clojure -M:test")}
  
  ;; Task with parameters
  deploy
  {:doc "Deploy to environment"
   :task (let [env (or (first *command-line-args*) "dev")]
           (println "Deploying to" env)
           (shell (str "deploy-" env ".sh")))}}}

;; Run tasks
;; bb tasks                  # List available tasks
;; bb clean                  # Run clean task
;; bb test                   # Run test (which runs clean first)
;; bb deploy production      # Run with args
```

### Process Execution

Babashka provides excellent shell integration:

``` clojure
(require '[babashka.process :as p])

;; Simple command
(p/shell "ls -la")
;; Output goes to stdout

;; Capture output
(-> (p/shell {:out :string} "git status --short")
    :out
    println)

;; Pipeline
(p/pipeline
  (p/process ["cat" "file.txt"])
  (p/process ["grep" "ERROR"])
  (p/process ["wc" "-l"]))

;; Handle errors
(let [result (p/shell {:continue true} "false")]
  (when-not (zero? (:exit result))
    (println "Command failed!")))
```

## Common Workflows

### Workflow 1: Writing Executable Scripts

``` clojure
#!/usr/bin/env bb

;; Script: backup.clj
;; Usage: ./backup.clj source-dir backup-dir

(require '[babashka.fs :as fs]
         '[babashka.process :as p])

(defn backup [source dest]
  (println "Backing up" source "to" dest)
  
  ;; Create backup directory
  (fs/create-dirs dest)
  
  ;; Copy files
  (doseq [file (fs/glob source "**/*")]
    (when (fs/regular-file? file)
      (let [relative (fs/relativize source file)
            target (fs/path dest relative)]
        (fs/create-dirs (fs/parent target))
        (fs/copy file target {:replace-existing true}))))
  
  (println "Backup complete!"))

;; Parse arguments
(when (< (count *command-line-args*) 2)
  (println "Usage: backup.clj <source> <dest>")
  (System/exit 1))

(let [[source dest] *command-line-args*]
  (backup source dest))

;; Make it executable:
;; chmod +x backup.clj
;;
;; Run it:
;; ./backup.clj ~/docs ~/backups/docs
```

### Workflow 2: Creating a Task Runner

``` clojure
;; bb.edn - Project task definitions
{:deps {medley/medley {:mvn/version "1.3.0"}
        io.github.paintparty/bling {:mvn/version "0.6.0"}}
 
 :paths ["src" "test"]
 
 :tasks
 {:requires ([babashka.fs :as fs]
             [babashka.process :as p]
             [bling.core :as bling])
  
  ;; Clean build artifacts
  clean
  {:doc "Remove target directory"
   :task (do
           (bling/callout {:type :info} 
                          (bling/bling [:bold "Cleaning..."]))
           (fs/delete-tree "target")
           (bling/callout {:type :success} "Clean complete"))}
  
  ;; Run tests
  test
  {:doc "Run tests with Kaocha"
   :task (do
           (bling/callout {:type :info} 
                          (bling/bling [:bold "Running tests..."]))
           (p/shell "clojure -M:test"))}
  
  ;; Lint code
  lint
  {:doc "Lint with clj-kondo"
   :task (p/shell "clojure -M:lint -m clj-kondo.main --lint src test")}
  
  ;; Format code
  fmt
  {:doc "Format code with cljstyle"
   :task (p/shell "clojure -M:format -m cljstyle.main fix src test")}
  
  ;; Check formatting
  fmt-check
  {:doc "Check code formatting"
   :task (p/shell "clojure -M:format -m cljstyle.main check src test")}
  
  ;; Run full CI pipeline
  ci
  {:doc "Run CI pipeline: clean, lint, test"
   :task (do
           (run 'clean)
           (run 'fmt-check)
           (run 'lint)
           (run 'test)
           (bling/callout {:type :success} 
                          (bling/bling [:bold "CI passed!"])))}
  
  ;; Start REPL
  repl
  {:doc "Start nREPL server on port 7889"
   :task (p/shell "clojure -M:nrepl")}
  
  ;; Build uberjar
  build
  {:doc "Build uberjar"
   :depends [clean test]
   :task (do
           (bling/callout {:type :info} "Building uberjar...")
           (p/shell "clojure -T:build uber")
           (bling/callout {:type :success} "Build complete!"))}}}

;; Usage:
;; bb tasks              # List all tasks
;; bb clean              # Run clean task
;; bb ci                 # Run full CI pipeline
;; bb build              # Build (runs clean and test first)
```

### Workflow 3: HTTP API Client Script

``` clojure
#!/usr/bin/env bb

;; Script: github-info.clj
;; Usage: ./github-info.clj username

(require '[babashka.http-client :as http]
         '[cheshire.core :as json])

(defn get-user-info [username]
  (let [url (str "https://api.github.com/users/" username)
        response (http/get url {:headers {"User-Agent" "Babashka"}})]
    (when (= 200 (:status response))
      (json/parse-string (:body response) true))))

(defn get-repos [username]
  (let [url (str "https://api.github.com/users/" username "/repos")
        response (http/get url {:headers {"User-Agent" "Babashka"}})]
    (when (= 200 (:status response))
      (json/parse-string (:body response) true))))

(defn display-info [username]
  (if-let [user (get-user-info username)]
    (do
      (println "Name:" (:name user))
      (println "Bio:" (:bio user))
      (println "Public Repos:" (:public_repos user))
      (println "Followers:" (:followers user))
      
      (println "\nTop 5 Repos:")
      (doseq [repo (->> (get-repos username)
                        (sort-by :stargazers_count >)
                        (take 5))]
        (println (format "  ⭐ %d - %s" 
                         (:stargazers_count repo)
                         (:name repo)))))
    (println "User not found")))

;; Main
(when (empty? *command-line-args*)
  (println "Usage: github-info.clj <username>")
  (System/exit 1))

(display-info (first *command-line-args*))
```

### Workflow 4: File Processing Pipeline

``` clojure
#!/usr/bin/env bb

;; Script: process-logs.clj
;; Process log files and generate report

(require '[babashka.fs :as fs]
         '[clojure.string :as str])

(defn parse-log-line [line]
  (when-let [[_ timestamp level message] 
             (re-matches #"\[(.*?)\] (\w+): (.*)" line)]
    {:timestamp timestamp
     :level (keyword (str/lower-case level))
     :message message}))

(defn analyze-logs [log-dir]
  (let [log-files (fs/glob log-dir "*.log")
        entries (->> log-files
                     (mapcat (comp str/split-lines slurp))
                     (keep parse-log-line))]
    
    {:total (count entries)
     :by-level (frequencies (map :level entries))
     :errors (->> entries
                  (filter #(= :error (:level %)))
                  (map :message)
                  (take 10))}))

(defn report [stats]
  (println "Log Analysis Report")
  (println "===================")
  (println "Total entries:" (:total stats))
  (println "\nBy level:")
  (doseq [[level count] (sort-by val > (:by-level stats))]
    (println (format "  %s: %d" (name level) count)))
  (println "\nRecent errors:")
  (doseq [error (:errors stats)]
    (println "  -" error)))

;; Main
(when (empty? *command-line-args*)
  (println "Usage: process-logs.clj <log-directory>")
  (System/exit 1))

(-> (first *command-line-args*)
    analyze-logs
    report)
```

### Workflow 5: Data Transformation Script

``` clojure
#!/usr/bin/env bb

;; Script: transform-data.clj
;; Read JSON/YAML, transform, output JSON/YAML

(require '[cheshire.core :as json]
         '[clj-yaml.core :as yaml]
         '[clojure.string :as str]
         '[babashka.fs :as fs])

(defn read-file [path]
  (let [content (slurp path)
        ext (fs/extension path)]
    (case ext
      "json" (json/parse-string content true)
      "yaml" (yaml/parse-string content)
      "yml" (yaml/parse-string content)
      (throw (ex-info "Unsupported format" {:ext ext})))))

(defn write-file [path data]
  (let [ext (fs/extension path)]
    (case ext
      "json" (spit path (json/generate-string data {:pretty true}))
      "yaml" (spit path (yaml/generate-string data))
      "yml" (spit path (yaml/generate-string data))
      (throw (ex-info "Unsupported format" {:ext ext})))))

(defn transform [data]
  ;; Example: uppercase all string values
  (clojure.walk/postwalk
    (fn [x]
      (if (string? x)
        (str/upper-case x)
        x))
    data))

;; Main
(when (< (count *command-line-args*) 2)
  (println "Usage: transform-data.clj <input> <output>")
  (System/exit 1))

(let [[input output] *command-line-args*]
  (-> input
      read-file
      transform
      (write-file output))
  (println "Transformed" input "→" output))
```

### Workflow 6: Interactive Task with User Input

``` clojure
#!/usr/bin/env bb

;; Script: interactive-setup.clj
;; Interactive project setup

(defn prompt [message]
  (print (str message " "))
  (flush)
  (read-line))

(defn confirm [message]
  (= "y" (prompt (str message " (y/n)?"))))

(defn setup-project []
  (println "Project Setup")
  (println "=============")
  
  (let [name (prompt "Project name:")
        desc (prompt "Description:")
        author (prompt "Author:")
        use-test? (confirm "Include test setup?")
        use-ci? (confirm "Include CI config?")]
    
    {:name name
     :description desc
     :author author
     :features {:test use-test?
                :ci use-ci?}}))

(defn create-files [config]
  (require '[babashka.fs :as fs])
  
  ;; Create project structure
  (fs/create-dirs (str (:name config) "/src"))
  
  (when (get-in config [:features :test])
    (fs/create-dirs (str (:name config) "/test")))
  
  (when (get-in config [:features :ci])
    (spit (str (:name config) "/.github/workflows/ci.yml")
          "name: CI\n..."))
  
  (println "\n✓ Project created:" (:name config)))

;; Main
(let [config (setup-project)]
  (when (confirm "\nCreate project files?")
    (create-files config)
    (println "Done!")))
```

### Workflow 7: Conditional Task Execution

``` clojure
;; bb.edn with conditional tasks
{:tasks
 {:requires ([babashka.fs :as fs])
  
  ;; Check if files changed
  changed?
  {:task (let [src-files (fs/glob "src" "**/*.clj")
               target (fs/file "target/build.timestamp")
               last-build (when (fs/exists? target)
                            (fs/file-time->millis target))
               latest-src (when (seq src-files)
                            (apply max (map fs/file-time->millis src-files)))]
           (or (nil? last-build)
               (> latest-src last-build)))}
  
  ;; Build only if changed
  build
  {:doc "Build if source files changed"
   :task (if (-> (shell {:out :string :continue true} "bb changed?")
                 :out
                 str/trim
                 boolean)
           (do
             (println "Source changed, rebuilding...")
             (shell "clojure -T:build compile")
             (fs/create-dirs "target")
             (spit "target/build.timestamp" (System/currentTimeMillis)))
           (println "No changes, skipping build"))}}}
```

## When to Use Each Approach

**Use Babashka when:** - Writing shell scripts that need Clojure -
Building task runners and build tools - Creating CLI tools with fast
startup - Processing files and data transformations - Quick automation
scripts - Replacing bash/python scripts with Clojure - CI/CD scripts -
Git hooks

**Use JVM Clojure when:** - Building long-running applications - Need
dynamic classloading - Require libraries not compatible with bb -
Performance-critical computations - Large heap requirements - Web
servers (use http-kit/Ring on JVM)

**Can use either:** - Data processing pipelines (bb faster startup, JVM
faster runtime) - API clients (bb sufficient for most cases) - Testing
utilities (bb for fast feedback, JVM for comprehensive)

## Best Practices

**DO:** - Use shebang `#!/usr/bin/env bb` for executable scripts -
Handle *command-line-args* explicitly - Exit with proper codes (0
success, non-zero error) - Use `babashka.process/shell` for external
commands - Leverage built-in libraries (fs, http-client, process) -
Define reusable tasks in bb.edn - Use `:doc` strings in task
definitions - Handle errors with proper messages - Make scripts portable
(avoid platform-specific paths)

**DON'T:** - Try to load incompatible libraries - Expect same
performance as JVM for CPU-intensive work - Use dynamic classloading
(not supported) - Forget to handle missing arguments - Ignore exit codes
from shell commands - Hard-code paths (use babashka.fs) - Write overly
complex scripts (consider JVM app instead)

## Common Issues

### Issue: "Class Not Found"

**Problem:** Trying to use a library not included in Babashka

``` clojure
(require '[some.library :as lib])
;; => Could not find namespace: some.library
```

**Solution:** Check if library is compatible or use pods

``` clojure
;; Check built-in libraries
;; bb -e "(keys (ns-publics 'clojure.core))"

;; Add compatible dependency to bb.edn
{:deps {medley/medley {:mvn/version "1.3.0"}}}

;; Or use a pod for native libraries
;; See: https://github.com/babashka/pods
```

### Issue: "Command Not Found in Shell"

**Problem:** Shell command fails to find executable

``` clojure
(require '[babashka.process :as p])
(p/shell "my-tool")
;; => Error: Cannot run program "my-tool"
```

**Solution:** Use full path or check PATH

``` clojure
;; Use full path
(p/shell "/usr/local/bin/my-tool")

;; Or check PATH
(p/shell {:extra-env {"PATH" (str (System/getenv "PATH") ":/custom/path")}}
         "my-tool")
```

### Issue: "Task Not Found"

**Problem:** bb.edn task not recognized

``` clojure
;; bb.edn
{:tasks {test {:task (println "Testing")}}}

;; bb test
;; => Could not find task: test
```

**Solution:** Check bb.edn location and syntax

``` bash
# bb.edn must be in current directory or parent
# Check it's valid EDN
bb -e '(clojure.edn/read-string (slurp "bb.edn"))'

# List available tasks
bb tasks
```

### Issue: "Slow Performance"

**Problem:** Script is slower than expected

``` clojure
;; Intensive computation
(reduce + (range 10000000))
;; Takes longer than JVM Clojure
```

**Solution:** Babashka optimizes for startup, not runtime

``` bash
# For CPU-intensive work, use JVM Clojure
clojure -M -e '(time (reduce + (range 10000000)))'

# Or keep bb for orchestration, delegate heavy work:
bb -e '(babashka.process/shell "clojure -M -m my.cpu-intensive-app")'
```

## Advanced Topics

### Pods System

Pods extend Babashka with native binaries:

``` clojure
;; Load pod
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/postgresql "0.1.0")

;; Use pod
(require '[pod.babashka.postgresql :as pg])
(pg/execute! db-spec ["SELECT * FROM users"])
```

Available pods: https://github.com/babashka/pods

### Preloads

Load code before script execution:

``` clojure
;; bb.edn
{:paths ["."]
 :tasks {:init (load-file "helpers.clj")}}

;; helpers.clj
(defn my-helper [x] (str x "!"))

;; Now available in all tasks
{:tasks
 {greet {:task (println (my-helper "Hello"))}}}
```

### Native Image Compilation

Compile your own bb scripts to native binaries:

``` bash
# Using GraalVM native-image
# See: https://www.graalvm.org/latest/reference-manual/native-image/
```

### Socket REPL

Start a REPL server for debugging:

``` bash
bb socket-repl 1666
# Connect with: rlwrap nc localhost 1666

bb nrepl-server 1667
# Connect from editors (CIDER, Calva, etc.)
```

## Resources

-   Official Documentation: https://book.babashka.org
-   GitHub: https://github.com/babashka/babashka
-   Built-in libraries: https://book.babashka.org/#libraries
-   Pods registry: https://github.com/babashka/pods
-   Examples: https://github.com/babashka/babashka/tree/master/examples
-   Task runner guide: https://book.babashka.org/#tasks
-   API docs: https://babashka.org/doc/api

## Related Tools

-   **Babashka.fs** - File system library (built-in)
-   **Babashka.process** - Shell process library (built-in)
-   **Babashka.cli** - Command-line parsing
-   **Babashka.http-client** - HTTP client (built-in)
-   **Scittle** - Babashka for the browser
-   **Nbb** - Node.js-based Clojure scripting

## Summary

Babashka makes Clojure practical for scripting:

-   **Instant startup** - \~10ms for shell scripts
-   **Native binary** - No JVM required
-   **Task runner** - Built-in task system
-   **Rich stdlib** - fs, http, process, json, yaml, and more
-   **Script-friendly** - Args, stdin/stdout, exit codes
-   **Pod system** - Extend with native libraries

Use Babashka for shell scripts, build automation, CLI tools, and
anywhere fast startup matters more than long-running performance.
