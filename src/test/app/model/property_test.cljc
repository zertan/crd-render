(ns app.model.property_test
  (:require
   [app.model.property :as property]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   #?(:clj
      [app.model.database :as db])
   #?(:clj
      [app.util.k8s :as k8s :refer [oc]])
   #?(:clj
      [datascript.core :as d])
    [taoensso.timbre :as log]
    [com.wsscode.pathom.connect :as pc]
    #?(:cljs [com.fulcrologic.fulcro.mutations :as m])
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    #?(:clj
       [com.fulcrologic.fulcro.algorithms.server-render :as server-render])
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [app.util.parse-crds :as parse-crds])
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div]])
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item ui-icon ui-popup ui-grid ui-grid-column ui-grid-row]]))


(comment

(let [crd-temp (k8s/get-crd k8s/crds "Elasticsearch")]
     (d/transact db/conn [{:crd/id "Elasticsearch"
                           :crd/group "elasticsearch.k8s.elastic.co"
                           :crd/version (parse-crds/get-version-name crd-temp)
                           :crd/property (parse-crds/parse-cr (parse-crds/get-schema crd-temp) {:name :spec})}]))


(d/transact! db/conn [[:db/add eid :aliases ["X" "Y" "Z"]]])

 (d/pull @db/conn [:property/id] [:property/id 0])

(d/q '[:find ?id
       :where [?p :property/id ?id]]
     @db/conn)

(d/q '[:find  ?r, ?t
       :where [?p :property/id #uuid "51a40c81-853f-443f-86f1-c38f6d46b0a5"]
       [?p :property/name :spec]
       [?p :property/required ?r]
       [?p :property/type ?t]
              ;[?p :property/required true]
       ]
     @db/conn)

(def r (d/q '[:find ?r
              :where [?c :crd/id "Elasticsearch"]
                     [?c :crd/property ?p]
                     [?p :property/required ?r]]
        @db/conn))



(d/pull @db/conn '[{:crd/property [{:property/properties ...} :property/required]}] [:crd/id "Elasticsearch"])



(def fid (d/q [:find '?id
               ['?p :property/id '?id]
               (into (map (fn [x] ['?p :property/name (keyword x)]) (ffirst r)) '(or ) )]
        @db/conn))

(d/pull @db/conn '[:property/properties] [:property/id (ffirst fid)])

; (d/pull @db/conn '[:property/_properties {:property/_id [:db/id]}] #uuid "73756528-dbe6-4105-8b1a-2e39c504ee8d")

; (d/pull @db/conn '[:property/_properties {:property/_id [:db/id]}] #uuid "73756528-dbe6-4105-8b1a-2e39c504ee8d")

(d/pull @db/conn '[{:property/properties [:property/id]}] (:crd/property (d/pull @db/conn '[:crd/property] [:crd/id "APIManager"])))

(d/pull @db/conn '[{:crd/property [:property/properties ]} ] [:crd/id "APIManager"])

(def parsed-c (parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds "Keycloak")) {:name :spec}))

;(def db-crd (server-render/build-initial-state {:property/id "blah" :property/properties (parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds "Keycloak")) {:name :spec})} Property))

;(d/transact db/conn (vec (vals (:property/id db-t))))

;(d/transact db/conn  (vec (vals (:property/id db-crd))))

(let [crd (parse-crds/build-crd-data "APIManager" Property)]
(d/transact db/conn (:properties crd))
(d/transact db/conn [(:crd crd)]))

 (d/transact db/conn [ {:property/id 1 :property/name :crab}])

(def test-prop #:property{:id (tempid/uuid) :name :blah :properties [ #:property{:id (tempid/uuid)
                                                                                  :name :asd
                                                                                  :description "Factor is a factor to multiply the base duration after each failed retry",
                                                                                  :properties [#:property{:id (tempid/uuid) :name :bastu :type "integer"}]
                                                                                  :type "integer",
                                                                                  :format "int64"
                
                                                                                  }]})

:property/id :property/properties :property/name :property/type :property/description :property/items :property/required
(def db-t (server-render/build-initial-state test-prop Property))


;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Assuming you have these entities in datomic:

(d/transact conn [{:host/name "host1"}])

(d/transact conn [{:server/name "db1"
                   :server/host [:host/name "host1"]}
                  {:server/name "web1"
                   :server/host [:host/name "host1"]}])

;;And assuming each server has a reference to host (please see schema below), in order to query which servers are linked to a host, use the reverse relation syntax '_':


(d/q '[:find (pull ?h [* {:server/_host [:server/name]}])
       :in $ ?hostname
       :where
       [?h :host/name ?hostname]]
     (d/db conn)
     "host1")

;;will give you:

[[{:db/id 17592186045418,
   :host/name "host1",
   :server/_host [#:server{:name "db1"} #:server{:name "web1"}]}]]

;;Here is the sample schema for your reference:

(def uri "datomic:free://localhost:4334/svr")
(d/delete-database uri)
(d/create-database uri)
(def conn (d/connect uri))

(d/transact conn [{:db/ident       :server/name
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity
                   :db/valueType   :db.type/string}
                  {:db/ident       :server/host
                   :db/cardinality :db.cardinality/one
                   :db/valueType   :db.type/ref}
                  {:db/ident       :host/name
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity
                   :db/valueType   :db.type/string}])


;;;;;;;;;


(let [groups (k8s/create-crd-groups k8s/crds)
           ids (mapv (fn [x] [:crd-group/id (:crd-group/id x)]) groups)]
     (d/transact! db/conn groups))

(let [req (d/q '[:find ?id
                 :where [?c :crd-group/id ?id]]
                 @db/conn)]
        {:main/crd-groups (mapv (fn [x] [:crd-group/id (first x)]) req)})




)

