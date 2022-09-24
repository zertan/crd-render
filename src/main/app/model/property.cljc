(ns app.model.property
  (:require
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   #?(:clj
      [app.model.database :as db])
   #?(:clj
      [app.util.k8s :as k8s :refer [oc]])
   #?(:clj
      [datascript.core :as d])
    [taoensso.timbre :as log]
    [com.wsscode.pathom.connect :as pc]
    #?(:cljs [com.fulcrologic.fulcro.mutations :as m])
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    #?(:clj
       [com.fulcrologic.fulcro.algorithms.server-render :as server-render])
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div]])
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item ui-icon ui-popup ui-grid ui-grid-column ui-grid-row]]))

(declare ui-property)

(defsc Property [this {:keys [:property/id :property/properties :property/name :property/type :property/description :property/items :property/required :property/reference] :as props}]
  {:query         (fn [] [:property/id
                          :property/name
                          :property/type
                          {:property/reference [:property/id]}
                          {:property/items '...}
                          :property/description
                          :property/required
                          {:property/properties '...}])
   :ident         :property/id
   :initial-state (fn [{:keys [id name type description properties]}] {:property/id (or id 0)
                                                                       :property/name ""
                                                                       :property/type ""
                                                                       :property/required false

                                                                       :property/items []
                                                                       :property/description ""
                                                                 
                                                                       :property/properties properties})
   :initLocalState (fn [this props]
                     {:open? false})}

  (let [c  (comp/get-computed this)]
  (dom/div
  (dom/span
   (if properties
   (ui-button {:icon true
               :inverted true
               :size :tiny
               :onClick (fn [e]
                          (comp/set-state! this {:open? (not (comp/get-state this :open?))}))}
              (ui-icon {:name (if (comp/get-state this :open?) "angle down" "angle right")})))
   (ui-popup {:content description
              :trigger 
    ;(dom/span (ui-divider {:vertical true}))
              (condp = type
    (dom/text {:style {:outline "none" :border "none" :margin-left (* 10 (:c c))}} 
       (str name " " type " " items)))})
   (when (and (not (empty? properties))
              (comp/get-state this :open?))
       (map #(ui-property % {:c (inc (:c c))}) (reverse properties)))))))

(def ui-property (comp/computed-factory Property {:keyfn :property/id}))

#?(:clj 
   (pc/defresolver get-property [{:keys [db] :as env} {:keys [property/id]}]
     {::pc/input #{:property/id}
      ::pc/output [:property/id :property/name 
                   {:property/properties [:property/id]}
                   :property/description :property/type {:property/items [:property/id]}
                   :property/required]}
     (d/pull db [:property/id :property/name :property/properties
                 :property/description :property/type :property/items
                 :property/required] [:property/id id]))
   :cljs
   (m/defmutation get-property [{:keys [:property/id]}]
     (action [{:keys [app state]}]
             (df/load! app [:property/id id] Property
                       ;{:target (targeting/replace-at [:component/id :main :main/property])}
                       ))))

#?(:clj
   (def resolvers [get-property]))
