(ns app.util.parse-crds
(:require [app.util.k8s :as k8s :refer [crds]]
          [clojure.spec.alpha :as s]
          [spec-provider.provider :as sp]
          [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
          [com.fulcrologic.fulcro.algorithms.server-render :as server-render]))

(defn get-version-name [crd]
  (-> crd (:spec) (:versions) (first) (:name)))

(defn get-schema [crd]
  (-> crd (:spec) (:versions) (first) (:schema) (:openAPIV3Schema) (:properties) (:spec)))

(defn keep-required [schema-spec]
  )

(defn select-walker-t [afn ds]
  (filter #(and (not (coll? %)) (afn %))
          (tree-seq coll? seq ds)))


;; property

(s/def :property/id tempid/tempid?)
(s/def :property/name keyword?)
(s/def :property/description string?)
(s/def :property/type #{"object" "string" "integer" "array" "boolean"})


(s/def :property/properties (s/coll-of :cr/property))
(s/def :property/required (s/coll-of string?))
(s/def :property/items list?)
(s/def :property/format string?)


(s/def :cr/property (s/keys :req-un [:property/id :property/name :property/type :property/description]
                            :opt-un [:property/required :property/items :property/format :property/properties]))

(def test-prop {:id (tempid/tempid)
                :name :asd
                :description "Factor is a factor to multiply the base duration after each failed retry",
                :properties [{:id (tempid/tempid) :name :bastu :type "integer"}]
                :type "integer",
                :format "int64"})

;(s/conform :cr/property test-prop)

(defn convert-to-vec [m]
  (let [k (keys m)
        v (vals m)]
   (mapv
    #(conj (nth v %) {:id (tempid/tempid) :name (nth k %)}) (range (count k)))))

;; (convert-to-vec {:a {:b 1}
;;                  :s {:c 2}})

;; spec

;; (defn parse-cr [e]
;;   (condp = (type e)
;;     clojure.lang.PersistentVector (mapv parse-cr e)
;;     clojure.lang.PersistentArrayMap (do (println (keys e)) (parse-cr (convert-to-vec e)))
;;     clojure.lang.PersistentHashMap (do (println (keys e)) (parse-cr (convert-to-vec e)))
;;     ;java.lang.String 
;;     ;java.lang.Long st
;;     nil))

(defn map->nsmap
  [m n]
  (reduce-kv (fn [acc k v]
               (let [new-kw (if (and (keyword? k)
                                     (not (qualified-keyword? k)))
                              (keyword (str n) (name k))
                              k) ]
                 (assoc acc new-kw v)))
             {} m))

(defn parse-cr [e & {:keys [name]}]
  (map->nsmap
   (-> e
       (conj {:id (tempid/uuid)
              :name name})
       (conj
        (if (contains? e :properties)
        (let [k (keys (:properties e))
              v (vals (:properties e))]
          {:properties (mapv #(parse-cr (nth v %) {:name (nth k %)}) (range (count k)))})))
       (conj
        (if (contains? e :items)
          {:items (parse-cr (:items e))}))) 'property))

(defn build-crd-data [crd-string comp]
  (let [parsed (parse-cr (get-schema (k8s/get-crd k8s/crds crd-string)) {:name :spec})
        init-state (server-render/build-initial-state {:property/id "blah" :property/properties parsed} comp)
        crd-top-prop (:property/id parsed)]
    {:crd {:crd/id crd-string
           :crd/property [:property/id crd-top-prop]}
     :properties (vec (vals (:property/id init-state)))}))

;; (s/def :spec/id tempid/tempid?)
;; (s/def :spec/name #(= % :spec))
;; (s/def :spec/description string?)

;; (s/def :cr/spec (s/keys :req-un [:spec/id :spec/name :/type]
;;                                   :opt-un [:]))

;(s/def :cr/cr ())


(comment

(let [parsed (parse-cr (get-schema (k8s/get-crd k8s/crds "APIManager")) {:name :spec})
      ids (:property/id (server-render/build-initial-state {:property/id "blah" :property/properties parsed}  app.model.property/Property))
      top-p (:property/id parsed)]
ids
(get ids top-p)
)

;; (sp/infer-specs (flatten (get-schema (k8s/get-crd k8s/crds "Application"))) :cre)

;; (filter #(keyword (first %)) :properties )
)
