(ns clojure-sqlite-example.core
  (:require [clojure.java.jdbc :refer :all :as jdbc]
            [clojure.string :refer [lower-case]]))

(declare drop-if-exists!)

(defn ^:private create-tables
  [db-spec]
  (try
    ;; Remove existing table data which will fail if we don't have one in the first place!
    ;;(jdbc/db-do-commands db-spec (jdbc/drop-table-ddl :user))
    ;;(jdbc/db-do-commands db-spec (jdbc/drop-table-ddl :story))

    ;; Note: better code from xsc/ritual that don't blindly run the drop like the above code!
    (drop-if-exists! db-spec :user)
    (drop-if-exists! db-spec :story)

    ;; Create user table
    (jdbc/db-do-commands db-spec (jdbc/create-table-ddl :user
                                                        [:id :integer :primary :key]
                                                        [:name "varchar(255)"]))

    ;; Create the story table that references the user table via :author field
    (jdbc/db-do-commands db-spec (jdbc/create-table-ddl :story
                                                        [:id :integer :primary :key]
                                                        [:text :text]
                                                        [:author :integer "references user(id)"]))
    (catch Exception e
      (println e))))

(defn sqlize
  "Convert string/symbol/keyword to string with dashes replaced by underscores."
  [s]
  (when s
    (-> ^String (if (or (symbol? s) (keyword? s))
                  (name s)
                  (str s))
        (.replace "-" "_")
        (lower-case))))

(defn exists?
  "Check whether a given table exists."
  [db-spec table-key]
  (try
    (do
      (->> (format "select 1 from %s" (sqlize table-key))
           (vector)
           (jdbc/query db-spec))
      true)
    (catch Throwable ex
      false)))

(defn drop!
  "Drop a given table."
  [db-spec table-key]
  (->> (jdbc/drop-table-ddl (sqlize table-key))
       (vector)
       (jdbc/execute! db-spec))
  db-spec)

(defn drop-if-exists!
  "Drop table if it exists."
  [db-spec table-key]
  (when (exists? db-spec table-key)
    (drop! db-spec table-key))
  db-spec)

(defn query-for-user
  [db-spec]
  (jdbc/query db-spec "select * from user"))

(defn query-for-story
  [db-spec]
  ;; Skip the :text field for now
  (jdbc/query db-spec "select id, author from story"))

;; Show result
(defn display-result
  [result]
  (doseq [r result]
    (println r)))

(defn populate-user-table
  [db-spec]
  (jdbc/insert! db-spec
                :user
                {:name "freiksenet"}
                {:name "fson"}
                {:name "Hallie"}
                {:name "Sophia"}
                {:name "Riya"}
                {:name "Kari"}
                {:name "Estrid"}
                {:name "Burwenna"}
                {:name "Emma"}
                {:name "Kaia"}
                {:name "Halldora"}
                {:name "Dorte"}))

(defn populate-story-table
  [db-spec]
  (jdbc/insert! db-spec
                :story
                { :text  "One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin.", :author 1}
                {:text  "He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections.", :author 1}
                {:text  "The bedding was hardly able to cover it and seemed ready to slide off any moment. His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked.", :author 1}
                {:text  "\"What''s happened to me? \" he thought. It wasn''t a dream. His room, a proper human room although a little too small, lay peacefully between its four familiar walls.", :author 1}
                {:text  "A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame.", :author 2}
                {:text  "It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer.", :author 3}
                {:text  "Gregor then turned to look out the window at the dull weather. Drops of rain could be heard hitting the pane, which made him feel quite sad.", :author 2}
                {:text  "\"How about if I sleep a little bit longer and forget all this nonsense\", he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state couldn''t get into that position.", :author 4}
                {:text  "However hard he threw himself onto his right, he always rolled back to where he was.", :author 1}
                {:text  "He must have tried it a hundred times, shut his eyes so that he wouldn''t have to look at the floundering legs, and only stopped when he began to feel a mild, dull pain there that he had never felt before.", :author 1}
                {:text  "\"Oh, God\", he thought, \"what a strenuous career it is that I''ve chosen! Travelling day in and day out.", :author 1}
                {:text  "Doing business like this takes much more effort than doing your own business at home, and on top of that there''s the curse of travelling, worries about making train connections, bad and irregular food, contact with different people all the time so that you can never get to know anyone or become friendly with them.", :author 1}
                {:text  "It can all go to Hell!", :author 2}
                {:text  "\" He felt a slight itch up on his belly; pushed himself slowly up on his back towards the headboard so that he could lift his head better; found where the itch was, and saw that it was covered with lots of little white spots which he didn''t know what to make of; and when he tried to feel the place with one of his legs he drew it quickly back because as soon as he touched it he was overcome by a cold shudder.", :author 3}
                {:text  "He slid back into his former position. \"Getting up early all the time\", he thought, \"it makes you stupid. You''ve got to get enough sleep. Other travelling salesmen live a life of luxury.", :author 5}
                {:text  "For instance, whenever I go back to the guest house during the morning to copy out the contract, these gentlemen are always still sitting there eating their breakfasts.", :author 6}
                {:text  "I ought to just try that with my boss; I''d get kicked out on the spot. But who knows, maybe that would be the best thing for me.", :author 7}
                {:text  "If I didn''t have my parents to think about I''d have given in my notice a long time ago, I''d have gone up to the boss and told him just what I think, tell him everything I would, let him know just what I feel." , :author 10}
                {:text  "He''d fall right off his desk!", :author 1}
                {:text  "And it''s a funny sort of business to be sitting up there at your desk, talking down at your subordinates from up there, especially when you have to go right up close because the boss is hard of hearing.", :author 2}
                {:text  "Well, there''s still some hope; once I''ve got the money together to pay off my parents'' debt to him - another five or six years I suppose - that''s definitely what I''ll do. That''s when I''ll make the big change.", :author 3}
                {:text  "First of all though, I''ve got to get up, my train leaves at five. \" And he looked over at the alarm clock, ticking on the chest of drawers. \"God in Heaven! \" he thought.", :author 5}
                {:text  "It was half past six and the hands were quietly moving forwards, it was even later than half past, more like quarter to seven. Had the alarm clock not rung?", :author 2}
                {:text  "He could see from the bed that it had been set for four o''clock as it should have been; it certainly must have rung. Yes, but was it possible to quietly sleep through that furniture-rattling noise?", :author 1}
                {:text  "True, he had not slept peacefully, but probably all the more deeply because of that. What should he do now?", :author 1}
                {:text  "The next train went at seven; if he were to catch that he would have to rush like mad and the collection of samples was still not packed, and he did not at all feel particularly fresh and lively.", :author 1}
                {:text  "And even if he did catch the train he would not avoid his boss''s anger as the office assistant would have been there to see the five o''clock train go, he would have put in his report about Gregor''s not being there a long time ago.", :author 1}
                {:text  "The office assistant was the boss''s man, spineless, and with no understanding. What about if he reported sick? But that would be extremely strained and suspicious as in fifteen years of service Gregor had never once yet been ill.", :author 1}
                {:text  "His boss would certainly come round with the doctor from the medical insurance company, accuse his parents of having a lazy son, and accept the doctor''s recommendation not to make any claim as the doctor believed that no-one was ever ill but that many were workshy.", :author 2}
                {:text  "And what''s more, would he have been entirely wrong in this case? Gregor did in fact, apart from excessive sleepiness after sleeping for so long, feel completely well and even felt much hungrier than usual.", :author 3}
                {:text  "One morning, when Gregor Samsa woke from troubled dreams, he found himself transformed in his bed into a horrible vermin.", :author 1}
                {:text  "He lay on his armour-like back, and if he lifted his head a little he could see his brown belly, slightly domed and divided by arches into stiff sections. The bedding was hardly able to cover it and seemed ready to slide off any moment.", :author 1}
                {:text  "His many legs, pitifully thin compared with the size of the rest of him, waved about helplessly as he looked. \"What''s happened to me? \" he thought. It wasn''t a dream.", :author 1}
                {:text  "His room, a proper human room although a little too small, lay peacefully between its four familiar walls.", :author 1}
                {:text  "A collection of textile samples lay spread out on the table - Samsa was a travelling salesman - and above it there hung a picture that he had recently cut out of an illustrated magazine and housed in a nice, gilded frame.", :author 5}
                {:text  "It showed a lady fitted out with a fur hat and fur boa who sat upright, raising a heavy fur muff that covered the whole of her lower arm towards the viewer. Gregor then turned to look out the window at the dull weather.", :author 5}
                {:text  "Drops of rain could be heard hitting the pane, which made him feel quite sad.", :author 5}
                {:text  "\"How about if I sleep a little bit longer and forget all this nonsense\", he thought, but that was something he was unable to do because he was used to sleeping on his right, and in his present state couldn''t get into that position.", :author 3}
                {:text  "However hard he threw himself onto his right, he always rolled back to where he was. He must have tried it a hundred times, shut his eyes so that he wouldn''t have to look at the floundering legs, and only stopped when he began to feel a mild, dull pain there that he had never felt before.", :author 1}
                {:text  "\"Oh, God\", he thought, \"what a strenuous career it is that I''ve chosen! Travelling day in and day out.", :author 2}
                {:text  "Doing business like this takes much more effort than doing your own business at home, and on top of that there''s the curse of travelling, worries about making train connections, bad and irregular food, contact with different people all the time so that you can never get to know anyone or become friendly with them. It can all go to Hell!", :author 3}
                {:text  "\" He felt a slight itch up on his belly; pushed himself slowly up on his back towards the headboard so that he could lift his head better; found where the itch was, and saw that it was covered with lots of little white spots which he didn''t know what to make of; and when he tried to feel the place with one of his legs he drew it quickly back because as soon as he touched it he was overcome by a cold shudder.", :author 3}
                {:text  "He slid back into his former position. \"Getting up early all the time\", he thought, \"it makes you stupid. You''ve got to get enough sleep. Other travelling salesmen live a life of luxury.", :author 3}
                {:text  "For instance, whenever I go back to the guest house during the morning to copy out the contract, these gentlemen are always still sitting there eating their breakfasts. I ought to just try that with my boss; I''d get kicked out on the spot. But who knows, maybe that would be the best thing for me.", :author 6}
                {:text  "If I didn''t have my parents to think about I''d have given in my notice a long time ago, I''d have gone up to the boss and told him just what I think, tell him everything I would, let him know just what I feel. He''d fall right off his desk!", :author 6}
                {:text  "And it''s a funny sort of business to be sitting up there at your desk, talking down at your subordinates from up there, especially when you have to go right up close because the boss is hard of hearing. Well, there''s still some hope; once I''ve got the money together to pay off my parents'' debt to him - another five or six years I suppose - that''s definitely what I''ll do.", :author 6}
                {:text  "That''s when I''ll make the big change. First of all though, I''ve got to get up, my train leaves at five. \" And he looked over at the alarm clock, ticking on the chest of drawers. \"God in Heaven! \" he thought. It was half past six and the hands were quietly moving forwards, it was even later than half past, more like quarter to seven.", :author 2}
                {:text  "Had the alarm clock not rung? He could see from the bed that it had been set for four o''clock as it should have been; it certainly must have rung. Yes, but was it possible to quietly sleep through that furniture-rattling noise? True, he had not slept peacefully, but probably all the more deeply because of that. What should he do now?", :author 1}
                {:text  "The next train went at seven; if he were to catch that he would have to rush like mad and the collection of samples was still not packed, and he did not at all feel particularly fresh and lively. And even if he did catch the train he would not avoid his boss''s anger as the office assistant would have been there to see the five o''clock train go, he would have put in his report about Gregor''s not being there a long time ago.", :author 1}
                {:text  "The office assistant was the boss''s man, spineless, and with no understanding. What about if he reported sick? But that would be extremely strained and suspicious as in fifteen years of service Gregor had never once yet been ill. His boss would certainly come round with the doctor from the medical insurance company, accuse his parents of having a lazy son, and accept the doctor''s recommendation not to make any claim as the doctor believed that " , :author 1}))

(defn -main [& args]
  (try
    (let [db-spec {:classname   "org.sqlite.JDBC"
                   :subprotocol "sqlite"
                   :subname     "db/db.sqlite3"}]

      (create-tables db-spec)

      (populate-user-table db-spec)
      (populate-story-table db-spec)

      (println "FYI: query for user")
      (display-result (query-for-user db-spec))

      (println "FYI: query for story")
      (display-result (query-for-story db-spec)))
    (catch Exception e
      (.printStackTrace e)
      (println (str "Unexpected errors: " (.getMessage e))))))
