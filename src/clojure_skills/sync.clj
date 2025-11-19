(ns clojure-skills.sync
  "Sync markdown files to SQLite database."
  (:require
   [clj-yaml.core :as yaml]
   [clojure-skills.config :as config]
   [clojure-skills.db.core]
   [clojure-skills.db.prompt-fragments :as fragments]
   [clojure-skills.logging :as log]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql])
  (:import
   (java.io
    File)
   (java.security
    MessageDigest)))

(set! *warn-on-reflection* true)

(defn compute-hash
  "Compute SHA-256 hash of file content."
  ^String [^String content]
  (when content
    (let [^MessageDigest digest (MessageDigest/getInstance "SHA-256")
          ^bytes bytes (.digest digest (.getBytes content "UTF-8"))]
      (apply str (map #(format "%02x" %) bytes)))))

(defn extract-frontmatter
  "Extract YAML frontmatter from markdown content.
   Returns [frontmatter content-without-frontmatter]."
  [content]
  (let [lines (str/split-lines content)]
    (if (and (seq lines)
             (= "---" (first lines)))
      (let [end-idx (some #(when (= "---" (nth lines %)) %)
                          (range 1 (count lines)))]
        (if end-idx
          (let [frontmatter-lines (subvec (vec lines) 1 end-idx)
                frontmatter-text (str/join "\n" frontmatter-lines)
                content-lines (subvec (vec lines) (inc end-idx))
                content-text (str/join "\n" content-lines)]
            (try
              [(yaml/parse-string frontmatter-text) content-text]
              (catch Exception e
                (log/log-warning "Failed to parse frontmatter" :error (.getMessage e))
                [nil content])))
          [nil content]))
      [nil content])))

(defn estimate-tokens
  "Estimate token count (roughly 4 characters per token)."
  [text]
  (int (/ (count text) 4)))

(defn parse-skill-path
  "Parse skill file path to extract category and name.
   Example: skills/language/clojure_intro.md -> {:category language :name clojure_intro}"
  [path]
  (let [parts (str/split path #"/")
        filename (last parts)
        name (str/replace filename #"\.md$" "")
        ;; Find index of "skills" directory
        skills-idx (or (some #(when (= "skills" (nth parts %)) %)
                             (range (count parts)))
                       -1)
        ;; Extract parts after "skills" but before filename
        category-parts (if (> skills-idx -1)
                         (subvec (vec parts) (inc skills-idx) (dec (count parts)))
                         [])
        category (if (seq category-parts)
                   (str/join "/" category-parts)
                   "uncategorized")]
    {:category category
     :name name}))

(defn scan-skill-files
  "Scan skills directory and return list of skill file paths."
  [skills-dir]
  (let [dir (io/file skills-dir)]
    (when (.exists dir)
      (->> (file-seq dir)
           (filter #(and (.isFile ^File %)
                         (str/ends-with? (.getName ^File %) ".md")))
           (map #(str (.getPath ^File %)))
           (sort)))))

(defn scan-prompt-files
  "Scan prompts directory and return list of prompt file paths."
  [prompts-dir]
  (let [dir (io/file prompts-dir)]
    (when (.exists dir)
      (->> (file-seq dir)
           (filter #(and (.isFile ^File %)
                         (str/ends-with? (.getName ^File %) ".md")))
           (map #(str (.getPath ^File %)))
           (sort)))))

(defn scan-prompt-config-files
  "Scan prompt_configs directory and return list of YAML config file paths."
  [configs-dir]
  (let [dir (io/file configs-dir)]
    (when (.exists dir)
      (->> (file-seq dir)
           (filter #(and (.isFile ^File %)
                         (str/ends-with? (.getName ^File %) ".yaml")))
           (map #(str (.getPath ^File %)))
           (sort)))))

(defn parse-skill-file
  "Parse a skill markdown file and extract metadata."
  [path]
  (let [content (slurp path)
        [frontmatter _content-without-frontmatter] (extract-frontmatter content)
        {:keys [category name]} (parse-skill-path path)
        file-hash (compute-hash content)
        size-bytes (.length (io/file path))
        token-count (estimate-tokens content)]
    {:path path
     :category category
     :name name
     :title (get frontmatter "title")
     :description (get frontmatter "description")
     :content content
     :file_hash file-hash ;; Using snake_case for SQL compatibility
     :size_bytes size-bytes
     :token_count token-count}))

(defn parse-prompt-config-file
  "Parse a prompt YAML configuration file and extract skill associations."
  [path]
  (let [content (slurp path)
        config (yaml/parse-string content)
        filename (last (str/split path #"/"))
        file-name (str/replace filename #"\.yaml$" "")]
    {:path path
     :file-name file-name
     :name (get config :name file-name)  ;; Use name from config, fallback to filename
     :title (get config :title)
     :description (get config :description)
     :author (get config :author)
     :date (get config :date)
     :skills (get config :skills [])
     :content content}))

(defn get-skill-by-path
  "Get skill from database by path."
  [db path]
  (jdbc/execute-one! db ["SELECT * FROM skills WHERE path = ?" path]))

(defn get-prompt-by-name
  "Get prompt from database by name."
  [db name]
  (jdbc/execute-one! db ["SELECT * FROM prompts WHERE name = ?" name]))

(defn upsert-skill
  "Insert or update skill in database."
  [db skill]
  (let [existing (get-skill-by-path db (:path skill))]
    (if existing
      ;; Update
      (sql/update! db :skills
                   (dissoc skill :path)
                   {:path (:path skill)})
      ;; Insert
      (sql/insert! db :skills skill))))

(defn upsert-prompt
  "Insert or update prompt in database.
   Uses name (not path) as the unique identifier due to UNIQUE constraint."
  [db prompt]
  (let [existing (get-prompt-by-name db (:name prompt))]
    (if existing
      ;; Update existing prompt (matched by name)
      (sql/update! db :prompts
                   (dissoc prompt :name :sections)
                   {:name (:name prompt)})
      ;; Insert new prompt
      (sql/insert! db :prompts (dissoc prompt :sections)))))

(defn sync-skill
  "Sync a single skill file to database."
  [db skill-path]
  (try
    (let [skill-data (parse-skill-file skill-path)
          existing (get-skill-by-path db skill-path)]
      (if (and existing
               (= (:skills/file_hash existing) (:file_hash skill-data)))
        (do
          (log/log-info "Skipped skill sync (unchanged)" :path skill-path)
          (println "  Skipped (unchanged):" skill-path))
        (do
          (upsert-skill db skill-data)
          (log/log-info "Synced skill" :path skill-path)
          (println "  Synced:" skill-path))))
    (catch Exception e
      (log/log-error "Error syncing skill" :path skill-path :error (.getMessage e))
      (println "  ERROR syncing" skill-path ":" (.getMessage e)))))

(defn find-prompt-content-file
  "Find the corresponding .md file for a prompt name."
  [prompt-name]
  (let [project-root (System/getProperty "user.dir")]
    (str project-root "/prompts/" prompt-name ".md")))

(defn sync-prompt-from-config
  "Sync a prompt using config file as metadata source.
   This is the NEW approach where metadata comes from .yaml and content from .md"
  [db config-path]
  (try
    (let [config-data (parse-prompt-config-file config-path)
          prompt-name (:name config-data)
          content-path (find-prompt-content-file prompt-name)
          content (slurp content-path)
          ;; Hash both config and content to detect changes in either
          combined-content (str (:content config-data) "\n---\n" content)
          file-hash (compute-hash combined-content)
          size-bytes (+ (.length (io/file config-path))
                        (.length (io/file content-path)))
          token-count (estimate-tokens content)
          prompt-data {:path content-path  ;; Store path to .md file for compatibility
                       :name prompt-name
                       :title (:title config-data)
                       :author (:author config-data)
                       :description (:description config-data)
                       :content content
                       :file_hash file-hash
                       :size_bytes size-bytes
                       :token_count token-count}
          existing (get-prompt-by-name db prompt-name)]
      (if (and existing
               (= (:prompts/file_hash existing) file-hash))
        (do
          (log/log-info "Skipped prompt sync (unchanged)" :name prompt-name)
          (println "  Skipped (unchanged):" prompt-name))
        (do
          (upsert-prompt db prompt-data)
          (log/log-info "Synced prompt from config" :name prompt-name)
          (println "  Synced:" prompt-name))))
    (catch Exception e
      (log/log-error "Error syncing prompt from config" :path config-path :error (.getMessage e))
      (println "  ERROR syncing" config-path ":" (.getMessage e)))))

(defn sync-all-skills
  "Sync all skills from skills directory to database."
  [db config]
  (let [project-root (config/expand-path (or (get-in config [:project :root])
                                             (System/getProperty "user.dir")))
        skills-dir (str project-root "/" (get-in config [:project :skills-dir]))
        skill-files (scan-skill-files skills-dir)]
    (log/log-info "Starting skills sync" :count (count skill-files) :directory skills-dir)
    (println (format "Syncing %d skills from %s..." (count skill-files) skills-dir))
    (doseq [skill-file skill-files]
      (sync-skill db skill-file))
    (log/log-success "Skills sync complete" :count (count skill-files))
    (println "Skills sync complete.")))

(defn sync-all-prompts
  "Sync all prompts from prompt_configs directory to database.
   NEW APPROACH: Reads metadata from .yaml configs and content from .md files."
  [db config]
  (let [project-root (config/expand-path (or (get-in config [:project :root])
                                             (System/getProperty "user.dir")))
        configs-dir (str project-root "/prompt_configs")
        config-files (scan-prompt-config-files configs-dir)]
    (log/log-info "Starting prompts sync from configs" :count (count config-files) :directory configs-dir)
    (println (format "Syncing %d prompts from %s..." (count config-files) configs-dir))
    (doseq [config-file config-files]
      (sync-prompt-from-config db config-file))
    (log/log-success "Prompts sync complete" :count (count config-files))
    (println "Prompts sync complete.")))

(defn resolve-skill-path
  "Resolve a relative skill path from YAML config to absolute path."
  [relative-path]
  (let [project-root (System/getProperty "user.dir")]
    (str project-root "/" relative-path)))

(defn get-skill-by-absolute-path
  "Get skill from database by absolute path."
  [db absolute-path]
  (jdbc/execute-one! db ["SELECT * FROM skills WHERE path = ?" absolute-path]))

(defn sync-prompt-skills-for-config
  "Sync prompt skills from a YAML configuration file to database using fragments model."
  [db config-path]
  (try
    (let [config-data (parse-prompt-config-file config-path)
          prompt-name (:name config-data)
          skill-paths (:skills config-data)
          prompt-record (get-prompt-by-name db prompt-name)]
      (if prompt-record
        ;; Create or get fragment for this prompt
        (let [fragment-name (str prompt-name "-embedded")
              fragment-title (str (or (:title config-data) prompt-name) " Embedded Skills")
              existing-fragment (fragments/get-prompt-fragment-by-name db fragment-name)
              fragment (if existing-fragment
                         existing-fragment
                         (fragments/create-prompt-fragment
                          db
                          {:name fragment-name
                           :title fragment-title
                           :description (str "Embedded skills for " prompt-name " prompt")}))]

          ;; Clear existing fragment-skill associations
          (jdbc/execute! db ["DELETE FROM prompt_fragment_skills WHERE fragment_id = ?"
                             (:prompt_fragments/id fragment)])

          ;; Add skills to fragment
          (doseq [[idx skill-path] (map vector (range) skill-paths)]
            (let [absolute-path (resolve-skill-path skill-path)
                  skill-record (get-skill-by-absolute-path db absolute-path)]
              (if skill-record
                (do
                  (fragments/associate-skill-with-fragment
                   db
                   {:fragment_id (:prompt_fragments/id fragment)
                    :skill_id (:skills/id skill-record)
                    :position idx})
                  (println (format "  Associated skill: %s -> %s (position %d)"
                                   prompt-name (:skills/name skill-record) idx)))
                (do
                  (log/log-warning "Skill not found in database" :path skill-path :prompt prompt-name)
                  (println (format "  WARNING: Skill not found: %s" skill-path))))))

          ;; Clear existing prompt references
          (jdbc/execute! db ["DELETE FROM prompt_references WHERE source_prompt_id = ? AND reference_type = 'fragment'"
                             (:prompts/id prompt-record)])

          ;; Add prompt reference to fragment
          (fragments/add-prompt-reference
           db
           {:source_prompt_id (:prompts/id prompt-record)
            :target_fragment_id (:prompt_fragments/id fragment)
            :reference_type "fragment"
            :position 1})

          (log/log-info "Synced prompt fragment" :prompt prompt-name :skills-count (count skill-paths))
          (println (format "  Synced fragment for prompt: %s (%d skills)" prompt-name (count skill-paths))))
        (do
          (log/log-warning "Prompt not found in database" :name prompt-name :config config-path)
          (println (format "  WARNING: Prompt '%s' not found in database" prompt-name)))))
    (catch Exception e
      (log/log-error "Error syncing prompt fragment" :path config-path :error (.getMessage e))
      (println (format "  ERROR syncing prompt fragment from %s: %s" config-path (.getMessage e))))))

(defn sync-all-prompt-skills
  "Sync all prompt fragments from prompt_configs directory to database."
  [db config]
  (let [project-root (config/expand-path (or (get-in config [:project :root])
                                             (System/getProperty "user.dir")))
        configs-dir (str project-root "/prompt_configs")
        config-files (scan-prompt-config-files configs-dir)]
    (log/log-info "Starting prompt fragments sync" :count (count config-files) :directory configs-dir)
    (println (format "Syncing prompt fragments from %d config files in %s..." (count config-files) configs-dir))
    (when (seq config-files)
      (doseq [config-file config-files]
        (sync-prompt-skills-for-config db config-file)))
    (log/log-success "Prompt fragments sync complete" :count (count config-files))
    (println "Prompt fragments sync complete.")))

(defn sync-all
  "Sync all skills, prompts, and prompt skills to database."
  ([db config]
   (sync-all-skills db config)
   (sync-all-prompts db config)
   (sync-all-prompt-skills db config))
  ([]
   (let [config (config/load-config)
         db (clojure-skills.db.core/get-db config)]
     (sync-all db config))))
