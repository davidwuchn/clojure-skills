(ns clojure-skills.main
  "Main entry point for clojure-skills CLI."
  (:require [clojure-skills.cli :as cli])
  (:gen-class))

(defn -main
  "Main entry point for the CLI."
  [& args]
  (cli/run-cli args))
