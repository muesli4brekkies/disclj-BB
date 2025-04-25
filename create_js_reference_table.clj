(ns create-js-reference-table
  (:require
   [clojure.java.io :as io]
   [clojure.string :as s])
  (:import
   [java.util.zip ZipInputStream]))

;; (def mdn-repository "./mdn.zip")
(def mdn-repository "https://codeload.github.com/mdn/content/zip/refs/heads/main")


(defn- read-mdn-repository []
  (with-open [stream (-> mdn-repository io/input-stream ZipInputStream.)]
    (loop [entry (.getNextEntry stream) collect []]
      (if entry
        (recur (.getNextEntry stream) (conj collect (.getName entry)))
        collect))))

(defn create-properties []
  (let [files (read-mdn-repository)]
    (->> files
         (filter #(s/starts-with? % "content-main/files/en-us/web/javascript/reference"))
         (filter #(s/ends-with? % "/index.md"))
         (map #(vector
                (get (re-find #"^.*?javascript\/reference\/(.*?)\/index.md$" %) 1 "")
                (-> %
                    (s/replace #"^.*?\/en-us" "https://developer.mozilla.org/en-US/docs")
                    (s/replace #"\/index\.md" ""))))
         (map (fn [[key, url]]
                (if (s/starts-with? key "global_objects")
                  [(-> key (s/replace #"^global_objects\/" "") (s/replace #"\/" ".")) url]
                  [(s/replace key #"\/" " ") url])))
         (filter #(not (= (first %) "")))
         (reduce #(str %1 (first %2) "=" (second %2) \newline) ""))))

(spit "mdn-ref.properties" (create-properties))