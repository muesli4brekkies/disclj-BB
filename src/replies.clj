(ns replies
  (:require
   [clojure.java.io :as io]
   [clj-time       [core :as time]]
   [clojure.string :as string]
   [spoiler-channels      :as i]
   [clj-fuzzy.metrics :as fuzzy]))
(def md-dir "./data/markdown/")

(def baseurl "https://github.com/bitburner-official/bitburner-src/blob/stable/markdown/bitburner.")
(def mdn-url "https://developer.mozilla.org")

(defn sig [start n] (str "\n-# \\#" n " ~" (time/in-millis (time/interval start (time/now))) "ms, @mushroom.botherer if I misbehave."))

(defn- big-sig [start n]
  (str
   (sig start n)
   "This information is distributed without warranty under the MPL - \"muesli public license\" "
   "whereby you are legally compelled to immediately share this information with everyone within 100 metres "
   "under penalty of imprisonment or fine. "
   "Click [here](<https://www.youtube.com/watch?v=fC7oUOUEEi4>) for more information."))

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

(defn lcase-&-rm-ns [r] (string/lower-case (-> r (string/replace #"\(\)" "") (string/replace #"(?i)^ns\." "") string/trim)))

(defn signature-decorator
  "Wraps a message router with a) how long it took to generate the message b) Muesli's signature"
  [msg event f]
  (let [start-time (time/now)
        result (f msg event)
        end-time (time/now)]
    (str result "\n\n" "~" (time/in-millis (time/interval start-time end-time)) "ms, @mushroom.botherer if I misbehave.")))


(def ns-replies
  (reduce
   (fn [replies name]
     (assoc
      replies
      (keyword (lcase-&-rm-ns name))
      {:name name
       :sname (lcase-&-rm-ns name)
       :spoiler? (some #(string/includes? (lcase-&-rm-ns name) %) spoilers)
       :url (str baseurl (string/lower-case (string/replace name #"\(\)" "")) ".md")}))
   {}
   (flatten
    (for [f (.list (io/file md-dir))]
      (->
       md-dir
       (str f)
       slurp
       string/split-lines
       (nth 4)
       (string/split #" ")
       second
       (string/replace #"\\" ""))))))

(def mdn-replies
  (->>
   "./data/mdn-ref.properties"
   slurp
   string/split-lines
   (map (fn [line] (string/split line #"=")))
   (reduce
    (fn [prev [name url]]
      (assoc prev (keyword name)
             {:name name
              :sname name
              :spoiler false
              :url url}))
    {:mdn {:url mdn-url}})))

(defn fuzzy-search
  "Returns a message trying to fuzzy match the input."
  [spoil-ok? request replies]
  (->> [(str "\"" request "\" not found")
        "The machine spirit wonders if you meant..."
        (->> replies
             vals
             (map #(when (or spoil-ok? (not (% :spoiler?))) (assoc % :score (fuzzy/dice % request))))
             (sort-by #(get % :score ##Inf))
             reverse
             (take 5)
             (map #(str "- " (% :name) ": <" (% :url) ">")))
        "...? Bye!"]
       flatten
       (string/join "\n")))

(defn spoil-ok?
  [event]
  (and (not (= (:type event) :mdn)) (some #(= % (:channel-id event)) i/spoiler-channels)))
