(ns app.model.main
  (:require
   #?(:clj
      [app.model.mock-database :as db])
   #?(:clj
      [app.util.k8s :as k8s :refer [oc]])
    [taoensso.timbre :as log]
    [app.model.property :as property]
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div]])
    #?(:cljs [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item]])))


#?(:cljs
   (do
   (defsc Crd [this {:keys [:crd/id :crd/group :crd/top-property]}]
     {:query         [:crd/id :crd/group
                      {:crd/top-property (comp/get-query property/Property)}]
      :initial-state (fn [_] {:crd/group ""
                              :group/crds []})})

   (defsc Crds [this {:keys [:crd/group :group/crds] :as props}]
     {:query         [:crd/group :group/crds]
      :initial-state (fn [_] {:crd/group ""
                              :group/crds []})}
     (dom/text crds))

   (defsc Main [this {:keys [:main/property :main/crd-groups :main/crds] :as props}]
     {:query         [{:main/crds (comp/get-query Crds)}
                      :main/crd-groups
                      {:main/property (comp/get-query property/Property)}]
      :initial-state (fn [_] {:main/crds []
                              :main/crd-groups []
                              :main/property [(comp/get-initial-state property/Property)]})
      :initLocalState (fn [this props]
                        {:selected-group nil
                         :selected-crd nil})
      :ident         (fn [] [:component/id :main])
      :route-segment ["main"]
      :componentDidMount (fn [this]
                           (println "mountt")
                           (comp/transact! this `[(app.model.main/add-crd-groups! {})]))
      }
     (div :.ui.container
          (ui-dropdown {:placeholder "Select CustomResourceGroup ..."
                        :fluid true
                        :search true
                        :selection true
                        :options (mapv (fn [x] {:text x :value x :key x}) crd-groups)
                        :onChange (fn [e]
                                    (comp/set-state! this {:selected-group (str e.target.textContent)})
                                    (comp/transact! this `[(app.model.main/add-crds! {:crd/group ~(str e.target.textContent)})]))})
          (ui-dropdown {:placeholder "Select CustomResourceDefinition ..."
                        :fluid true
                        :selection true
                        :options (mapv (fn [x] {:text x :value x :key x}) (:group/crds crds))
                        :onChange (fn [e]
                                    (comp/set-state! this {:selected-crd (str e.target.textContent)})
                                    (comp/transact! this `[(app.model.property/get-crd! {:crd/id ~(str e.target.textContent)})])
                                    (println property)
                                    )})
          (dom/div
           (dom/text (str "apiVersion: " (comp/get-state this :selected-group) "/" (comp/get-state this :selected-crd)))
           (property/ui-property property {:c 0}))))

   (def ui-main (comp/computed-factory Main))))

#?(:clj
   (defresolver add-crd-groups! [env]
     {::pc/output [:main/crd-groups]}
     (log/info "add-crd-groups!")
     {:main/crd-groups (k8s/get-crd-groups k8s/crds)})

   ;; (defmutation add-crd-groups! [{:keys []}]
   ;;   {:main/crd-groups (k8s/get-crd-groups k8s/crds)})
   :cljs
   (defmutation add-crd-groups! [{:keys []}]
     (action [{:keys [app state]}]
             (df/load! app :main/crd-groups nil {:target (targeting/replace-at [:component/id :main :main/crd-groups])}))
     ;(remote [env] true)
     ))

#?(:clj
   (defresolver add-crds! [env {:keys [:crd/group]}]
     {::pc/input  #{:crd/group}
      ::pc/output [:crd/group :group/crds ;:crd/group :crd/id :crd/name
                   ]}
     (log/info "add-crds!")
     {:crd/group group :group/crds (k8s/get-crds-in-group k8s/crds group)})
   :cljs
   (defmutation add-crds! [{:keys [:crd/group]}]
     (action [{:keys [app state]}]
             (df/load! app [:crd/group group] Crds {:target (targeting/replace-at [:component/id :main :main/crds])}))
     ))

;; #?(:clj
;;    (defresolver get-required [env {:keys [:property/id]}]
;;      {::pc/input  #{:property/id}
;;       ::pc/output [:property/required :group/crds ;:crd/group :crd/id :crd/name
;;                    ]}
;;      (log/info "add-crds!")
;;      {:crd/group group :group/crds (k8s/get-crds-in-group k8s/crds group)})
;;    :cljs
;;    (defmutation add-crds! [{:keys [:crd/group]}]
;;      (action [{:keys [app state]}]
;;              (df/load! app [:crd/group group] Crds {:target (targeting/replace-at [:component/id :main :main/crds])}))
;;      ))



#?(:clj
   (def resolvers [add-crd-groups! add-crds!]))
