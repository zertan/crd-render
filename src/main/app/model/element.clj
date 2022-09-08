(ns app.model.element
  (:require
   [app.model.mock-database :as dbs :refer [db]]
    [datascript.core :as d]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))


;; (>defn all-txt-ids
;;   "Returns a sequence of UUIDs for all of the active accounts in the system"
;;   [db]
;;   [any? => (s/coll-of uuid? :kind vector?)]
;;   (d/q '[:find [?v ...]
;;          :where
;;          [?e :txt/id ?v]]
;;     db))

;; (defresolver all-txts-resolver [{:keys [db]} input]
;;   {;;GIVEN nothing (e.g. this is usable as a root query)
;;    ;; I can output all accounts. NOTE: only ID is needed...other resolvers resolve the rest
;;    ::pc/output [{:all-txts [:txt/id]}]}
;;   {:all-txts (mapv
;;                    (fn [id] {:txt/id id})
;;                    (all-txt-ids db))})

;; (>defn get-txt [db id subquery]
;;   [any? uuid? vector? => (? map?)]
;;   (d/pull db subquery [:txt/id id]))

;(defonce txt-database (atom {}))

(defresolver element-resolver [env {:keys [element/id]}]
  {::pc/input  #{:element/id}
   ::pc/output [:element/id :element/text :element/elements]}
  (let [r
        (get-in db [:element/id id])
        ;; (->> (get-in db [:element/id 0])
        ;;     (mapv (fn [id] {:user/id (first id)}) ))
        ]
    (log/info "called e")
    ;{:element/id id :element/text (get-in dbs/st-db )}
  r))

;; (defmutation add-top-element! [env]
;;   {;::pc/input [:main/element]
;;    ::pc/output [:main/element]}
;;   (log/info "asd")
;;   {:main/element [:element/id 0]})

(defresolver add-top-element! [env]
  {;::pc/input [:main/element]
   ::pc/output [{:main/element [:element/id]}]}
  (log/info "asd")
  ;{:main/element {:element/id 0}}
  {:main/element dbs/st-db}
  )

(defresolver add-top-element! [env]
  {;::pc/input [:main/element]
   ::pc/output [:main/element]}
  (log/info "m")
  ;{:main/element {:element/id 0}}
  {:main/element dbs/st-db}
  )

(def resolvers [add-top-element! element-resolver])
