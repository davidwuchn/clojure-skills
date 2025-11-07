(ns clojure-skills.main-test
  (:require [clojure.test :as t]))

(t/deftest test-okay
  (t/testing "testing"
    (t/is (true? false))))
