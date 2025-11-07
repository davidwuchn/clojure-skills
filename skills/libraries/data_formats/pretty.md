---
name: pretty_printing_library
description: Pretty printing library for Clojure data structures.
---

# Pretty

A Clojure library for pretty printing and formatting data structures for display.

## Overview

org.clj-commons/pretty provides utilities for printing Clojure data structures in a readable, formatted manner. It handles complex nested structures and respects terminal width.

## Core Concepts

**Pretty Printing**: Format data structures for readable output.

```clojure
(require '[clojure.pprint :refer [pprint]])

(def data {:users [{:id 1 :name "Alice" :email "alice@example.com"}
                   {:id 2 :name "Bob" :email "bob@example.com"}]})

(pprint data)
; Output:
; {:users
;  [{:id 1,
;    :name "Alice",
;    :email "alice@example.com"}
;   {:id 2, :name "Bob", :email "bob@example.com"}]}
```

**Custom Formatters**: Define how different types are printed.

```clojure
(require '[clojure.pprint :as pprint])

; Register a custom print function
(defmethod pprint/simple-dispatch java.util.Date [date]
  (.print *out* (str "Date<" date ">")))
```

## Key Features

- Automatic indentation
- Column width detection
- Nested structure formatting
- Custom formatters
- Table printing
- Error message formatting

## When to Use

- Debugging (printing data structures at REPL)
- Logging complex data structures
- Displaying error messages
- Development tools and diagnostics

## When NOT to Use

- For serialization (use JSON or EDN)
- High-performance output
- Programmatic processing (data is for humans)

## Common Patterns

```clojure
(require '[clojure.pprint :refer [pprint print-table]])

; Debug printing
(def results (fetch-data))
(pprint results)

; Table printing
(print-table [{:id 1 :name "Alice" :active true}
              {:id 2 :name "Bob" :active false}])
; Output:
; | :id | :name | :active |
; |-----|-------|---------|
; |   1 | Alice |    true |
; |   2 |   Bob |   false |

; Pretty printing at REPL
(pprint (assoc user :updated-at (java.util.Date.)))
```

## Related Libraries

- clj-commons/clj-yaml - YAML formatting
- org.clojure/data.json - JSON formatting

## Resources

- Official Documentation: https://github.com/clj-commons/pretty
- Clojure pprint: https://clojure.github.io/clojure/clojure.pprint-api.html

## Notes

This project uses Pretty for formatted output in development and debugging.
