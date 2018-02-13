(ns duct-env-dbs.module-test
  (:require
   [clojure.java.io :as io]
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer :all]
   [duct.core :as duct]
   [duct.database.sql :as sql]
   [duct.database.sql.hikaricp :as hikaricp]
   [duct.logger :as logger]
   [integrant.core :as ig]
   [duct-env-dbs.main]))

(duct/load-hierarchy)

(defrecord NoOpLogger []
  logger/Logger
  (-log [logger level ns-str file line id event data]))

;; fake logger initialization
;; we don't need whole logger subsystem
(defmethod ig/init-key :duct/logger [_ config] (->NoOpLogger))

(def base-config
  {::duct/environment :development
   :duct/logger {}
   :duct.module/sql {:development {:database-url "jdbc:sqlite:"}
                     :production  {:database-url "db-production-url"}
                     :testing     {:database-url "db-testing-url"}}})

(deftest configuration-test
  (testing "DB url is taken based on environment"
    (is (= (duct/prep base-config)
           (merge base-config
                  {:duct.database.sql/hikaricp
                   {:jdbc-url "jdbc:sqlite:"
                    :logger   (ig/ref :duct/logger)}

                   :duct.migrator/ragtime
                   {:database   (ig/ref :duct.database/sql)
                    :logger     (ig/ref :duct/logger)
                    :strategy   :rebase
                    :migrations []}})))
    (let [base-config (assoc base-config ::duct/environment :testing)]
      (is (= (duct/prep base-config)
             (merge base-config
                    {:duct.database.sql/hikaricp
                     {:jdbc-url "db-testing-url"
                      :logger   (ig/ref :duct/logger)}

                     :duct.migrator/ragtime
                     {:database   (ig/ref :duct.database/sql)
                      :logger     (ig/ref :duct/logger)
                      :strategy   :rebase
                      :migrations []}}))))))

(defn- unwrap-logger [^javax.sql.DataSource datasource]
  (.unwrap datasource javax.sql.DataSource))

(deftest db-pool-test
  (let [launch-parts [:duct.module/sql :duct.database.sql/hikaricp]
        _ (println "== before ")
        _ (clojure.pprint/pprint (duct/prep base-config))
        system (-> base-config
                   (duct/prep)
                   (ig/init))
        _ (println "== system started")
        _ (clojure.pprint/pprint system)
        spec (-> system :duct.module/sql :spec)]

    (testing "jdbc using Hikari connection pool"
      (jdbc/execute! spec ["CREATE TABLE foo (id INT, body TEXT)"])
      (jdbc/db-do-commands spec ["INSERT INTO foo VALUES (1, 'a')"
                                 "INSERT INTO foo VALUES (2, 'b')"])
      (is (= (jdbc/query spec ["SELECT * FROM foo"])
            [{:id 1, :body "a"} {:id 2, :body "b"}]))
      (is (= (jdbc/query spec ["SELECT * FROM foo WHERE id = ?" 1])
            [{:id 1, :body "a"}]))
      (is (= (jdbc/query spec ["SELECT * FROM foo WHERE id = ? AND body = ?" 1 "a"])
            [{:id 1, :body "a"}])))
    
    (testing "closing Hikari connection pool"
      (is (not (-> spec :datasource unwrap-logger .isClosed)))
      (ig/halt! system)
      (is (-> spec :datasource unwrap-logger .isClosed))
      #_(ig/halt! system))))
