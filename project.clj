(defproject com.mjachimowicz/duct-env-dbs "0.1.0-SNAPSHOT"
  :description "Very simple sql db module for Duct framework"
  :url         "https://github.com/mariusz-jachimowicz-83/duct-env-dbs"
  :license     {:name "Eclipse Public License"
                :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [integrant "0.6.1"]
                 [medley    "1.0.0"]
                 [duct/core "0.6.1"]
                 [duct/database.sql.hikaricp "0.3.2"]
                 [duct/migrator.ragtime      "0.2.1"]]

  :deploy-repositories [["clojars" {:sign-releases false}]]

  ;; lein cloverage --fail-threshold 95
  ;; lein kibit
  ;; lein eastwood
  :profiles {:dev {:dependencies [[duct/logger "0.2.1"]
                                  [org.xerial/sqlite-jdbc "3.20.1"]
                                  [org.slf4j/slf4j-nop    "1.7.25"]
                                  [org.clojure/java.jdbc  "0.7.3"]]
                   :plugins [[lein-cloverage  "1.0.10"]
                             [lein-kibit      "0.1.6"]
                             [jonase/eastwood "0.2.5"]]}})
