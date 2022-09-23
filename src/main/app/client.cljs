(ns app.client
  (:require
    [app.application :refer [SPA]]
    [app.ui.root :as root]
    [app.model.property :as property]
    [app.model.main :as main]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro-css.css-injection :as cssi]
    [taoensso.timbre :as log]
    [com.fulcrologic.fulcro.algorithms.denormalize :as fdn]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.inspect.inspect-client :as inspect]
    [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(defn ^:export refresh []
  (log/info "Hot code Remount")
  (cssi/upsert-css "componentcss" {:component root/Root})
  (app/mount! SPA root/Root "app"))

(defn ^:export init []
  (log/info "Application starting.")
  (cssi/upsert-css "componentcss" {:component root/Root})
  ;(inspect/app-started! SPA)
  (app/set-root! SPA root/Root {:initialize-state? true})
  (dr/initialize! SPA)
  ;; (log/info "Starting session machine.")
  ;; (uism/begin! SPA session/session-machine ::session/session
  ;;   {:actor/login-form      root/Login
  ;;    :actor/current-session root/Session})
  (app/mount! SPA root/Root "app" {:initialize-state? false}))

;; (comment
;;   (inspect/app-started! SPA)
;;   (app/mounted? SPA)
;;   (app/set-root! SPA root/Root {:initialize-state? true})
;;   (uism/begin! SPA session/session-machine ::session/session
;;     {:actor/login-form      root/Login
;;      :actor/current-session root/Session})

;;   (reset! (::app/state-atom SPA) {})

;;   (comp/transact! SPA [`(app.model.txt/add-txt! {:url "https://raw.githubusercontent.com/keycloak/keycloak-operator/main/deploy/crds/keycloak.org_keycloakrealms_crd.yaml"})])
;;   (comp/transact! SPA [`(app.model.element/add-top-element! {:url "https://raw.githubusercontent.com/keycloak/keycloak-operator/main/deploy/crds/keycloak.org_keycloakrealms_crd.yaml" :tempid (tempid/tempid)})])

;;   (comp/get-query root/Settings (app/current-state SPA))

;;   (tap> SPA)
;;   (com.fulcrologic.fulcro.algorithms.indexing/reindex)

;;   (merge/merge-component! SPA root/Settings {:account/time-zone "America/Los_Angeles"
;;                                              :account/real-name "Joe Schmoe"})
;;   (dr/initialize! SPA)
;;   (app/current-state SPA)
;;   (dr/change-route SPA ["settings"])
;;   (app/mount! SPA root/Root "app")
;;   (comp/get-query root/Root {})
;;   (comp/get-query root/Root (app/current-state SPA))

;; ;;;;;;;;;;
;; (comp/transact! SPA `[(app.model.property/get-crd! {:crd/id "APIManager"})])

;; (comp/transact! SPA `[(app.model.property/get-property {:property/id #uuid "4ee79b45-08c7-4054-9a4b-05799e1c41d9"})])
;; ;;;;;;;;;


;; (df/load! SPA :main/element app.ui.element/Element)

;; (let [s (app/current-state SPA)]
;;   (fdn/db->tree [{ :main/element [ :element/id { :element/text [ :txt/id :txt/text ] } { :element/elements '... } ] }] {} s))

;; (let [s (app/current-state SPA)]
;;   (fdn/db->tree [{ :main/element [ :element/id { :element/text [ :txt/id :txt/text ] } ] }] (comp/get-initial-state app.ui.root/Root {}) s))

;;   (-> SPA ::app/runtime-atom deref ::app/indexes)
;;   (comp/class->any SPA root/Root)
;;   (let [s (app/current-state SPA)]
;;     (fdn/db->tree [{[:component/id :login] [:ui/open? :ui/error :account/email
;;                                             {[:root/current-session '_] (comp/get-query root/Session)}
;;                                             [::uism/asm-id ::session/session]]}] {} s)))
