(ns dev
  (:require
   [clj-reload.core :as reload]
   [kaocha.repl :as k]))


(defn refresh
  []
  (reload/reload))


(comment

  (k/run-all)
  (k/run 'test-namespace)
  (k/run 'test-namespace/deftest-var))
