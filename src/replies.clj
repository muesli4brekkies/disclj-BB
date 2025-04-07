(ns replies
  (:require
   [clojure.java.io :as io]
   [clj-time       [core :as t]]
   [clojure.string :as s]))

(def md-dir "/home/debbyadmin/serverfiles/markdown/")

(def baseurl "https://github.com/bitburner-official/bitburner-src/blob/dev/markdown/bitburner.")

(defn sig [start n] (str "\n-# \\#" n " ~" (t/in-millis (t/interval start (t/now))) "ms, @mushroom.botherer if I misbehave."))

(defn- big-sig [start n]
  (str
   (sig start n)
   "This information is distributed without warranty under the MPL - \"muesli public license\" "
   "whereby you are legally compelled to immediately share this information with everyone within 100 metres "
   "under penalty of imprisonment or fine. "
   "Click [here](<https://www.youtube.com/watch?v=fC7oUOUEEi4>) for more information."))
(defn measured-hacknet-response [start n]
  (str
   "Hacknet a bad investment early game? You serious? Have you done your mathematics with that thing or not? I guess not. I'm from a heavily mathematical and scientific and scholastic family and lineage since like, Ancient China 5000 years ago? I was forced to do math drills before I could even play anything or whatever. I even had to compete in speed and accuracy with my childhood friend. Of course I won by a mile. Look, calculate the multiplicative aspects of the Hacknets. They'll earn way more than any early servers you can hack because the early game server are so darn poor. hack() is a percentage thingy as far as I can gather. Low server max money, low script income. It doesn't get any simpler to figure out than that. Before you insult my \"mental bandwidth\", I'll have you know that I have full score for all IQ tests I take anytime, anywhere, including those ever increasing difficulty ones that keep going until seemingly forever, and those were so easy and repetitive that I eventually got bored after I passed 300+IQ score and quit out of boredom, even though I originally was aiming for 1000+IQ score. I've always gotten 100% for my Mathematics from kindergarten till end of college/uni and so same programming subjects and logic and abstraction subject. Anyway, enough about me. Just wanted you to know not to judge other people without first getting to know them. It's rude."
   (big-sig start n)))

(def duk "quack 🦆")

(def tell-off "Doesn't look like anything to me. That's probably a spoiler on this channel. --> <#415207923506216971>")

(def spoilers
  ["gang"

   "corporation"
   "warehouse"
   "office"
   "corp"
   "division"

   "bitnode"
   "bitnodemultipliers"

   "bladeburner"

   "singularity"
   "stanek"
   "hash"
   "grafting"
   "sleeve"])

(defn lcase-&-rm-ns [r] (s/lower-case (-> r (s/replace #"\(\)" "") (s/replace #"(?i)^ns\." "") s/trim)))


(def replies
  (reduce
   (fn [replies name]
     (assoc
      replies
      (keyword (lcase-&-rm-ns name))
      {:name name
       :sname (lcase-&-rm-ns name)
       :spoiler? (some #(s/includes? (lcase-&-rm-ns name) %) spoilers)
       :url (str baseurl (s/lower-case (s/replace name #"\(\)" "")) ".md")}))
   {}
   (flatten
    (for [f (.list (io/file md-dir))]
      (->
       md-dir
       (str f)
       slurp
       s/split-lines
       (nth 4)
       (s/split #" ")
       second
       (s/replace #"\\" ""))))))