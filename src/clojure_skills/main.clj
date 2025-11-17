(ns clojure-skills.main
  "Main entry point for clojure-skills CLI."
  (:require [clojure-skills.cli :as cli]
            [clojure-skills.logging :as log])
  (:gen-class))

(defn -main
  "Main entry point for the CLI."
  [& args]
  (log/start-logging!)
  (log/set-global-context!)
  (log/log-info "Starting clojure-skills CLI")
  (try
    (cli/run-cli args)
    (finally
      (log/log-info "Stopping clojure-skills CLI")
      (log/stop-logging!))))
