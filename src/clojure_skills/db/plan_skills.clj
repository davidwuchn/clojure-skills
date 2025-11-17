(ns clojure-skills.db.plan-skills
  "Database functions for managing plan-skill associations.

  All functions use HoneySQL for SQL generation and Malli for validation."
  (:require
   [honey.sql :as sql]
   [honey.sql.helpers :as h]
   [malli.core :as m]
   [malli.error :as me]
   [next.jdbc :as jdbc]))

;; ------------------------------------------------------------
;; Schemas
;; ------------------------------------------------------------

(def associate-skill-schema
  "Schema for associating a skill with a plan."
  [:map
   [:plan-id [:int {:min 1}]]
   [:skill-id [:int {:min 1}]]
   [:position {:optional true} [:maybe [:int {:min 0}]]]])

(def dissociate-skill-schema
  "Schema for dissociating a skill from a plan."
  [:map
   [:plan-id [:int {:min 1}]]
   [:skill-id [:int {:min 1}]]])

;; ------------------------------------------------------------
;; Validation Helpers
;; ------------------------------------------------------------

(defn validate!
  "Validate data against schema. Throws ex-info with humanized errors on failure."
  [schema data]
  (when-not (m/validate schema data)
    (let [explanation (m/explain schema data)
          errors (me/humanize explanation)]
      (throw (ex-info "Validation failed"
                      {:type ::validation-error
                       :errors errors
                       :data data}))))
  data)

;; ------------------------------------------------------------
;; Database Functions
;; ------------------------------------------------------------

(defn associate-skill-with-plan
  "Associate a skill with an implementation plan.

  Required keys in params:
    :plan-id - Plan ID (integer >= 1)
    :skill-id - Skill ID (integer >= 1)

  Optional keys:
    :position - Position in list (integer >= 0, default: 0)

  Returns the created association with all fields.
  Throws if validation fails or if association already exists.

  Example:
    (associate-skill-with-plan db {:plan-id 1
                                    :skill-id 5
                                    :position 1})"
  [db {:keys [plan-id skill-id position] :as params}]
  (validate! associate-skill-schema params)

  (try
    (let [pos (or position 0)
          sql-map (-> (h/insert-into :plan_skills)
                      (h/values [{:plan_id plan-id
                                  :skill_id skill-id
                                  :position pos}])
                      (h/returning :*)
                      (sql/format))]
      (jdbc/execute-one! db sql-map))
    (catch Exception e
      (throw (ex-info "Failed to associate skill with plan"
                      {:type ::database-error
                       :params params
                       :cause (.getMessage e)}
                      e)))))

(defn dissociate-skill-from-plan
  "Remove a skill association from an implementation plan.

  Required keys in params:
    :plan-id - Plan ID (integer >= 1)
    :skill-id - Skill ID (integer >= 1)

  Returns a map with :next.jdbc/update-count indicating number of rows deleted.
  Returns 0 if association did not exist.

  Example:
    (dissociate-skill-from-plan db {:plan-id 1
                                     :skill-id 5})"
  [db {:keys [plan-id skill-id] :as params}]
  (validate! dissociate-skill-schema params)

  (try
    (let [sql-map (-> (h/delete-from :plan_skills)
                      (h/where [:and
                                [:= :plan_id plan-id]
                                [:= :skill_id skill-id]])
                      (sql/format))]
      (jdbc/execute-one! db sql-map))
    (catch Exception e
      (throw (ex-info "Failed to dissociate skill from plan"
                      {:type ::database-error
                       :params params
                       :cause (.getMessage e)}
                      e)))))

(defn list-plan-skills
  "List all skills associated with an implementation plan.

  Returns a sequence of skill records with full details from the skills table
  plus association metadata (position, created_at).

  Results are ordered by position ascending.

  Example:
    (list-plan-skills db 1)
    => ({:skills/id 5
         :skills/name \"malli\"
         :skills/category \"libraries/data_validation\"
         :skills/title \"Malli Schema Validation\"
         :skills/description \"...\"
         :plan_skills/position 1
         :plan_skills/created_at \"2025-11-17 12:34:56\"}
        ...)"
  [db plan-id]
  (when-not (int? plan-id)
    (throw (ex-info "plan-id must be an integer" {:plan-id plan-id})))
  (when (< plan-id 1)
    (throw (ex-info "plan-id must be >= 1" {:plan-id plan-id})))

  (try
    (let [sql-map (-> (h/select :s.id :s.path :s.category :s.name :s.title
                                :s.description :ps.position :ps.created_at)
                      (h/from [:plan_skills :ps])
                      (h/join [:skills :s] [:= :ps.skill_id :s.id])
                      (h/where [:= :ps.plan_id plan-id])
                      (h/order-by [:ps.position :asc])
                      (sql/format))]
      (jdbc/execute! db sql-map))
    (catch Exception e
      (throw (ex-info "Failed to list plan skills"
                      {:type ::database-error
                       :plan-id plan-id
                       :cause (.getMessage e)}
                      e)))))

(defn get-skill-by-name
  "Get a skill by its name.

  Returns the skill or nil if not found.

  Example:
    (get-skill-by-name db \"malli\")"
  [db name]
  (validate! [:string {:min 1}] name)

  (try
    (let [query (-> (h/select :*)
                    (h/from :skills)
                    (h/where [:= :name name])
                    (sql/format))]
      (jdbc/execute-one! db query))
    (catch Exception e
      (throw (ex-info "Failed to get skill by name"
                      {:type ::database-error
                       :name name
                       :cause (.getMessage e)}
                      e)))))

(defn get-skill-by-path
  "Get a skill by its file path.

  Returns the skill or nil if not found.

  Example:
    (get-skill-by-path db \"skills/libraries/data_validation/malli.md\")"
  [db path]
  (validate! [:string {:min 1}] path)

  (try
    (let [query (-> (h/select :*)
                    (h/from :skills)
                    (h/where [:= :path path])
                    (sql/format))]
      (jdbc/execute-one! db query))
    (catch Exception e
      (throw (ex-info "Failed to get skill by path"
                      {:type ::database-error
                       :path path
                       :cause (.getMessage e)}
                      e)))))
