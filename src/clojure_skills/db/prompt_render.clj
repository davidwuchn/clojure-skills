(ns clojure-skills.db.prompt-render
  "Database functions for prompt rendering operations."
  (:require
   [clojure.string :as str]
   [next.jdbc :as jdbc]))

(defn get-prompt-with-fragments
  "Get a prompt with all its fragment references.
   
   Returns a prompt map with :fragment-references key containing
   ordered list of fragments with their skills.
   
   Args:
     db - Database connection
     prompt-name - Name of the prompt to fetch
   
   Returns:
     Map with prompt data and fragment references, or nil if not found"
  [db prompt-name]
  (let [prompt (jdbc/execute-one!
                db
                ["SELECT * FROM prompts WHERE name = ?" prompt-name])]
    (when prompt
      (let [fragments (jdbc/execute!
                       db
                       ["SELECT pr.*, pf.name as fragment_name, pf.title as fragment_title
                         FROM prompt_references pr
                         JOIN prompt_fragments pf ON pr.target_fragment_id = pf.id
                         WHERE pr.source_prompt_id = ? AND pr.reference_type = 'fragment'
                         ORDER BY pr.position"
                        (:prompts/id prompt)])]
        (assoc prompt :fragment-references fragments)))))

(defn get-skill-details
  "Get full details of a skill by name.
   
   Args:
     db - Database connection
     skill-name - Name of the skill to fetch
   
   Returns:
     Skill map with all fields, or nil if not found"
  [db skill-name]
  (jdbc/execute-one!
   db
   ["SELECT * FROM skills WHERE name = ?" skill-name]))

(defn list-all-skill-names
  "Get a list of all skill names in the database.
   
   Returns:
     Sequence of skill name strings"
  [db]
  (->> (jdbc/execute!
        db
        ["SELECT name FROM skills ORDER BY name"])
       (map :skills/name)))

(defn get-prompt-fragment-skills
  "Get all skills for a prompt's fragments, ordered by position.
   
   Args:
     db - Database connection
     prompt-id - ID of the prompt
   
   Returns:
     Sequence of skill maps ordered by position"
  [db prompt-id]
  (jdbc/execute!
   db
   ["SELECT pfs.position, s.* 
     FROM prompt_references pr
     JOIN prompt_fragments pf ON pr.target_fragment_id = pf.id
     JOIN prompt_fragment_skills pfs ON pf.id = pfs.fragment_id
     JOIN skills s ON pfs.skill_id = s.id
     WHERE pr.source_prompt_id = ? 
       AND pr.reference_type = 'fragment'
     ORDER BY pfs.position"
    prompt-id]))

(defn render-prompt-as-plain-markdown
  "Render a prompt as plain markdown without extra sections.
   
   Args:
     db - Database connection
     prompt - Prompt map with :prompts/id and :prompts/content
   
   Returns:
     String with composed markdown content containing only the prompt intro and embedded skills"
  [db prompt]
  (let [skills (get-prompt-fragment-skills db (:prompts/id prompt))]
    (str/join "\n\n"
              (filter some?
                      [;; 1. Prompt introduction
                       (:prompts/content prompt)

                       ;; 2. Embedded skills content (without section header)
                       (when (seq skills)
                         (str/join "\n\n"
                                   (map :skills/content skills)))]))))
