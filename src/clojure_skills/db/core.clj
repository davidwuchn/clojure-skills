(ns clojure-skills.db.core
  "Database connection management and operations."
  (:require
   [clojure-skills.config :as config]
   [clojure-skills.db.migrate :as migrate]))

(defn get-db
  "Get database connection spec from config.
  Returns a simple db-spec map that next.jdbc can use directly.
  Foreign keys are enabled for SQLite to support CASCADE deletes."
  ([]
   (get-db (config/load-config)))
  ([config]
   (let [db-path (config/get-db-path config)]
     {:dbtype "sqlite"
      :dbname db-path
      ;; Enable foreign keys for CASCADE delete support
      :foreign_keys "on"})))

(defn init-db
  "Initialize database with Ragtime migrations."
  ([db]
   (migrate/migrate-db db))
  ([]
   (let [db (get-db)]
     (init-db db))))
