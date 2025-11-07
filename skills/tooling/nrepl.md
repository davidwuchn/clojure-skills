---
name: nrepl_network_repl_protocol
description: Network REPL protocol for remote Clojure evaluation.
---

# nREPL

A network-based REPL protocol that enables remote code evaluation and interactive development.

## Overview

nREPL provides a networked REPL server that allows editors and tools to connect and evaluate code remotely. It's the foundation for most modern Clojure editor integration.

## Core Concepts

**Server**: Start nREPL server.

```clojure
; In terminal:
; clojure -M:nrepl          ; Start server (port 7889)

; In deps.edn:
; :nrepl
; {:extra-deps {nrepl/nrepl {:mvn/version "1.3.0"}
;               cider/cider-nrepl {:mvn/version "0.56.0"}}
;  :jvm-opts ["-Djdk.attach.allowAttachSelf"]
;  :main-opts ["-m" "nrepl.cmdline" "--port" "7889"]}
```

**Client Connection**: Connect from editor.

```clojure
; Emacs CIDER:
; M-x cider-connect
; localhost:7889

; VSCode Calva:
; Ctrl+Alt+C, Ctrl+Alt+C
; localhost:7889

; IntelliJ IDEA:
; Tools > REPL > Connect to REPL
; localhost:7889
```

## Key Features

- Remote code evaluation
- Editor integration support
- Session management
- Middleware support
- Full REPL functionality over network
- Long-lived connections

## When to Use

- Editor-based development
- Remote debugging
- Distributed evaluation
- Interactive development

## When NOT to Use

- Simple scripts (use clojure CLI)

## Common Patterns

```clojure
; Start server in one terminal:
; bb nrepl
; Listening on nrepl://127.0.0.1:7889

; Connect from editor (Emacs, VSCode, IntelliJ, etc.)
; Then evaluate code from editor

; Example workflow:
; 1. Start: bb nrepl
; 2. In editor: Connect to REPL (port 7889)
; 3. Edit code
; 4. Evaluate form: Ctrl+Enter (editor-specific)
; 5. See results in REPL

; Get system info through connected REPL:
(require '[clojure.java.shell :as sh])
(sh/sh "uname" "-a")

; Access application state:
(require '[my.app :as app])
(app/get-users)

; Reload code:
(require '[clj-reload.core :as reload])
(reload/reload)
```

## Related Libraries

- cider/cider-nrepl - CIDER middleware
- clojure-lsp/clojure-lsp - Language server

## Resources

- Official Documentation: https://github.com/nrepl/nrepl
- CIDER Documentation: https://docs.cider.mx/
- Calva Documentation: https://calva.io/

## Notes

This project uses nREPL for interactive editor-based development. Start with `bb nrepl`.
