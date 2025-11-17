(ns clojure-skills.main-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-skills.main :as main]))

(deftest test-main-function-exists
  (testing "main namespace has -main function"
    (is (fn? main/-main))))
