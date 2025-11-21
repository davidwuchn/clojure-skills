(ns clojure-skills.main-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure-skills.test-utils :as tu]
            [clojure-skills.main :as main]
            [next.jdbc :as jdbc]))

(use-fixtures :each tu/use-sqlite-database)

(deftest test-main-function-exists

  (testing "main namespace has -main function"

    (is (nil? tu/*connection*))
    (is (nil? (jdbc/execute! tu/*connection* ["select * from sqlite_master;"])))
    (is (fn? main/-main))))
