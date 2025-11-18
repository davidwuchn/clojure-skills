(ns clojure-skills.db.plan-results
  "Database functions for managing plan results.

  All functions use HoneySQL for SQL generation and Malli for validation.

  Outcome values: success, failure, partial"
  (:require
   [clojure.string :as str]
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [malli.core :as m]
   [malli.error :as me]
   [next.jdbc :as jdbc]))

(set! *warn-on-reflection* true)

;; Schemas

(def valid-outcomes
  "Valid plan result outcomes."
  #{"success" "failure" "partial"})

(def outcome-schema
  "Schema for plan result outcome."
  [:enum "success" "failure" "partial"])

(def create-result-schema
  "Schema for creating a new plan result."
  [:map
   [:plan_id [:int {:min 1}]]
   [:outcome outcome-schema]
   [:summary [:string {:min 1 :max 1000}]]
   [:challenges {:optional true} [:maybe :string]]
   [:solutions {:optional true} [:maybe :string]]
   [:lessons_learned {:optional true} [:maybe :string]]
   [:metrics {:optional true} [:maybe :string]]])

(def update-result-schema
  "Schema for updating a plan result."
  [:map
   {:min 1} ;; At least one field required
   [:outcome {:optional true} outcome-schema]
   [:summary {:optional true} [:string {:min 1 :max 1000}]]
   [:challenges {:optional true} [:maybe :string]]
   [:solutions {:optional true} [:maybe :string]]
   [:lessons_learned {:optional true} [:maybe :string]]
   [:metrics {:optional true} [:maybe :string]]])

(def search-options-schema
  "Schema for search options."
  [:map
   [:max-results {:optional true} [:int {:min 1 :max 1000}]]])

;; Validation helpers

(defn validate!
  "Validate data against schema. Throws on validation error."
  [schema data]
  (when-not (m/validate schema data)
    (throw (ex-info "Validation failed"
                    {:type ::validation-error
                     :errors (me/humanize (m/explain schema data))
                     :data data})))
  data)

(defn validate-result-exists!
  "Validate that a result exists. Throws if result is nil."
  [result]
  (when-not result
    (throw (ex-info "Plan result not found"
                    {:type ::not-found})))
  result)

;; CRUD Operations

(defn create-result
  "Create a new plan result.

  Required keys in result-map:
    :plan_id - ID of the plan (must exist)
    :outcome - Result outcome (success, failure, partial)
    :summary - Brief outcome summary (required, 1-1000 chars, searchable)

  Optional keys:
    :challenges - What was difficult (searchable)
    :solutions - How challenges were solved (searchable)
    :lessons_learned - What was learned (searchable)
    :metrics - JSON string with quantitative data

  Returns the created result with all fields.

  Throws if plan_id doesn't exist or if result already exists for this plan.

  Example:
    (create-result db {:plan_id 1
                       :outcome \"success\"
                       :summary \"Successfully implemented feature X\"
                       :challenges \"Database schema design was complex\"
                       :solutions \"Used migration system to iterate\"
                       :lessons_learned \"Start with simple schema, iterate\"
                       :metrics \"{\\\"lines_changed\\\": 245}\"})"
  [db result-map]
  (validate! create-result-schema result-map)

  (try
    ;; Using raw SQL for INSERT...RETURNING because HoneySQL has issues
    ;; with SQLite's RETURNING clause
    (jdbc/execute-one!
     db
     ["INSERT INTO plan_results
       (plan_id, outcome, summary, challenges, solutions, lessons_learned, metrics)
       VALUES (?, ?, ?, ?, ?, ?, ?)
       RETURNING *"
      (:plan_id result-map)
      (:outcome result-map)
      (:summary result-map)
      (:challenges result-map)
      (:solutions result-map)
      (:lessons_learned result-map)
      (:metrics result-map)])
    (catch Exception e
      (let [msg (.getMessage e)]
        (cond
          (str/includes? msg "UNIQUE constraint failed")
          (throw (ex-info "Result already exists for this plan"
                          {:type ::duplicate-error
                           :plan_id (:plan_id result-map)}
                          e))

          (str/includes? msg "FOREIGN KEY constraint failed")
          (throw (ex-info "Plan not found"
                          {:type ::foreign-key-error
                           :plan_id (:plan_id result-map)}
                          e))

          :else
          (throw (ex-info "Failed to create plan result"
                          {:type ::database-error
                           :result-map result-map
                           :cause msg}
                          e)))))))

(defn get-result-by-plan-id
  "Get plan result by plan ID.

  Returns the result or nil if not found.

  Example:
    (get-result-by-plan-id db 1)"
  [db plan-id]
  (validate! [:int {:min 1}] plan-id)

  (jdbc/execute-one!
   db
   (-> (h/select :*)
       (h/from :plan_results)
       (h/where [:= :plan_id plan-id])
       (sql/format))))

(defn update-result
  "Update a plan result.

  Takes a plan_id and a map of fields to update.
  Only provided fields are updated; nil values are preserved.

  Updates updated_at timestamp automatically.

  Returns the updated result.

  Example:
    (update-result db 1 {:outcome \"partial\"
                         :challenges \"Additional issues found\"})"
  [db plan-id update-map]
  (validate! [:int {:min 1}] plan-id)
  (validate! update-result-schema update-map)

  ;; Verify result exists
  (-> (get-result-by-plan-id db plan-id)
      validate-result-exists!)

  (try
    ;; Build SET clause dynamically based on provided fields
    (let [set-clause (assoc update-map :updated_at [:datetime "now"])
          sql (-> (h/update :plan_results)
                  (h/set set-clause)
                  (h/where [:= :plan_id plan-id])
                  (sql/format {:returning [:*]}))]
      (jdbc/execute-one! db sql))
    (catch Exception e
      (throw (ex-info "Failed to update plan result"
                      {:type ::database-error
                       :plan-id plan-id
                       :update-map update-map
                       :cause (.getMessage e)}
                      e)))))

(defn delete-result
  "Delete a plan result.

  Returns true if deleted, throws if not found.

  Example:
    (delete-result db 1)"
  [db plan-id]
  (validate! [:int {:min 1}] plan-id)

  ;; Verify result exists
  (-> (get-result-by-plan-id db plan-id)
      validate-result-exists!)

  (jdbc/execute-one!
   db
   (-> (h/delete-from :plan_results)
       (h/where [:= :plan_id plan-id])
       (sql/format)))
  true)

(defn search-results
  "Search plan results using FTS5 full-text search.

  The query uses FTS5 syntax and searches across summary, challenges,
  solutions, and lessons_learned.

  Results include snippets showing matching text and are ranked by relevance.

  Options:
    :max-results - Maximum number of results (default: 50, max: 1000)

  Returns a sequence of results with:
    - All result fields
    - :snippet - Text snippet showing matches (with [...] markers)
    - :rank - Relevance score (lower is better)

  Example:
    (search-results db \"database migration\")
    (search-results db \"FTS5 OR search\" :max-results 10)

  FTS5 query syntax:
    - \"word1 word2\" - Both words (AND)
    - \"word1 OR word2\" - Either word
    - \"\\\"exact phrase\\\"\" - Exact phrase match
    - \"word*\" - Prefix match"
  [db query & {:keys [max-results]
               :or {max-results 50}
               :as opts}]
  (validate! [:string {:min 1}] query)
  ;; opts can be nil when no keyword arguments are provided
  (validate! search-options-schema (or opts {}))

  (try
    ;; Using raw SQL for FTS5 MATCH clause (HoneySQL doesn't support it well)
    ;; Column parameter -1 tells snippet() to auto-select the best matching column
    (let [sql-str "SELECT r.*,
                          snippet(plan_results_fts, -1, '[', ']', '...', 30) as snippet,
                          rank
                   FROM plan_results_fts
                   JOIN plan_results r ON plan_results_fts.rowid = r.id
                   WHERE plan_results_fts MATCH ?
                   ORDER BY rank
                   LIMIT ?"
          params [query max-results]]
      (jdbc/execute! db (into [sql-str] params)))
    (catch Exception e
      (throw (ex-info "Failed to search plan results"
                      {:type ::database-error
                       :query query
                       :options opts
                       :cause (.getMessage e)}
                      e)))))
