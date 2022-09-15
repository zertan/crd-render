(ns app.util.parse-crds
(:require [app.util.k8s :as k8s :refer [crds]]
          [clojure.spec.alpha :as s]
          [spec-provider.provider :as sp]
          [com.fulcrologic.fulcro.algorithms.tempid :as tempid]))

(defn get-schema [crd]
  (-> crd (:spec) (:versions) (first) (:schema) (:openAPIV3Schema) (:properties) (:spec)))

(defn keep-required [schema-spec]
  )

(defn parse-cr [cr]
  {:cre/keyword 
   :cre/required []
   :cre/properties []
   :cre/type #{:object :string :float64}
   :cre/description
   })

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


(s/def :cr/property (s/keys :req-un [:property/id :property/name :property/type]
                            :opt-un [:property/required :property/items :property/format :property/properties]))

(def test-prop {:id (tempid/tempid)
                :name :asd
                :description "Factor is a factor to multiply the base duration after each failed retry",
                :properties [{:id (tempid/tempid) :name :bastu :type "integer"}]
                :type "integer",
                :format "int64"})

(s/conform :cr/property test-prop)

(defn convert-to-vec [m]
  (let [k (keys m)
        v (vals m)]
   (mapv
    #(conj (nth v %) {:id (tempid/tempid) :name (nth k %)}) (range (count k)))))

;; (convert-to-vec {:a {:b 1}
;;                  :s {:c 2}})

;; spec

(defn parse-cr [e]
  (condp = (type e)
    clojure.lang.PersistentVector (mapv parse-cr e)
    clojure.lang.PersistentArrayMap (do (println (keys e)) (parse-cr (convert-to-vec e)))
    clojure.lang.PersistentHashMap (do (println (keys e)) (parse-cr (convert-to-vec e)))
    ;java.lang.String 
    ;java.lang.Long st
    nil))


(defn parse-cr [e & name]
  (let [property
        {:id (tempid/tempid)
         :name (or name "spec")
         :type (:type e)
         :description (:description e)
         :required (:required e)}]
   (if (contains? e :properties)
    (let [k (keys (:properties e))
          v (vals (:properties e))]
      (conj property {:properties (mapv #(parse-cr (nth v %) (nth k %)) (range (count k)))}))
      property)))



;; (s/def :spec/id tempid/tempid?)
;; (s/def :spec/name #(= % :spec))
;; (s/def :spec/description string?)

;; (s/def :cr/spec (s/keys :req-un [:spec/id :spec/name :/type]
;;                                   :opt-un [:]))

;(s/def :cr/cr ())



;; (sp/infer-specs (flatten (get-schema (k8s/get-crd k8s/crds "Application"))) :cre)

;; (filter #(keyword (first %)) :properties )
