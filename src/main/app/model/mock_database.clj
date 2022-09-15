(ns app.model.mock-database
  "This is a mock database implemented via Datascript, which runs completely in memory, has few deps, and requires
  less setup than Datomic itself.  Its API is very close to Datomics, and for a demo app makes it possible to have the
  *look* of a real back-end without having quite the amount of setup to understand for a beginner."
  (:require
   [kubernetes-api.core :as k8sc]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
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

;; (def st (k8sc/invoke k8s/oc {:kind :CustomResourceDefinition :action :get :request {:name "keycloakrealms.keycloak.org"}}))

;; let [st (or st (get-in (:openAPIV3Schema (:schema (first (get-in st [:spec :versions])))) [:properties :spec :properties :realm :properties]))
;;         id (or id 0)
;;         ;db (or db {:element/id []})
;;         ]

(defn drop-nil [m]
  (apply merge (for [[k v] m :when (not (nil? v))] {k v})))

(defn parse-st-at [st id at]
  (if-not (= id at)
  (let [h (fn [st id] (let [k (vec (keys st))]
                                      (if (> (count k) 1)
                                          (mapv (fn [x]
                                                  (parse-st-at (select-keys st [x]) id at)) k)
                                          (if (= (count k) 1)
                                           (drop-nil
                                            {:element/id (tempid/tempid)
                                             :element/text (str (first k))
                                             :element/elements (let [v (vec (flatten [(parse-st-at ((first k) st) id at)]))]
(if-not (every? nil? v) v))})
                                            {:element/id (tempid/tempid)
                                             :element/text k
                                             })
                                          )))]
  (condp = (type st)
    clojure.lang.PersistentVector (mapv #(parse-st-at % (inc id) at) st)
    clojure.lang.PersistentArrayMap (h st (inc id))
    clojure.lang.PersistentHashMap (h st (inc id))
    ;java.lang.String 
    ;java.lang.Long st
    {:element/id (tempid/tempid)
     :element/text (str st)}))))

(defn flatten-one-level [coll]  
  (mapcat  #(if (sequential? %) % [%]) coll))

(defn parse-st [st]
  ;(println (type st))
  ;(println (vec (keys st)))
  (let [h (fn [st] (let [k (vec (keys st))]
                                      (if (> (count k) 1)
                                          (mapv (fn [x]
                                                 (parse-st (select-keys st [x]))) k)
                                          (if (= (count k) 1)
                                            {:element/id (tempid/tempid)
                                             :element/text (str (first k))
                                             :element/elements (flatten (parse-st ((first k) st)))}
                                            {:element/id (tempid/tempid)
                                             :element/text k
                                             :element/elements nil}
))))]
  (condp = (type st)
    clojure.lang.PersistentVector (mapv parse-st st)
    clojure.lang.PersistentArrayMap (h st)
    clojure.lang.PersistentHashMap (h st)
    ;java.lang.String 
    ;java.lang.Long st
    {:element/id (tempid/tempid)
     :element/text (str st)
     :element/elements nil})))

(defn make-flat [graph]
  (loop [graph graph]
    (if (every? keyword? graph) graph
      (recur (into graph (flatten (seq (first (filter #(not (keyword? %)) graph)))))))))

(defn drop-at [st at]
  )

(def db {:element/id  [{:element/id 0
                        :element/text {:txt/id 0}
                        :element/elements [{:element/id 1}]
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

;; (def st-db (parse-st (get-in (:openAPIV3Schema (:schema (first (get-in st [:spec :versions])))) [:properties :spec :properties :realm :properties])))

;; (def st-db (parse-st st))

;;(def st-db (parse-st-at st 0 20))



                                        ;(get-in db [:element/id 1])


