(ns clojure-skills.config-test
  "Tests for configuration loading and command filtering."
  (:require
   [clojure.test :refer [deftest testing is]]
   [clojure-skills.config :as config]))

(deftest command-filtering-test
  (testing "Command filtering with permissions"
    (let [test-commands
          [{:command "db"
            :description "Database operations"
            :subcommands
            [{:command "init" :description "Init DB"}
             {:command "reset" :description "Reset DB"}
             {:command "stats" :description "DB Stats"}]}
           {:command "plan"
            :description "Plan operations"
            :subcommands
            [{:command "create" :description "Create plan"}
             {:command "delete" :description "Delete plan"}]}
           {:command "skill"
            :description "Skill operations"
            :subcommands
            [{:command "search" :description "Search skills"}
             {:command "list" :description "List skills"}]}]

          test-permissions
          {:db {:reset false}
           :plan {:delete false}}]

      (testing "Disabled commands are filtered out"
        (let [filtered (config/filter-commands test-commands test-permissions [])]
          ;; db/reset should be removed
          (is (not (some #(= "reset" (:command %))
                         (->> filtered
                              (filter #(= "db" (:command %)))
                              first
                              :subcommands))))

          ;; plan/delete should be removed
          (is (not (some #(= "delete" (:command %))
                         (->> filtered
                              (filter #(= "plan" (:command %)))
                              first
                              :subcommands))))

          ;; Enabled commands should remain
          (is (some #(= "init" (:command %))
                    (->> filtered
                         (filter #(= "db" (:command %)))
                         first
                         :subcommands)))

          (is (some #(= "stats" (:command %))
                    (->> filtered
                         (filter #(= "db" (:command %)))
                         first
                         :subcommands)))

          (is (some #(= "create" (:command %))
                    (->> filtered
                         (filter #(= "plan" (:command %)))
                         first
                         :subcommands)))

          ;; Commands without specific permissions should remain
          (is (some #(= "skill" (:command %)) filtered))
          (is (some #(= "search" (:command %))
                    (->> filtered
                         (filter #(= "skill" (:command %)))
                         first
                         :subcommands)))
          (is (some #(= "list" (:command %))
                    (->> filtered
                         (filter #(= "skill" (:command %)))
                         first
                         :subcommands)))))))

  (testing "Command enabled check"
    (let [permissions {:db {:reset false :init true}
                       :plan {:delete false}}]

      (testing "Explicitly disabled commands return false"
        (is (false? (config/command-enabled? permissions [:db :reset])))
        (is (false? (config/command-enabled? permissions [:plan :delete]))))

      (testing "Explicitly enabled commands return true"
        (is (true? (config/command-enabled? permissions [:db :init]))))

      (testing "Commands without permissions return true by default"
        (is (true? (config/command-enabled? permissions [:db :stats])))
        (is (true? (config/command-enabled? permissions [:plan :create]))))

      (testing "Top-level commands without permissions return true by default"
        (is (true? (config/command-enabled? permissions [:skill])))))))

(deftest top-level-command-disabling-test
  (testing "Top-level command disabling"
    (let [permissions {:plan false
                       :db {:reset false}}]

      (testing "Top-level disabled command returns false for all subcommands"
        (is (false? (config/command-enabled? permissions [:plan])))
        (is (false? (config/command-enabled? permissions [:plan :create])))
        (is (false? (config/command-enabled? permissions [:plan :delete])))
        (is (false? (config/command-enabled? permissions [:plan :show]))))

      (testing "Nested disabled commands still work"
        (is (false? (config/command-enabled? permissions [:db :reset]))))

      (testing "Mixed configuration works correctly"
        (is (true? (config/command-enabled? permissions [:db])))
        (is (true? (config/command-enabled? permissions [:db :init])))
        (is (true? (config/command-enabled? permissions [:db :stats]))))

      (testing "Commands without any permissions return true by default"
        (is (true? (config/command-enabled? permissions [:skill])))
        (is (true? (config/command-enabled? permissions [:skill :search])))
        (is (true? (config/command-enabled? permissions [:task-list])))))))

(deftest command-filtering-with-top-level-disabling-test
  (testing "Command filtering with top-level permissions"
    (let [test-commands
          [{:command "db"
            :description "Database operations"
            :subcommands
            [{:command "init" :description "Init DB"}
             {:command "reset" :description "Reset DB"}
             {:command "stats" :description "DB Stats"}]}
           {:command "plan"
            :description "Plan operations"
            :subcommands
            [{:command "create" :description "Create plan"}
             {:command "delete" :description "Delete plan"}]}
           {:command "skill"
            :description "Skill operations"
            :subcommands
            [{:command "search" :description "Search skills"}
             {:command "list" :description "List skills"}]}]

          test-permissions
          {:plan false
           :db {:reset false}}]

      (testing "Top-level disabled commands are completely filtered out"
        (let [filtered (config/filter-commands test-commands test-permissions [])]
          ;; plan command should be completely removed
          (is (not (some #(= "plan" (:command %)) filtered)))))

      (testing "Nested disabled commands are filtered out"
        (let [filtered (config/filter-commands test-commands test-permissions [])]
          ;; db/reset should be removed
          (is (not (some #(= "reset" (:command %))
                         (->> filtered
                              (filter #(= "db" (:command %)))
                              first
                              :subcommands))))))

      (testing "Enabled commands remain"
        (let [filtered (config/filter-commands test-commands test-permissions [])]
          ;; db/init and db/stats should remain
          (is (some #(= "init" (:command %))
                    (->> filtered
                         (filter #(= "db" (:command %)))
                         first
                         :subcommands)))

          (is (some #(= "stats" (:command %))
                    (->> filtered
                         (filter #(= "db" (:command %)))
                         first
                         :subcommands)))

          ;; skill command and subcommands should remain
          (is (some #(= "skill" (:command %)) filtered))
          (is (some #(= "search" (:command %))
                    (->> filtered
                         (filter #(= "skill" (:command %)))
                         first
                         :subcommands)))
          (is (some #(= "list" (:command %))
                    (->> filtered
                         (filter #(= "skill" (:command %)))
                         first
                         :subcommands))))))))