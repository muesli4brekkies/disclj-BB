(ns routes
  (:gen-class)
  (:require
   [replies               :as r]
   [clojure.string :as string]))

(def empty-mdn
  {:condition (fn [msg event] (and (= "" msg) (re-matches #"(?i)^!mdn.*" (:content event))))
   :result (fn [_ _]  "https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Scripting")})

(def empty-ns
  {:condition (fn [msg event] (and (= "" msg) (re-matches #"(?i)^!ns.*" (:content event))))
   :result (fn [_ _]  "https://github.com/bitburner-official/bitburner-src/blob/stable/markdown/bitburner.md")})

(def too-long
  {:condition (fn [msg _] (< 70 (count msg)))
   :result (fn [_ _]  "Nuh uh, that request is too long.")})

(def robot
  {:condition (fn [msg _] (or (= msg "bleep bloop") (= msg "bloop bleep")))
   :result (fn [msg _] (string/join " " (-> msg (string/split #"\s") reverse)))})

(def poast-coad
  {:condition (fn [msg _] (= msg "poast coad"))
   :result (fn [_ _] "https://cdn.discordapp.com/attachments/1287394172894052507/1357485911885222060/image.png")})

(def pspsps
  {:condition (fn [msg _] (re-find #"^ps(ps)+" msg))
   :result (fn [_ _] "https://cdn.discordapp.com/attachments/1287394172894052507/1365428275496882224/image.png")})

(def zoe
  {:condition (fn [msg _] (or (= msg "zoe") (= msg "zoë")))
   :result (fn [_ _] "https://media.discordapp.net/attachments/1369725817038442620/1371201395813781645/image.png")})

(def persecution
  {:condition (fn [msg _]
                (or
                 (seq (re-matches #".*(do( not|n'?t|nut|ughnut) buy hacknet).*" msg))
                 (seq (re-matches #".*(hacknet (is( not|n'?t) worth it|sucks|is a bad investment)).*" msg))))
   ;; Source: https://github.com/bitburner-official/typescript-template/issues/23
   :result (fn [_ _] "Hacknet a bad investment early game? You serious? Have you done your mathematics with that thing or not? I guess not. I'm from a heavily mathematical and scientific and scholastic family and lineage since like, Ancient China 5000 years ago? I was forced to do math drills before I could even play anything or whatever. I even had to compete in speed and accuracy with my childhood friend. Of course I won by a mile. Look, calculate the multiplicative aspects of the Hacknets. They'll earn way more than any early servers you can hack because the early game server are so darn poor. hack() is a percentage thingy as far as I can gather. Low server max money, low script income. It doesn't get any simpler to figure out than that. Before you insult my \"mental bandwidth\", I'll have you know that I have full score for all IQ tests I take anytime, anywhere, including those ever increasing difficulty ones that keep going until seemingly forever, and those were so easy and repetitive that I eventually got bored after I passed 300+IQ score and quit out of boredom, even though I originally was aiming for 1000+IQ score. I've always gotten 100% for my Mathematics from kindergarten till end of college/uni and so same programming subjects and logic and abstraction subject. Anyway, enough about me. Just wanted you to know not to judge other people without first getting to know them. It's rude.")})

(def duck
  {:condition (fn [msg _] (or (string/includes? msg "quack") (string/includes? msg "duck")))
   :result (fn [_ _] "quack 🦆")})

(def naughty
  {:condition (fn [msg event]
                (and (not (r/spoil-ok? event)) (some (fn [r] (string/includes? msg r)) r/spoilers)))
   :result (fn [_ _] r/tell-off)})

(def lookup
  {:condition (fn [_ _] true)
   :result (fn [msg event]
             (let [replies (if (string/starts-with? (:content event) "!ns") r/ns-replies r/mdn-replies)]
               (r/signature-decorator
                msg
                event
                (fn [msg event]
                  (if-let [match (-> msg keyword replies :url)]
                    (str "<" match ">")
                    (r/fuzzy-search (r/spoil-ok? event) msg replies))))))})