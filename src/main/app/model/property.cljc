(ns app.model.property
  (:require
   [app.util :as util]
   ;[app.application :refer [SPA]]
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

(declare copy-property)

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
   :initial-state (fn [{:keys [id name type reference
                               items description
                               required properties]}] {:property/id (or id (tempid/tempid))
                                                       :property/name (or name "new-name")
                                                       :property/type (or type "object")
                                                       :property/items (or items [])
                                                       :property/description ""
                                                       :property/required (or required nil)
                                                       :property/properties (or properties [])})
   :initLocalState (fn [this props]
                     {:open? false
                      :show-dropdown? false})}

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
       (str name ": " " " items)))})
   (if (:property/properties reference)
     (ui-button {:icon true
               :inverted true
               :size :tiny
               :onClick (fn [e]
                          (comp/set-state! this {:show-dropdown true})
                          )}
              (ui-icon {:name "plus"}))
     (if (comp/get-state this :show-dropdown?)
       (ui-dropdown {:placeholder "Property ..."
                     :fluid true
                     :search true
                     :selection true
                     :options (mapv (fn [x] {:text (str (:property/name x) " " (:property/type x)) :value (:property/id x) :key (:property/name x)}) (:property/properties reference))
                     :onChange (fn [e]
                             (comp/transact! this `[(model.app.property/add-property! ~{:property (copy-property (first (filterv #(= (:property/id %)  (util/get-value e)) reference)))
                                                                                        :append [:property/id id :property/properties]})])
                             (comp/set-state! this {:show-dropdown? false})
                             )})))
   (when (and (not (empty? properties))
              (comp/get-state this :open?))
       (map #(ui-property % {:c (inc (:c c))}) (reverse properties)))))))

(def ui-property (comp/computed-factory Property {:keyfn :property/id}))

#?(:cljs
   (do
   ;; (defn copy-property-id [id]
   ;;   (let [property (get-in SPA [:property/id id])]
   ;;    (if-let [p (get property :property/properties)]
   ;;      (map copy-property p)
   ;;      (comp/get-initial-state Property (assoc-in property [:property/id] (tempid/tempid))))))
))

(defn filter-r [property filter-req]
  (vec (remove nil? (mapv (fn [x] (first (filterv (fn [y] (= (:property/name y) x)) (:property/properties property)))) filter-req))))

(defn copy-property [property & filter-req]
  (-> property
      (assoc :property/id (tempid/uuid))
      (assoc :property/reference [:property/id (:property/id property)])
      (assoc :property/properties (mapv #(copy-property %)
                                        (if (first filter-req)
                                            (filter-r property (first filter-req))
                                            (:property/properties property))))))

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
   (pc/defmutation add-property! [{:keys [db] :as env} {:keys [property & more :as opts]}]
     {}
     (let [res (d/transact db/conn property)]
     ;{tempid res}
     {}
))
   :cljs
   (m/defmutation add-property! [{:keys [property & more :as opts]}]
     (action [{:keys [app state]}]
             (apply (partial (merge/merge-component! app Property (comp/get-initial-state Property property)))
                    (flatten (into '() more)))
             )))

#?(:clj
   (def resolvers [get-property add-property!]))
