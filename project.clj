(defproject disclj "0.2.x-DUCKnRACCOONFRIENDS-edition"
  :description "The Official Unofficial Bitburner Discord Bot"
  :url "https://github.com/muesli4brekkies/disclj-BB"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [com.github.discljord/discljord "1.3.1"]
                 [clj-fuzzy "0.4.1"]
                 [clj-time "0.15.2"]]
  :main ^:skip-aot core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
