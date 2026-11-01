(ns core
  (:gen-class)
  (:require
   [routes :as routes]
   [clojure.string        :as string]
   [clojure.core.async    :as async]
   [discljord.connections :as connection]
   [discljord.messaging   :as msg]
   [discljord.formatting :as fmt]
   [clojure.pprint :as pp]))

(def TEST
  true)

(def token
  "Discord bot token, used as ID and 'security measure' in one. Use your own!"
  (slurp "./data/discord_bot.key"))

(def guild-id
  "The guild you use to test the bot. Global commands can take a while to register in comparison.
  Note: There is a global rate limit of 200 application command creates per day, per guild"
  1369725817038442616)

(def app-id
  "Application ID of the bot."
  1369689799413334178)

;; (def intents #{:guilds :guild-messages})

;; (defn route-msg
;;   "Decides on what output message to generate based on the routes (condition->result) and the input message.
;;   Routes will be handled from front to back."
;;   [routes msg event]
;;   (let [route (first routes)]
;;     (when (= 0 (count routes)) (prn "No match!"))
;;     (if ((:condition route) msg event)
;;       ((:result route) msg event)
;;       (recur (rest routes) msg event))))

;; (def router
;;   "Message router, partially applied with all routes."
;;   (partial route-msg
;;            ;; Order matters here, as the router will go through this front to back.
;;            [routes/empty-mdn
;;             routes/empty-ns
;;             routes/version
;;             routes/too-long
;;             routes/robot
;;             routes/poast-coad
;;             routes/pspsps
;;             routes/zoe
;;             routes/persecution
;;             routes/duck
;;             routes/naughty
;;             routes/lookup-mdn
;;             routes/lookup-ns]))

;; (defn- event-enricher
;;   "Turns an event into a map with all relevant data."
;;   [event message-ch n]
;;   ;; Ensure bogus requests are ignored early.
;;   (when (< 70 (count (:content event))))

;; ;; Dev note: https://gist.github.com/chrischambers/fcbbb4d1e856fdab6fa173d713fa96e7


(def conn (msg/start-connection! token))
(def events (async/chan 100 (comp (filter (comp #{:interaction-create} first)) (map second))))
(def channel (connection/connect-bot! token events :intents #{}))

(def command-option-types
  "See this link for more info about the types:
  https://discord.com/developers/docs/interactions/application-commands#application-command-object-application-command-option-type"
  {:sub-command 1
   :sub-command-group 2
   :string 3
   :integer 4
   :boolean 5
   :user 6
   :channel 7
   :role 8
   :mentionable 9
   :number 10
   :attachment 11
   })

(def greet-cmd
  {:name "hello"

   :description "Say hi to someone"

   :options [{:type (command-option-types :user)
              :name "user"
              :description "The user to greet"}]

   :handler (fn
              ;; `target-id` will be the user id of the user to greet (if set)
              [{:keys [id token] {{user-id :id} :user} :member {[{target-id :value}] :options} :data}]
              (msg/create-interaction-response! conn id token 4 :data
                                                {:content (str "Hello, " (fmt/mention-user (or target-id user-id)) " :smile:")}))})

(def ns-cmd
  {:name "ns"

   :description "Look up documentation of an NS API entry."

   :options [{:type (command-option-types :string)
              :name "query"
              :description "What you want to look for in the NS API."
              :required true
              :max_length 70}]

   :handler (fn
              [{:keys [id token] {cmd-options :options} :data}]
              (msg/create-interaction-response! conn id token 4 :data
                                                {:content
                                                 "hello"
                                                 ;;(r/signature-decorator (:value (first cmd-options)) nil (fn [query _] r/fuzzy-search true query r/ns-replies))
                                                 }))})


(def mdn-cmd
  {:name "mdn"

   :description "Look up documentation of an MDN JS API entry."

   :options [{:type (command-option-types :string)
              :name "query"
              :description "What you want to look for in the MDN API."
              :required true
              :max_length 70}]

   :handler (fn
              ;; TODO FIXME
              ;; edit original interaction response
              [{:keys [id token] {cmd-options :options} :data}]
              ;;(println @(msg/create-interaction-response! conn id token 6 :data {:content "x"}))
              ;;(println "MDN!")
              ;;(Thread/sleep 5000)
              ;;(println @(msg/create-followup-message! conn id token :data {:content "hello"} ))

              (println @(msg/create-interaction-response! conn id token 4 :data
                                                          ;; {:content
                                                          ;;  ;;"hello"
                                                          ;;  (r/fuzzy-search true (:value (first cmd-options)) r/mdn-replies)}
                                                          {:flags 32768
                                                           :components
                                                           [{:type 10
                                                             :content (r/fuzzy-search true (:value (first cmd-options)) r/mdn-replies)}
                                                            {:type 1
                                                             :components [{:type 2
                                                                           :label "1"
                                                                           :style 1
                                                                           :custom_id "button_one"}
                                                                          {:type 2
                                                                           :label "2"
                                                                           :style 1
                                                                           :custom_id "button_two"}
                                                                          {:type 2
                                                                           :label "3"
                                                                           :style 1
                                                                           :custom_id "button_three"}]}
                                                            ;; {:type 3
                                                            ;;  :custom_id "string_select"
                                                            ;;  :placeholder "Favourite bug?"
                                                            ;;  :options [{:label "Butterfly"
                                                            ;;             :value "butterfly"
                                                            ;;             :emoji {:name "🦋"}}
                                                            ;;            {:label "Caterpillar"
                                                            ;;             :value "caterpillar"
                                                            ;;             :emoji {:name "🐛"}}]}

                                                            ]
                                                           }
                                                          )))})

                                                          (defn register-command!
                                                            "Register a single command."
                                                            [command]
                                                            ;; Params: guild id (omit for global commands), command name, command description, optionally command options
                                                            (println @(msg/create-guild-application-command! conn app-id guild-id (command :name) (command :description) :options (command :options)))

                                                            (when (not TEST)
                                                              @(msg/create-global-application-command! conn app-id (command :name) (command :description) :options (command :options))))

(defn handle-command
  ;; `target-id` will be the user id of the user to greet (if set)
  [commands interaction]

  ;; For testing/debugging
  (pp/pprint interaction)

  ;; Destructure interaction for easy access
  (let [{:keys [id token] {cmd-name :name cmd-options :options} :data} interaction]
    (println cmd-name)
    (println cmd-options)

    ;; (cond
    ;;   (= "ns" cmd-name) ((ns-cmd :handler) interaction)
    ;;   (= "mdn" cmd-name) ((mdn-cmd :handler) interaction)
    ;;   (= "greet" cmd-name) ((greet-cmd :handler) interaction)
    ;; :else "Oops! This isn't supposed to happen. Please try again."))

    ((mdn-cmd :handler) interaction)))

(def commands
  [ns-cmd greet-cmd mdn-cmd])

(defn -main
  "Register and handle commands."
  []
  (run! register-command! commands)

  (async/go-loop []
    (when-let [interaction (async/<! events)]
      ;; See example interaction: https://discord.com/developers/docs/interactions/application-commands#slash-commands-example-interaction
      (handle-command commands interaction)
      (recur)))
  )
