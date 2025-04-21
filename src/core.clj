(ns core
  (:gen-class)
  (:require
   [replies               :as r]
   [spoiler-channels      :as i]
   [clj-time       [core :as t]]
   [clojure.string        :as s]
   [clojure.core.async    :as a]
   [discljord.connections :as c]
   [discljord.messaging   :as m]))

(def token (slurp "./DISCORD_KEY_DO_NOT_PUSH"))
(def intents #{:guilds :guild-messages})

(defn levenshtein [{w1 :sname} w2]
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

(defn- bad_request [spoil-ok? request replies]
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
       (s/join "\n")))

(defn- hacknet-persecution-detector [content]
  (or
   (seq (re-matches #".*(do( not|n'?t|nut|ughnut) buy hacknet).*" content))
   (seq (re-matches #".*(hacknet (is( not|n'?t) worth it|sucks|is a bad investment)).*" content))))

(defn- do-ns-command [event-data message-ch n replies]
  (prn (-> replies))
  (let [start              (t/now)
        content            (-> event-data :content)
        channel-id         (-> event-data :channel-id)
        spoil-ok?          (some #(= % channel-id) i/spoiler-channels)
        request            (-> content (s/replace #"<@\d+>( |)" "") (s/replace #"(?i)^!NS\b" "") r/lcase-&-rm-ns)
        match              ((keyword request) replies)

        robot?             #(or (= request "bleep bloop") (= request "bloop bleep"))
        too-long?          #(< 65 (count request))
        persecution?       #(hacknet-persecution-detector request)
        naughty?           #(and (not spoil-ok?) (some (fn [r] (s/includes? request r)) r/spoilers))
        duck?              #(or (s/includes? request "quack") (s/includes? request "duck"))

        reply
        (cond
          (empty? request) (str "NS - " "<" (-> replies :ns :url) ">" (r/sig start n))
          (too-long?)      "nuh uh, that request is too long."
          (robot?)         (s/join " " (-> request (s/split #"\s") reverse))
          (persecution?)   (r/measured-hacknet-response start n)
          (duck?)          r/duk
          (naughty?)       r/tell-off
          (some? match)    (str (match :name) " - " "<" (match :url) ">" (r/sig start n))
          :else            (str (bad_request spoil-ok? request replies) (r/sig start n)))]
    (m/create-message! message-ch channel-id :content reply)))

(defn- do-mdn-command [event-data message-ch n replies]
  (prn replies)
  (let [start              (t/now)
        content            (-> event-data :content)
        channel-id         (-> event-data :channel-id)
        request            (-> content (s/replace #"<@\d+>( |)" "") (s/replace #"(?i)^!MDN\b" "") r/lcase-&-rm-ns)
        match              ((keyword request) replies)

        robot?             #(or (= request "bleep bloop") (= request "bloop bleep"))
        too-long?          #(< 65 (count request))
        duck?              #(or (s/includes? request "quack") (s/includes? request "duck"))

        reply
        (cond
          (empty? request) (str "MDN - " "<" (-> replies :mdn :url) ">" (r/sig start n))
          (too-long?)      "nuh uh, that request is too long."
          (robot?)         (s/join " " (-> request (s/split #"\s") reverse))
          (duck?)          r/duk
          (some? match)    (str (match :name) " - " "<" (match :url) ">" (r/sig start n))
          :else            (str (bad_request true request replies) (r/sig start n)))]
    (m/create-message! message-ch channel-id :content reply)))

(defn get-cmd-type [content]
  (get (re-find #"^!(.*?)(?:\s|$).*$" content) 1))

(defn- crunch-msg [event-data message-ch n]
  (let [content (-> event-data :content)
        cmd-type (get-cmd-type content)]
    (prn cmd-type)
    (case cmd-type
      "ns" (do-ns-command event-data message-ch n (r/replies :ns))
      "mdn" (do-mdn-command event-data message-ch n (r/replies :mdn)))))

(defn -main []
  (letfn [(at?  [data] (some #(= (% :username) "ns.pest()") (get data :mentions)))
          (!ns? [data] (re-find #"(?i)^!NS\b" (get data :content "")))
          (!mdn? [data] (re-find #"(?i)^!MDN\b" (get data :content "")))]
    (let [event-ch     (a/chan 100)
          _conn_ch     (c/connect-bot! token event-ch :intents intents)
          message-ch   (m/start-connection! token)]
      (try
        (loop [n 0]
          (recur
           (let [[type data] (a/<!! event-ch)
                 msg?        (= :message-create type)
                 notbot?     (-> data :author :bot not)
                 for-me?     (or (at? data) (!ns? data) (!mdn? data))
                 ok?         (and msg? notbot? for-me?)]
             (if ok? (do (crunch-msg data message-ch n) (inc n)) n))))

        (finally
          (m/stop-connection! message-ch)
          (a/close!           event-ch))))))

