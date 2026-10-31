(ns core
  (:gen-class)
  (:require
   [routes :as routes]
   [clojure.string        :as string]
   [clojure.core.async    :as async]
   [clojure.java.io :as io]
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
            routes/code-format
            routes/strike-format
            routes/spoiler-format
            routes/long-code
            routes/static-ram-parsing
            routes/endgame-welcome
            routes/check-pins
            routes/robot
            routes/poast-coad
            routes/pspsps
            routes/conglats
            routes/spam
            routes/yuri
            routes/zoe
            routes/persecution
            routes/duck
            routes/too-long
            routes/naughty
            routes/lookup]))

(defn- create-filesvector
  "Takes a vector of filenames and turns it into a vector of java.io.File objects."
  [filenames]
  (mapv io/file filenames))

(defn- event-enricher
  "Turns an event into a map with all relevant data."
  [event message-ch]
  (let [event (assoc event :type (if (string/starts-with? (event :content) "!ns") :ns :mdn))
        msg (-> event :content (string/replace #"(?i)^!(MDN|NS)\b" "") string/lower-case string/trim)
        ;; Pass the event to the router
        reply (router msg event)]
        ;reply is a map with :content and :files keys, :content takes a string while :files takes a vector of filenames,
        ;with the format of "files/<filename>", the file(s) must be placed inside the files folder of the project.
        ;The file paths are with respect to the location of the project.clj file
    ; alternate-comment these lines for testing purposes
    (m/create-message! message-ch (:channel-id event)
                                   :content (:content reply)
                                   :attachments (create-filesvector (:files reply)))))
    ;(m/create-message! message-ch "1357304376330289233" :content reply)))

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
             (if ok? (do (event-enricher data message-ch) (inc n)) n))))

        (finally
          (m/stop-connection! message-ch)
          (async/close!           event-ch))))))
