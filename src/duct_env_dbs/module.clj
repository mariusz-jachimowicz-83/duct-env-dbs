(ns duct-env-dbs.module
  (:require
    [duct.core :as core]
    [duct.core.env :as env]
    [duct.core.merge :as merge]
    [integrant.core :as ig]
    [medley.core :as m]))

(def ^:private env-strategy
  {:production  :raise-error
   :development :rebase})

(defn- database-config [jdbc-url]
  {:duct.database.sql/hikaricp
   ^:demote {:jdbc-url jdbc-url
             :logger   (ig/ref :duct/logger)}})

(defn- migrator-config [environment]
  {:duct.migrator/ragtime
   ^:demote {:database   (ig/ref :duct.database/sql)
             :strategy   (env-strategy environment :rebase)
             :logger     (ig/ref :duct/logger)
             :migrations []}})

(defn- populate-required-dbs [config]
  (m/map-kv (fn [k v]
              (if (ig/derived-from? k ::requires-db)
                [k (assoc v :db (ig/ref :duct.database/sql))]
                [k v]))
            config))

(defn- get-environment [config options]
  (:environment options (:duct.core/environment config :production)))

(defmethod ig/init-key :duct-env-dbs.module/sql [_ options]
  {:req #{:duct/logger}
   :fn  (fn [config]
          (let [environment (get-environment config options)
                db-cfg  (database-config (get-in options [environment :database-url]))
                mig-cfg (migrator-config environment)]
            (-> config
                (core/merge-configs db-cfg mig-cfg)
                (populate-required-dbs))))})
