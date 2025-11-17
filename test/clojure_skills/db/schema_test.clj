(ns clojure-skills.db.schema-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure-skills.db.schema :as schema]
            [next.jdbc :as jdbc]
            [clojure-skills.test-utils :as tu]))

;; Use shared test database fixture
(use-fixtures :each (tu/with-test-db-fixture {:in-memory? false :db-path "test-db.db"}))

(deftest test-migrations-structure
  (testing "migrations is a vector of maps"
    (is (vector? schema/migrations))
    (is (pos? (count schema/migrations))))

  (testing "each migration has required keys"
    (doseq [migration schema/migrations]
      (is (contains? migration :version))
      (is (contains? migration :up))
      (is (contains? migration :down))
      (is (vector? (:up migration)))
      (is (vector? (:down migration)))
      (is (pos-int? (:version migration))))))

(deftest test-schema-version
  (testing "schema-version is defined"
    (is (pos-int? schema/schema-version))))

(deftest test-get-current-version
  (testing "get-current-version returns 0 for new database"
    (let [db {:dbtype "sqlite" :dbname "test-db.db"}]
      (is (= 0 (schema/get-current-version db))))))

(deftest test-migrate
  (testing "migrate creates schema on new database"
    (let [db {:dbtype "sqlite" :dbname "test-db.db"}]
      (schema/migrate db)

      ;; Check that schema_version table exists
      (let [version (schema/get-current-version db)]
        (is (pos? version)))

      ;; Check that skills table exists
      (let [result (jdbc/execute-one! db ["SELECT name FROM sqlite_master WHERE type='table' AND name='skills'"])]
        (is (some? result)))

      ;; Check that prompts table exists
      (let [result (jdbc/execute-one! db ["SELECT name FROM sqlite_master WHERE type='table' AND name='prompts'"])]
        (is (some? result)))

      ;; Check that FTS tables exist
      (let [result (jdbc/execute-one! db ["SELECT name FROM sqlite_master WHERE type='table' AND name='skills_fts'"])]
        (is (some? result)))

      (let [result (jdbc/execute-one! db ["SELECT name FROM sqlite_master WHERE type='table' AND name='prompts_fts'"])]
        (is (some? result))))))

(deftest test-migrate-idempotent
  (testing "migrate is idempotent - running twice doesn't fail"
    (let [db {:dbtype "sqlite" :dbname "test-db.db"}]
      (schema/migrate db)
      (let [version1 (schema/get-current-version db)]
        (schema/migrate db)
        (let [version2 (schema/get-current-version db)]
          (is (= version1 version2)))))))

(deftest test-reset-database
  (testing "reset-database drops and recreates schema"
    (let [db {:dbtype "sqlite" :dbname "test-db.db"}]
      ;; First migration
      (schema/migrate db)

      ;; Insert test data
      (jdbc/execute! db ["INSERT INTO skills (path, category, name, content, file_hash, size_bytes) 
                          VALUES (?, ?, ?, ?, ?, ?)"
                         "test/path.md" "test" "test" "content" "hash123" 100])

      (let [count-before (jdbc/execute-one! db ["SELECT COUNT(*) as count FROM skills"])]
        (is (= 1 (:count count-before))))

      ;; Reset database
      (schema/reset-database db)

      ;; Check that tables are empty
      (let [count-after (jdbc/execute-one! db ["SELECT COUNT(*) as count FROM skills"])]
        (is (= 0 (:count count-after))))

      ;; Check that schema version is current
      (let [version (schema/get-current-version db)]
        (is (= schema/schema-version version))))))