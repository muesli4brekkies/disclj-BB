(ns routes
  (:gen-class)
  (:require
   [replies               :as r]
   [spoiler-channels      :as i]
   [clj-time       [core :as t]]
   [clojure.string        :as s]
   [clojure.core.async    :as a]
   [discljord.connections :as c]
   [discljord.messaging   :as m]))

(def robot
  {:condition (fn [msg _] (or (= msg "bleep bloop") (= msg "bloop bleep")))
   :result (fn [msg _] (s/join " " (-> msg (s/split #"\s") reverse)))})

(def poast-coad
  {:condition (fn [msg _] (= msg "poast coad"))
   :result (fn [msg _] "https://cdn.discordapp.com/attachments/1287394172894052507/1357485911885222060/image.png")
   })

(def pspsps
  {:condition (fn [msg _] (re-find #"^ps(ps)+" msg))
   :result (fn [msg _] "https://cdn.discordapp.com/attachments/1287394172894052507/1365428275496882224/image.png")})

(def persecution
  {:condition (fn [msg _]
                (or
                 (seq (re-matches #".*(do( not|n'?t|nut|ughnut) buy hacknet).*" msg))
                 (seq (re-matches #".*(hacknet (is( not|n'?t) worth it|sucks|is a bad investment)).*" msg))))
   :result (fn [msg _] "Hacknet a bad investment early game? You serious? Have you done your mathematics with that thing or not? I guess not. I'm from a heavily mathematical and scientific and scholastic family and lineage since like, Ancient China 5000 years ago? I was forced to do math drills before I could even play anything or whatever. I even had to compete in speed and accuracy with my childhood friend. Of course I won by a mile. Look, calculate the multiplicative aspects of the Hacknets. They'll earn way more than any early servers you can hack because the early game server are so darn poor. hack() is a percentage thingy as far as I can gather. Low server max money, low script income. It doesn't get any simpler to figure out than that. Before you insult my \"mental bandwidth\", I'll have you know that I have full score for all IQ tests I take anytime, anywhere, including those ever increasing difficulty ones that keep going until seemingly forever, and those were so easy and repetitive that I eventually got bored after I passed 300+IQ score and quit out of boredom, even though I originally was aiming for 1000+IQ score. I've always gotten 100% for my Mathematics from kindergarten till end of college/uni and so same programming subjects and logic and abstraction subject. Anyway, enough about me. Just wanted you to know not to judge other people without first getting to know them. It's rude.")})

(def duck
  {:condition (fn [msg _] (or (s/includes? msg "quack") (s/includes? msg "duck")))
   :result (fn [msg _] r/duk)})


(def naughty
  {:condition (fn [msg event]
                (and (not (r/spoil-ok? event)) (some (fn [r] (s/includes? msg r)) r/spoilers)))
   :result (fn [msg _] r/tell-off)})

(def mdn-lookup
  {:condition (fn [_ event] (re-matches #"(?i)^!mdn.*" (:content event)))
   :result (fn [msg _] "mdn placeholder")})

(def ns-lookup
  {:condition (fn [_ event] (re-matches #"(?i)^!ns.*" (:content event)))
   :result (fn [msg _] "ns placeholder")})
