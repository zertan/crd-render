(ns app.model.element
  (:require
    [app.ui.element :as elem]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]))

;; (defn txt-path
;;   "Normalized path to a user entity or field in Fulcro state-map"
;;   ([id] [:txt/id id]))

;; (defn insert-txt*
;;   "Insert a user into the correct table of the Fulcro state-map database."
;;   [state-map {:keys [:txt/id] :as txt}]
;;   (assoc-in state-map (txt-path id) txt))



;; (df/load! app.application/SPA :main/element app.ui.element/Element {
;;                                                               :target (targeting/append-to [:main/element])
;;                                         })

(defmutation add-top-element!
  "Client Mutation: Upsert a user (full-stack. see CLJ version for server-side)."
  [{:keys [url tempid]}]
  (action [{:keys [app state]}]
          (println "yes")
          (df/load! app :main/element app.ui.element/Element {
                                                              :target (targeting/replace-at :main/element)
                                        })
          ;(merge/merge-component! app element/Element (comp/get-initial-state element/Element {:id tempid :text "abcd" :elements []}))
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
  ;; (remote [env]
  ;;         (-> env
  ;;             (m/returning 'app.ui.element/Element)
  ;;             (m/with-target (targeting/append-to [:main/element]))
  ;;             ))
)
