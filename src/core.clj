(ns core
  (:gen-class)
  (:require
   [replies               :as r]
   [routes :as routes]
   [spoiler-channels      :as i]
   [clj-time       [core :as time]]
   [clojure.string        :as string]
   [clojure.core.async    :as async]
   [discljord.connections :as c]
   [discljord.messaging   :as m]))

(def token
  "Discord bot token, used as ID and 'security measure' in one. Use your own!"
  (slurp "./data/discord_bot.key"))

(def intents #{:guilds :guild-messages})

(defn levenshtein
  "Fuzzy matcher based on levenshtein algorithm."
  [{w1 :sname} w2]
  (letfn [(cell-value [same-char? prev-row cur-row col-idx]
            (min (inc (nth prev-row col-idx))
                 (inc (last cur-row))
                 (+ (nth prev-row (dec col-idx)) (if same-char? 0 1))))]
    (loop [row-idx  1
           max-rows (inc (count w2))
           prev-row (range (inc (count w1)))]
      (if (= row-idx max-rows)
        (last prev-row)
        (let [ch2           (nth w2 (dec row-idx))
              next-prev-row (reduce
                             (fn [cur-row i] (conj cur-row (cell-value (= (nth w1 (dec i)) ch2) prev-row cur-row i)))
                             [row-idx]
                             (range 1 (count prev-row)))]
          (recur (inc row-idx) max-rows next-prev-row))))))

(defn- bad_request
  "Returns a message trying to fuzzy match the input."
  [spoil-ok? request replies]
  (->> [(str "\"" request "\" not found")
        "The machine spirit wonders if you meant..."
        (->> replies
             vals
             (map #(when (or spoil-ok? (not (% :spoiler?))) (assoc % :score (levenshtein % request))))
             (sort-by #(get % :score ##Inf))
             (take 5)
             (map #(str "- " (% :name) ": <" (% :url) ">")))
        "...? Bye!"]
       flatten
       (string/join "\n")))

(defn route-msg
  [routes msg event]
  (let [route (var-get (first routes))]
    (prn route)
    (if (= 0 (count routes)) (prn "No match!"))
    (if ((:condition route) msg event)
      ((:result route) msg event)
      (recur (rest routes) msg event))))

(def auto-router
  "Automatically hooks up any route in the `routes` namespace to the router"
  (partial route-msg
           (vals (ns-publics 'routes))))


(defn- event-enricher
  "Turns an event into a map with all relevant data."
  [event message-ch n]
  ;; Ensure bogus requests are ignored early.
  (if (< 70 (count (:content event))) "Nuh uh, that request is too long.")

  (let [msg (-> event :content (string/replace #"(?i)^!(MDN|NS)\b" "") r/lcase-&-rm-ns)
        ;; Pass the event to the router
        reply (auto-router msg event)]
    (m/create-message! message-ch (:channel-id event) :content reply)))

(defn -main
  "Start the server.
   Note to Zoë and other REPLers, use: `(doto (Thread. -main) (.setDaemon true) (.start))`."
  []
  (letfn [(check-prefix [data] (re-find #"(?i)^!(MDN|NS)\b" (get data :content "")))
          ]
    (let [event-ch     (async/chan 100)
          _conn_ch     (c/connect-bot! token event-ch :intents intents)
          message-ch   (m/start-connection! token)]
      (try
        (loop [n 0]
          (recur
           (let [[type data] (async/<!! event-ch)
                 msg?        (= :message-create type)
                 notbot?     (-> data :author :bot not)
                 for-me?     (check-prefix data)
                 ok?         (and msg? notbot? for-me?)]
             (prn data)
             (if ok? (do (event-enricher data message-ch n) (inc n)) n))))

        (finally
          (m/stop-connection! message-ch)
          (async/close!           event-ch))))))
