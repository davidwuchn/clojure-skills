---
name: clj_yaml_yaml_parsing
description: YAML parsing and generation for Clojure.
---

# clj-yaml

A Clojure library for reading and writing YAML data.

## Overview

clj-commons/clj-yaml provides functions for parsing YAML strings into Clojure data structures and converting Clojure data to YAML format.

## Core Concepts

**Reading YAML**: Parse YAML strings into Clojure data.

```clojure
(require '[clj-yaml.core :as yaml])

(yaml/parse-string "name: Alice\nage: 30")
; => {:name "Alice", :age 30}

; Multiple documents
(yaml/parse-string "---\nname: Alice\n---\nname: Bob")
; => ({:name "Alice"} {:name "Bob"})
```

**Writing YAML**: Convert Clojure data to YAML strings.

```clojure
(yaml/generate-string {:name "Alice" :age 30})
; => "name: Alice\nage: 30\n"

; Pretty printing
(yaml/generate-string {:users [{:name "Alice"} {:name "Bob"}]})
```

## Key Features

- Simple read/write API
- Support for multiple YAML documents
- Custom data type handling
- Keyword preservation
- Comment preservation options

## When to Use

- Reading configuration files (YAML is human-friendly)
- Parsing YAML-based data formats
- Serializing Clojure data to YAML
- Working with deployment manifests

## When NOT to Use

- For complex data structures (JSON may be simpler)
- When you need strict type validation

## Common Patterns

```clojure
; Reading configuration
(require '[clj-yaml.core :as yaml]
         '[clojure.java.io :as io])

(def config (yaml/parse-string (slurp "config.yaml")))

; Configuration file example:
; database:
;   host: localhost
;   port: 5432
;   name: myapp_db

; Writing configuration
(yaml/generate-string
  {:database {:host "localhost"
              :port 5432
              :name "myapp_db"}})
```

## Related Libraries

- org.clojure/data.json - JSON parsing
- aero/aero - Configuration library

## Resources

- Official Documentation: https://github.com/clj-commons/clj-yaml
- API Documentation: https://cljdoc.org/d/clj-commons/clj-yaml

## Notes

This project uses clj-yaml for reading YAML configuration files and serializing configuration data.
