(ns app.model.mock-database
  "This is a mock database implemented via Datascript, which runs completely in memory, has few deps, and requires
  less setup than Datomic itself.  Its API is very close to Datomics, and for a demo app makes it possible to have the
  *look* of a real back-end without having quite the amount of setup to understand for a beginner."
  (:require
   [kubernetes-api.core :as k8sc]
   [app.util.k8s :as k8s :refer [oc]]
    [datascript.core :as d]
    [mount.core :refer [defstate]]))

;; In datascript just about the only thing that needs schema
;; is lookup refs and entity refs.  You can just wing it on
;; everything else.
(def schema {:account/id {:db/cardinality :db.cardinality/one
                          :db/unique      :db.unique/identity}})

(defn new-database [] (d/create-conn schema))

(defstate conn :start (new-database))

(def db {:element/id  [{:element/id 0
                        :element/text {:txt/id 0}
                        :element/elements [{:element/id 1}] ;(k8sc/invoke k8s/oc {:kind :CustomResourceDefinition :action :get :request {:name "keycloakrealms.keycloak.org"}})
                        }
                       {:element/id 1
                        :element/text {:txt/id 1}
                        :element/elements nil ;(k8sc/invoke k8s/oc {:kind :CustomResourceDefinition :action :get :request {:name "keycloakrealms.keycloak.org"}})
                        }]
         :txt/id [{:txt/id 0
                   :txt/text "basdaaaaa"}
                  {:txt/id 1
                   :txt/text "b"}]
         })

(get-in db [:element/id 1])
