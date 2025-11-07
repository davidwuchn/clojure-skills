---
name: malli_schema_validation
description: Data validation and schema definition using Malli.
---

# Malli

Malli is a data-driven schema and validation library for Clojure.

## Overview

Malli provides a way to define, validate, and work with data schemas. It is data-driven, meaning schemas are represented as Clojure data structures rather than special objects.

## Core Concepts

**Schemas**: Schemas describe the structure and constraints of data.

```clojure
(require '[malli.core :as m])

; Simple schema
(m/schema int?)

; Map schema
{:user (m/schema [:map
                  [:name string?]
                  [:email string?]
                  [:age int?]])}
```

**Validation**: Validate data against schemas.

```clojure
(m/validate [:map [:name string?]] {:name "Alice"})
; => true

(m/validate [:map [:name string?]] {:name 123})
; => false
```

**Error Messages**: Get detailed error information when validation fails.

```clojure
(m/explain [:map [:name string?]] {:name 123})
; => {:errors ...}
```

## Key Features

- Data-driven schema definition
- Composable and reusable schemas
- Detailed validation error messages
- Support for custom validators
- Integration with Clojure specs
- Performance optimized

## When to Use

- Validating API request/response data
- Defining data contracts
- Generating documentation from schemas
- Building form validation
- Type checking at runtime

## When NOT to Use

- For static type checking (use a language with types)
- When performance is extremely critical
- For simple type checks (just use predicates)

## Common Patterns

```clojure
; Reusable schema definitions
(def user-schema [:map
                  [:id int?]
                  [:name string?]
                  [:email string?]])

; Validation with coercion
(m/coerce user-schema {:id "123" :name "Alice" :email "alice@example.com"})
; => {:id 123, :name "Alice", :email "alice@example.com"}
```

## Related Libraries

- org.clojure/spec - Alternative validation approach
- metosin/reitit - Uses Malli for route data validation
- liberator/liberator - REST resources

## Resources

- Official Documentation: https://github.com/metosin/malli
- API Documentation: https://cljdoc.org/d/metosin/malli

## Notes

This project uses Malli for validating configuration and API contracts.
