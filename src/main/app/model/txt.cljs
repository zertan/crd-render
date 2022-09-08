(ns app.model.txt
  (:require
    [app.ui.components :as my-comps]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp]))

;; (defn txt-path
;;   "Normalized path to a user entity or field in Fulcro state-map"
;;   ([id] [:txt/id id]))

;; (defn insert-txt*
;;   "Insert a user into the correct table of the Fulcro state-map database."
;;   [state-map {:keys [:txt/id] :as txt}]
;;   (assoc-in state-map (txt-path id) txt))

(defmutation add-txt!
  "Client Mutation: Upsert a user (full-stack. see CLJ version for server-side)."
  [{:keys [url]}]
  (action [{:keys [app state]}]
          (merge/merge-component! app my-comps/Text (comp/get-initial-state my-comps/Text {:id 1 :text "abcd"}))
          )
  ;; (action [{:keys [state]}]
  ;;   (log/info "Upsert txt action")
  ;;   (swap! state (fn [s]
  ;;                  (-> s
  ;;                    (insert-txt* params)
  ;;                    (targeting/integrate-ident* [:txt/id id] :append [:all-txts])))))
  ;; (ok-action [env]
  ;;   (log/info "OK action"))
  ;; (error-action [env]
  ;;   (log/info "Error action"))
  (remote [env]
          true
    ;; (-> env
    ;;   (m/returning 'app.ui.root/Txt)
    ;;   (m/with-target (targeting/append-to [:all-txts]))
      ))

