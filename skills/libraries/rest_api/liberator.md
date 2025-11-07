---
name: liberator_rest_resources
description: REST resource library based on HTTP semantics and decision logic.
---

# Liberator

A Clojure library for building REST resources that conform to HTTP semantics.

## Overview

Liberator provides a way to build REST resources declaratively by defining decision handlers that determine HTTP responses based on content negotiation, caching, validation, and other HTTP features.

## Core Concepts

**Resources**: Declarative REST resource definitions.

```clojure
(require '[liberator.core :refer [resource]])

(defn user-resource [user-id]
  (resource
    :available-media-types ["application/json" "text/html"]
    :authorized? (fn [ctx] (check-auth (:request ctx)))
    :allowed-methods [:get :put :delete]
    :exists? (fn [ctx] (fetch-user user-id))
    :handle-ok (fn [ctx] (json-encode (:user ctx)))
    :handle-not-found (fn [ctx] "User not found")))
```

**Handlers**: Define what happens at each stage of request processing.

```clojure
(resource
  :available-media-types ["application/json"]
  :method-allowed? (fn [ctx] (= (get-in ctx [:request :method]) :get))
  :handle-ok (fn [ctx] {:status 200 :body "OK"}))
```

## Key Features

- HTTP semantic handling
- Content negotiation
- Method overloading
- Caching headers
- Conditional requests (If-Modified-Since, ETags)
- Language negotiation
- Decision tree based request handling

## When to Use

- Building RESTful HTTP services
- Handling complex HTTP semantics
- Content negotiation requirements
- Implementing proper HTTP caching

## When NOT to Use

- For simple endpoints (simpler routing might suffice)
- When you need high performance (overhead of decisions)

## Common Patterns

```clojure
(require '[liberator.core :refer [resource defresource]])

; Define a user resource
(defresource user [user-id]
  :available-media-types ["application/json"]
  :allowed-methods [:get :put :delete]
  :exists? (fn [ctx] (get-user user-id))
  :can-delete? (fn [ctx] true)
  :delete! (fn [ctx] (delete-user user-id))
  :put-to-different-url? false
  :can-put? (fn [ctx] true)
  :put! (fn [ctx] (update-user user-id (:body ctx)))
  :handle-ok (fn [ctx] (get-user user-id))
  :handle-not-found "User not found")

; Route to resource
; GET /users/123 -> (user 123)
```

## Related Libraries

- metosin/reitit - Routing
- http-kit/http-kit - HTTP server
- metosin/malli - Data validation

## Resources

- Official Documentation: https://github.com/liberator/liberator
- API Documentation: https://cljdoc.org/d/liberator/liberator

## Notes

This project uses Liberator for implementing proper REST resources with HTTP semantics.
