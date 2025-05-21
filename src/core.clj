(ns core
  (:gen-class)
  (:require
   [routes :as routes]
   [clojure.string        :as string]
   [clojure.core.async    :as async]
   [discljord.connections :as c]
   [discljord.messaging   :as m]))

(def token
  "Discord bot token, used as ID and 'security measure' in one. Use your own!"
  (slurp "./data/discord_bot.key"))

(def intents #{:guilds :guild-messages})

(defn route-msg
  "Decides on what output message to generate based on the routes (condition->result) and the input message.
  Routes will be handled from front to back."
  [routes msg event]
  (let [route (first routes)]
    (when (= 0 (count routes)) (prn "No match!"))
    (if ((:condition route) msg event)
      ((:result route) msg event)
      (recur (rest routes) msg event))))

(def router
  "Message router, partially applied with all routes."
  (partial route-msg
           ;; Order matters here, as the router will go through this front to back.
           [routes/empty-mdn
            routes/empty-ns
            routes/too-long
            routes/robot
            routes/poast-coad
            routes/pspsps
            routes/zoe
            routes/persecution
            routes/duck
            routes/naughty
            routes/lookup]))

(defn- event-enricher
  "Turns an event into a map with all relevant data."
  [event message-ch]
  (let [event (assoc event :type (if (string/starts-with? (event :content) "!ns") :ns :mdn))
        msg (-> event :content (string/replace #"(?i)^!(MDN|NS)\b" "") string/lower-case string/trim)
        ;; Pass the event to the router
        reply (router msg event)]
    (m/create-message! message-ch (:channel-id event) :content reply)))

(defn -main
  "Start the server.
   Note to Zoë and other REPLers, use: `(doto (Thread. -main) (.setDaemon true) (.start))`."
  []
  (letfn [(check-prefix [data] (re-find #"(?i)^!(MDN|NS)\b" (get data :content "")))]
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
             ;(prn data)
             (if ok? (do (event-enricher data message-ch) (inc n)) n))))

        (finally
          (m/stop-connection! message-ch)
          (async/close!           event-ch))))))
