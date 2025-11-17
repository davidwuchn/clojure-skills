(ns clojure-skills.db.plan-skills-test
  "Tests for clojure-skills.db.plan-skills namespace."
  (:require
   [clojure.test :refer [deftest testing is use-fixtures]]
   [clojure-skills.db.plan-skills :as plan-skills]
   [clojure-skills.db.plans :as plans]
   [clojure-skills.db.migrate :as migrate]
   [malli.core :as m]
   [next.jdbc :as jdbc]))

;; ------------------------------------------------------------
;; Test Fixtures
;; ------------------------------------------------------------

(def test-db-path "test-plan-skills.db")

(def ^:dynamic *test-db* nil)

(def ^:dynamic *test-plan-id* nil)

(def ^:dynamic *test-skill-ids* nil)

(defn with-test-db
  "Fixture to create and migrate a test database with sample data."
  [f]
  (let [db-spec {:dbtype "sqlite" :dbname test-db-path :foreign_keys "true"}
        ds (jdbc/get-datasource db-spec)]
    ;; Clean up any existing test db
    (try
      (.delete (java.io.File. test-db-path))
      (catch Exception _))

    ;; Run migrations
    (migrate/migrate-db db-spec)

    ;; Create test plan
    (let [test-plan (plans/create-plan ds {:name "Test Plan for Skills"})
          plan-id (:implementation_plans/id test-plan)]

      ;; Create test skills
      (jdbc/execute! ds
                     ["INSERT INTO skills (path, category, name, content, file_hash, size_bytes)
                       VALUES (?, ?, ?, ?, ?, ?)"
                      "/test/skill1.md" "test" "test-skill-1" "Test content 1" "hash1" 100])
      (jdbc/execute! ds
                     ["INSERT INTO skills (path, category, name, content, file_hash, size_bytes)
                       VALUES (?, ?, ?, ?, ?, ?)"
                      "/test/skill2.md" "test" "test-skill-2" "Test content 2" "hash2" 200])
      (jdbc/execute! ds
                     ["INSERT INTO skills (path, category, name, content, file_hash, size_bytes)
                       VALUES (?, ?, ?, ?, ?, ?)"
                      "/test/skill3.md" "test/nested" "test-skill-3" "Test content 3" "hash3" 300])

      ;; Get skill IDs
      (let [skills (jdbc/execute! ds ["SELECT id FROM skills ORDER BY id"])
            skill-ids (mapv :skills/id skills)]

        ;; Run tests with datasource, plan ID, and skill IDs
        (binding [*test-db* ds
                  *test-plan-id* plan-id
                  *test-skill-ids* skill-ids]
          (f))))

    ;; Clean up
    (try
      (.delete (java.io.File. test-db-path))
      (catch Exception _))))

(use-fixtures :each with-test-db)

;; ------------------------------------------------------------
;; Helper Functions
;; ------------------------------------------------------------

(defn get-skill-ids
  "Get test skill IDs."
  []
  *test-skill-ids*)

(defn get-first-skill-id
  "Get first test skill ID."
  []
  (first *test-skill-ids*))

(defn get-second-skill-id
  "Get second test skill ID."
  []
  (second *test-skill-ids*))

;; ------------------------------------------------------------
;; Schema Tests
;; ------------------------------------------------------------

(deftest schema-validation-test
  (testing "associate-skill-schema validates correctly"
    (is (m/validate plan-skills/associate-skill-schema
                    {:plan-id 1
                     :skill-id 5
                     :position 1}))

    (is (m/validate plan-skills/associate-skill-schema
                    {:plan-id 1
                     :skill-id 5})) ; position is optional

    (is (not (m/validate plan-skills/associate-skill-schema
                         {:plan-id 0 :skill-id 1}))) ; plan-id must be >= 1

    (is (not (m/validate plan-skills/associate-skill-schema
                         {:plan-id 1 :skill-id 0}))) ; skill-id must be >= 1

    (is (not (m/validate plan-skills/associate-skill-schema
                         {:plan-id 1}))) ; missing skill-id

    (is (not (m/validate plan-skills/associate-skill-schema
                         {:skill-id 1})))) ; missing plan-id

  (testing "dissociate-skill-schema validates correctly"
    (is (m/validate plan-skills/dissociate-skill-schema
                    {:plan-id 1 :skill-id 5}))

    (is (not (m/validate plan-skills/dissociate-skill-schema
                         {:plan-id 0 :skill-id 1})))

    (is (not (m/validate plan-skills/dissociate-skill-schema
                         {:plan-id 1}))) ; missing skill-id

    (is (not (m/validate plan-skills/dissociate-skill-schema
                         {:skill-id 1}))))) ; missing plan-id

;; ------------------------------------------------------------
;; Associate Skill Tests
;; ------------------------------------------------------------

(deftest associate-skill-with-plan-test
  (testing "associate-skill-with-plan with valid data"
    (let [skill-id (get-first-skill-id)
          result (plan-skills/associate-skill-with-plan *test-db*
                                                        {:plan-id *test-plan-id*
                                                         :skill-id skill-id
                                                         :position 1})]

      (is (some? result))
      (is (= *test-plan-id* (:plan_skills/plan_id result)))
      (is (= skill-id (:plan_skills/skill_id result)))
      (is (= 1 (:plan_skills/position result)))
      (is (some? (:plan_skills/created_at result)))))

  (testing "associate-skill-with-plan with default position"
    (let [skill-id (get-second-skill-id)
          result (plan-skills/associate-skill-with-plan *test-db*
                                                        {:plan-id *test-plan-id*
                                                         :skill-id skill-id})]

      (is (some? result))
      (is (= 0 (:plan_skills/position result))))) ; default position is 0

  (testing "associate-skill-with-plan with duplicate throws"
    (let [skill-id (get-first-skill-id)]
      ;; First association should work
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id skill-id
                                              :position 1})

      ;; Second association should fail (unique constraint)
      (is (thrown? Exception
                   (plan-skills/associate-skill-with-plan *test-db*
                                                          {:plan-id *test-plan-id*
                                                           :skill-id skill-id
                                                           :position 2})))))

  (testing "associate-skill-with-plan with invalid plan-id throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Validation failed"
                          (plan-skills/associate-skill-with-plan *test-db*
                                                                 {:plan-id 0
                                                                  :skill-id (get-first-skill-id)}))))

  (testing "associate-skill-with-plan with invalid skill-id throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Validation failed"
                          (plan-skills/associate-skill-with-plan *test-db*
                                                                 {:plan-id *test-plan-id*
                                                                  :skill-id 0}))))

  (testing "associate-skill-with-plan with non-existent skill-id fails"
    (is (thrown? Exception
                 (plan-skills/associate-skill-with-plan *test-db*
                                                        {:plan-id *test-plan-id*
                                                         :skill-id 99999})))))

;; ------------------------------------------------------------
;; Dissociate Skill Tests
;; ------------------------------------------------------------

(deftest dissociate-skill-from-plan-test
  (testing "dissociate-skill-from-plan removes association"
    (let [skill-id (get-first-skill-id)]
      ;; Create association
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id skill-id
                                              :position 1})

      ;; Dissociate
      (let [result (plan-skills/dissociate-skill-from-plan *test-db*
                                                           {:plan-id *test-plan-id*
                                                            :skill-id skill-id})]

        (is (some? result))
        (is (= 1 (:next.jdbc/update-count result)))

        ;; Verify removed
        (let [skills (plan-skills/list-plan-skills *test-db* *test-plan-id*)]
          (is (empty? skills))))))

  (testing "dissociate-skill-from-plan with non-existent association returns 0"
    (let [skill-id (get-first-skill-id)
          result (plan-skills/dissociate-skill-from-plan *test-db*
                                                         {:plan-id *test-plan-id*
                                                          :skill-id skill-id})]

      (is (= 0 (:next.jdbc/update-count result)))))

  (testing "dissociate-skill-from-plan with invalid plan-id throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Validation failed"
                          (plan-skills/dissociate-skill-from-plan *test-db*
                                                                  {:plan-id 0
                                                                   :skill-id (get-first-skill-id)}))))

  (testing "dissociate-skill-from-plan with invalid skill-id throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Validation failed"
                          (plan-skills/dissociate-skill-from-plan *test-db*
                                                                  {:plan-id *test-plan-id*
                                                                   :skill-id 0})))))

;; ------------------------------------------------------------
;; List Plan Skills Tests
;; ------------------------------------------------------------

(deftest list-plan-skills-test
  (testing "list-plan-skills returns empty for plan with no skills"
    (let [result (plan-skills/list-plan-skills *test-db* *test-plan-id*)]
      (is (empty? result))))

  (testing "list-plan-skills returns associated skills"
    (let [skill-ids (get-skill-ids)]
      ;; Associate multiple skills
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (nth skill-ids 0)
                                              :position 2})
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (nth skill-ids 1)
                                              :position 1})
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (nth skill-ids 2)
                                              :position 3})

      (let [result (plan-skills/list-plan-skills *test-db* *test-plan-id*)]
        (is (= 3 (count result)))

        ;; Verify ordered by position
        (is (= [1 2 3] (map :plan_skills/position result)))

        ;; Verify includes skill details
        (is (every? #(contains? % :skills/id) result))
        (is (every? #(contains? % :skills/name) result))
        (is (every? #(contains? % :skills/category) result))
        (is (every? #(contains? % :plan_skills/position) result)))))

  (testing "list-plan-skills with invalid plan-id throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"plan-id must be an integer"
                          (plan-skills/list-plan-skills *test-db* "not-an-int")))

    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"plan-id must be >= 1"
                          (plan-skills/list-plan-skills *test-db* 0)))))

;; ------------------------------------------------------------
;; Get Skill By Name/Path Tests
;; ------------------------------------------------------------

(deftest get-skill-by-name-test
  (testing "get-skill-by-name returns existing skill"
    (let [result (plan-skills/get-skill-by-name *test-db* "test-skill-1")]
      (is (some? result))
      (is (= "test-skill-1" (:skills/name result)))
      (is (= "test" (:skills/category result)))))

  (testing "get-skill-by-name with non-existent name returns nil"
    (is (nil? (plan-skills/get-skill-by-name *test-db* "does-not-exist"))))

  (testing "get-skill-by-name with empty string throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Validation failed"
                          (plan-skills/get-skill-by-name *test-db* "")))))

(deftest get-skill-by-path-test
  (testing "get-skill-by-path returns existing skill"
    (let [result (plan-skills/get-skill-by-path *test-db* "/test/skill1.md")]
      (is (some? result))
      (is (= "/test/skill1.md" (:skills/path result)))
      (is (= "test-skill-1" (:skills/name result)))))

  (testing "get-skill-by-path with non-existent path returns nil"
    (is (nil? (plan-skills/get-skill-by-path *test-db* "/does/not/exist.md"))))

  (testing "get-skill-by-path with empty string throws"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Validation failed"
                          (plan-skills/get-skill-by-path *test-db* "")))))

;; ------------------------------------------------------------
;; CASCADE Deletion Tests
;; ------------------------------------------------------------

(deftest cascade-deletion-test
  (testing "deleting plan removes associated skills"
    (let [skill-ids (get-skill-ids)]
      ;; Associate skills with plan
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (first skill-ids)
                                              :position 1})
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (second skill-ids)
                                              :position 2})

      ;; Verify associations exist
      (let [before (plan-skills/list-plan-skills *test-db* *test-plan-id*)]
        (is (= 2 (count before))))

      ;; Delete the plan
      (plans/delete-plan *test-db* *test-plan-id*)

      ;; Verify associations are gone
      (let [associations (jdbc/execute! *test-db*
                                        ["SELECT * FROM plan_skills WHERE plan_id = ?"
                                         *test-plan-id*])]
        (is (empty? associations)))

      ;; Verify skills themselves still exist
      (let [skills (jdbc/execute! *test-db*
                                  ["SELECT * FROM skills WHERE id IN (?, ?)"
                                   (first skill-ids)
                                   (second skill-ids)])]
        (is (= 2 (count skills)))))))

;; ------------------------------------------------------------
;; Integration Tests
;; ------------------------------------------------------------

(deftest full-lifecycle-test
  (testing "complete plan-skill association lifecycle"
    (let [skill-ids (get-skill-ids)]

      ;; Start with no associations
      (is (empty? (plan-skills/list-plan-skills *test-db* *test-plan-id*)))

      ;; Associate first skill
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (nth skill-ids 0)
                                              :position 1})

      ;; Verify one skill
      (is (= 1 (count (plan-skills/list-plan-skills *test-db* *test-plan-id*))))

      ;; Associate second skill
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (nth skill-ids 1)
                                              :position 2})

      ;; Verify two skills
      (is (= 2 (count (plan-skills/list-plan-skills *test-db* *test-plan-id*))))

      ;; Associate third skill
      (plan-skills/associate-skill-with-plan *test-db*
                                             {:plan-id *test-plan-id*
                                              :skill-id (nth skill-ids 2)
                                              :position 3})

      ;; Verify three skills in correct order
      (let [skills (plan-skills/list-plan-skills *test-db* *test-plan-id*)]
        (is (= 3 (count skills)))
        (is (= [1 2 3] (map :plan_skills/position skills)))
        (is (= skill-ids (map :skills/id skills))))

      ;; Dissociate middle skill
      (plan-skills/dissociate-skill-from-plan *test-db*
                                              {:plan-id *test-plan-id*
                                               :skill-id (nth skill-ids 1)})

      ;; Verify two skills remain
      (let [skills (plan-skills/list-plan-skills *test-db* *test-plan-id*)]
        (is (= 2 (count skills)))
        (is (= [(nth skill-ids 0) (nth skill-ids 2)]
               (map :skills/id skills))))

      ;; Dissociate all remaining
      (plan-skills/dissociate-skill-from-plan *test-db*
                                              {:plan-id *test-plan-id*
                                               :skill-id (nth skill-ids 0)})
      (plan-skills/dissociate-skill-from-plan *test-db*
                                              {:plan-id *test-plan-id*
                                               :skill-id (nth skill-ids 2)})

      ;; Verify empty
      (is (empty? (plan-skills/list-plan-skills *test-db* *test-plan-id*))))))