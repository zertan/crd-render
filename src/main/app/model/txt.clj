(ns app.model.txt
  (:require
    [app.model.mock-database :as db]
    [datascript.core :as d]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
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

(defonce txt-database (atom {}))

(defresolver txt-resolver [env {:keys [id]}]
  {::pc/input  #{:txt/id}
   ::pc/output [:txt/text]}
  {:txt/text
   (get @txt-database id)})

(defmutation add-txt! [env {:keys [url]}]
  {}
  (do
  (println (str "add: " (slurp url)))
  (swap! update txt-database conj {:txt/id 1 :txt/text (slurp url)}))
  {})

(def resolvers [txt-resolver add-txt!])
