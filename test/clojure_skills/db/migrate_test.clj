(ns clojure-skills.db.migrate-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure-skills.db.migrate :as migrate]
            [next.jdbc :as jdbc]
            [clojure-skills.test-utils :as tu]))

;; For migration tests, we want to test the migration process itself,
;; so we don't want the fixture to automatically run migrations
(defn test-fixture [f]
  ;; Use a file-based database for migration tests, not in-memory
  ;; This is necessary because Ragtime creates its own connection and
  ;; in-memory databases are not truly shared across connections
  (let [db-path (str "test-migrate-" (random-uuid) ".db")
        db-spec {:dbtype "sqlite" :dbname db-path}
        datasource (jdbc/get-datasource db-spec)]
    (try
      ;; Bind both the datasource and db-spec to dynamic vars
      (binding [tu/*test-db* datasource
                tu/*test-db-spec* db-spec]
        (f))
      (finally
        ;; Clean up the test database file
        (tu/cleanup-test-db db-path)))))

(use-fixtures :each test-fixture)

(deftest test-migrate-db
  (testing "migrate-db creates schema on new database"
    ;; Use the db-spec from the fixture for migrations
    (migrate/migrate-db tu/*test-db-spec*)

    ;; Check that ragtime_migrations table exists
    (let [result (jdbc/execute-one! tu/*test-db* ["SELECT name FROM sqlite_master WHERE type='table' AND name='ragtime_migrations'"])]
      (is (some? result) "ragtime_migrations table should exist"))

    ;; Check that skills table exists
    (let [result (jdbc/execute-one! tu/*test-db* ["SELECT name FROM sqlite_master WHERE type='table' AND name='skills'"])]
      (is (some? result) "skills table should exist"))

    ;; Check that prompts table exists
    (let [result (jdbc/execute-one! tu/*test-db* ["SELECT name FROM sqlite_master WHERE type='table' AND name='prompts'"])]
      (is (some? result) "prompts table should exist"))

    ;; Check that prompt_skills table exists
    (let [result (jdbc/execute-one! tu/*test-db* ["SELECT name FROM sqlite_master WHERE type='table' AND name='prompt_skills'"])]
      (is (some? result) "prompt_skills table should exist"))

    ;; Check that FTS tables exist
    (let [result (jdbc/execute-one! tu/*test-db* ["SELECT name FROM sqlite_master WHERE type='table' AND name='skills_fts'"])]
      (is (some? result) "skills_fts table should exist"))

    (let [result (jdbc/execute-one! tu/*test-db* ["SELECT name FROM sqlite_master WHERE type='table' AND name='prompts_fts'"])]
      (is (some? result) "prompts_fts table should exist"))

    ;; Check that at least one migration was applied
    (let [migrations (jdbc/execute! tu/*test-db* ["SELECT * FROM ragtime_migrations"])]
      (is (pos? (count migrations)) "At least one migration should be applied"))))

(deftest test-migrate-db-idempotent
  (testing "migrate-db is idempotent - running twice doesn't fail"
    ;; First migration
    (migrate/migrate-db tu/*test-db-spec*)
    (let [migrations1 (jdbc/execute! tu/*test-db* ["SELECT * FROM ragtime_migrations"])]
      ;; Second migration
      (migrate/migrate-db tu/*test-db-spec*)
      (let [migrations2 (jdbc/execute! tu/*test-db* ["SELECT * FROM ragtime_migrations"])]
        ;; Should have same number of migrations
        (is (= (count migrations1) (count migrations2))
            "Running migrate twice should not apply migrations again")))))

(deftest test-schema-structure
  (testing "migrated schema has correct table structure"
    (migrate/migrate-db tu/*test-db-spec*)

    ;; Test that we can insert into skills table
    (jdbc/execute! tu/*test-db* ["INSERT INTO skills (path, category, name, content, file_hash, size_bytes, token_count) 
                      VALUES (?, ?, ?, ?, ?, ?, ?)"
                                 "test/path.md" "test" "test" "content" "hash123" 100 25])

    (let [skill (jdbc/execute-one! tu/*test-db* ["SELECT * FROM skills WHERE path = ?" "test/path.md"])]
      (is (some? skill))
      (is (= "test/path.md" (:skills/path skill)))
      (is (= "test" (:skills/category skill)))
      (is (some? (:skills/created_at skill)))
      (is (some? (:skills/updated_at skill))))

    ;; Test that we can insert into prompts table
    (jdbc/execute! tu/*test-db* ["INSERT INTO prompts (path, name, content, file_hash, size_bytes, token_count) 
                      VALUES (?, ?, ?, ?, ?, ?)"
                                 "prompts/test.md" "test_prompt" "prompt content" "hash456" 200 50])

    (let [prompt (jdbc/execute-one! tu/*test-db* ["SELECT * FROM prompts WHERE path = ?" "prompts/test.md"])]
      (is (some? prompt))
      (is (= "prompts/test.md" (:prompts/path prompt)))
      (is (= "test_prompt" (:prompts/name prompt)))
      (is (some? (:prompts/created_at prompt)))
      (is (some? (:prompts/updated_at prompt))))))