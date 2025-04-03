(ns core
  (:require
   [replies :as r]
   [clojure.string :as s]
   [clojure.core.async    :as a]
   [discljord.connections :as c]
   [discljord.messaging   :as m]))

(def token    (slurp "./DISCORD_KEY_DO_NOT_PUSH"))
(def intents #{:guilds :guild-messages})

(defn- count_similar [reply [acc last] c] (let [n (if (s/includes? reply (str c)) 1 -2)] [(+ acc n last) n]))

(defn- score [request {spoil? :spoil? str :str} spoil-ok?]
  (if (and (not spoil-ok?) spoil?)
    (* -1 ##Inf)
    (first (reduce (partial count_similar str) [0 0] (seq request)))))

(defn by-score [request spoil-ok?]
  (fn [ra rb] (let [ca (score request rb spoil-ok?)
                    cb (score request ra spoil-ok?)]
                (compare ca cb))))

(defn- str-cands [options spoil-ok?]
  (map (fn [{spoil? :spoil? option :str}]
         (str "- "
              (when (and spoil?  spoil-ok?) "||")
              option ": <" (:url (r/replies (keyword (s/lower-case option)))) ">"
              (when (and spoil?  spoil-ok?) "||")
              "\n"))
       options))

(defn- bad_request [request spoil-ok?]
  (let [sorted (sort
                (by-score request spoil-ok?)
                (vals r/replies))
        candidates (take 5 sorted)]
    (str
     "\"" request "\""
     " not found \n\nThe machine spirit wonders if you meant...\n"
     (apply str (str-cands candidates spoil-ok?)) "?\n"
     "Bye!")))

(let [event-ch      (a/chan 100)
      _connection-ch (c/connect-bot! token event-ch :intents intents)
      message-ch    (m/start-connection! token)]
  (try
    (loop []
      (let [[event-type event-data] (a/<!! event-ch)
            msg? (= :message-create event-type)
            {{bot? :bot} :author} event-data]
        (when (and msg? (not bot?))
          (let [{content :content channel-id :channel-id} event-data]
            (when  (s/starts-with?  (s/trim content) "!NS ")
              (let [spoil-ok? (or (s/ends-with? content " -s") (s/includes? content " -s "))
                    request (s/lower-case (s/replace (s/replace content #"\W-s\W?" "") #"!NS " ""))
                    {spoil? :spoil? url :url} ((keyword request) r/replies)]
                (->>
                 (str
                  "I am summoned from mine digital slumber once more \n\n"
                  (cond
                    (s/includes? content "duck") "quack 🦆"
                    (= request "bleep bloop") "bloop bleep"
                    (= request "bloop bleep") "bleep bloop"
                    (and (not spoil-ok?) spoil?) "Doesn't look like anything to me."
                    (nil? url) (bad_request request spoil-ok?)
                    :else (str (when spoil? "||") "<" url ">" (when spoil? "||"))))
                 (m/create-message! message-ch channel-id :content))))))
        (recur)))
    (finally
      (m/stop-connection! message-ch)
      (a/close!           event-ch))))
