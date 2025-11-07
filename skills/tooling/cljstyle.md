---
name: cljstyle_code_formatter
description: Code formatter for consistent Clojure style.
---

# cljstyle

A code formatter for Clojure that enforces consistent style.

## Overview

cljstyle automatically formats Clojure code to follow the community style guide, ensuring consistency across your codebase.

## Core Concepts

**Formatting Code**: Apply consistent style.

```clojure
; In terminal:
; cljstyle check src/               ; Check formatting
; cljstyle fix src/                 ; Fix formatting in place
```

**Configuration**: Customize formatting rules.

```clojure
; In .cljstyle:
{:indentation? true
 :line-length 100
 :remove-trailing-whitespace? true
 :require-blank-line-before-namespace-docstring? false}
```

## Key Features

- Consistent indentation
- Line length enforcement
- Whitespace cleanup
- Require formatting
- Docstring formatting
- Customizable rules
- IDE integration

## When to Use

- Development (maintain consistent style)
- Pre-commit hooks
- CI/CD pipelines
- Team code standards

## When NOT to Use

- High-performance parsing

## Common Patterns

```clojure
; In bb.edn:
{:tasks
 {:fmt
  {:doc "Format code"
   :task (shell "cljstyle fix src test")}
  
  :fmt-check
  {:doc "Check formatting"
   :task (shell "cljstyle check src test")}}}

; Example formatting:
; Before:
(defn add[a b]
  (+  a    b))

; After:
(defn add [a b]
  (+ a b))
```

## Related Libraries

- clj-kondo/clj-kondo - Linting

## Resources

- Official Documentation: https://github.com/greglook/cljstyle
- API Documentation: https://cljdoc.org/d/mvxcvi/cljstyle

## Notes

This project uses cljstyle for code formatting. Run with `bb fmt` or `bb fmt-check`.
