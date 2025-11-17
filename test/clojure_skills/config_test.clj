(ns clojure-skills.config-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure-skills.config :as config]))

(deftest test-get-home-dir
  (testing "get-home-dir returns a non-empty string"
    (let [home (config/get-home-dir)]
      (is (string? home))
      (is (seq home)))))

(deftest test-get-xdg-config-home
  (testing "get-xdg-config-home returns default or env var"
    (let [xdg-home (config/get-xdg-config-home)]
      (is (string? xdg-home))
      (is (or (= xdg-home (System/getenv "XDG_CONFIG_HOME"))
              (.endsWith xdg-home ".config"))))))

(deftest test-get-config-dir
  (testing "get-config-dir returns clojure-skills config directory"
    (let [config-dir (config/get-config-dir)]
      (is (string? config-dir))
      (is (.contains config-dir "clojure-skills")))))

(deftest test-expand-path
  (testing "expand-path expands ~ to home directory"
    (let [home (config/get-home-dir)]
      (is (= (str home "/test")
             (config/expand-path "~/test")))))

  (testing "expand-path leaves absolute paths unchanged"
    (is (= "/absolute/path"
           (config/expand-path "/absolute/path")))))

(deftest test-default-config
  (testing "default-config has required keys"
    (is (map? config/default-config))
    (is (contains? config/default-config :database))
    (is (contains? config/default-config :project))
    (is (contains? config/default-config :search))
    (is (contains? config/default-config :output))))

(deftest test-deep-merge
  (testing "deep-merge merges nested maps"
    (is (= {:a 1 :b {:c 2 :d 3}}
           (config/deep-merge
            {:a 1 :b {:c 2}}
            {:b {:d 3}}))))

  (testing "deep-merge overwrites with later values"
    (is (= {:a 2 :b {:c 3}}
           (config/deep-merge
            {:a 1 :b {:c 2}}
            {:a 2 :b {:c 3}})))))

(deftest test-load-config
  (testing "load-config returns a valid config map"
    (let [cfg (config/load-config)]
      (is (map? cfg))
      (is (contains? cfg :database))
      (is (contains? cfg :project))
      (is (contains? cfg :search))
      (is (contains? cfg :output)))))

(deftest test-get-db-path
  (testing "get-db-path expands path from config"
    (let [cfg {:database {:path "~/test.db"}}
          db-path (config/get-db-path cfg)]
      (is (string? db-path))
      (is (not (.contains db-path "~")))
      (is (.endsWith db-path "test.db")))))
