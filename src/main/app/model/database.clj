(ns app.model.database
  (:require
   [kubernetes-api.core :as k8sc]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [app.util.k8s :as k8s :refer [oc]]
   [datascript.core :as d]
   [mount.core :refer [defstate]]))

;; In datascript just about the only thing that needs schema
;; is lookup refs and entity refs.  You can just wing it on
;; everything else.
(def schema {:property/id          {:db/cardinality          :db.cardinality/one
                                    :db/unique               :db.unique/identity}

             :property/properties  {:db/cardinality          :db.cardinality/many
                                    :db/valueType            :db.type/ref
                                    :db/isComponent          true}

             :property/items       {:db/cardinality          :db.cardinality/many
                                    :db/valueType            :db.type/ref
                                    :db/isComponent          true}

             :crd/id               {:db/cardinality          :db.cardinality/one
                                    :db/unique               :db.unique/identity}

             :crd/property         {:db/cardinality          :db.cardinality/one
                                    :db/valueType            :db.type/ref
                                    :db/isComponent          true}

             :crd-group/id         {:db/cardinality          :db.cardinality/one
                                    :db/unique               :db.unique/identity}})

(defn new-database [schema] (d/create-conn schema))

(defstate conn :start (new-database schema))

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
