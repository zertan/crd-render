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
    [clojure.string]
    [taoensso.timbre :as log]
    [com.wsscode.pathom.connect :as pc]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    #?(:clj
       [com.fulcrologic.fulcro.algorithms.server-render :as server-render])
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div]])
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item ui-icon ui-popup ui-grid ui-grid-column ui-grid-row ui-input ui-checkbox]]))

(declare copy-property)

(declare Property)

(declare ui-property)

(defsc Property [this {:keys [:property/id :property/properties :property/name :property/type :property/description :property/items :property/required :property/reference :property/value] :as props}]
  {:query         (fn [] [:property/id
                          :property/name
                          :property/type
                          {:property/reference '...}
                          {:property/items '...}
                          :property/description
                          :property/required
                          {:property/properties '...}
                          :property/value])
   :ident         :property/id
   :initial-state (fn [property] {:property/id (or (:property/id property) (tempid/tempid))
                                  :property/name (or (:property/name property) "new-name")
                                  :property/type (or (:property/type property) "object")
                                  :property/items (or (:property/items property) [])
                                  :property/description (or (:property/description property) "")
                                  :property/required (or (:property/required property) [])
                                  :property/properties (or (:property/properties property) [])})
   :initLocalState (fn [this props]
                     {:open? false
                      :show-dropdown? false
                      :show-plus? false
                      :show-type? false})}

  (let [c  (comp/get-computed this :c)
        parent-type (comp/get-computed this :parent-type)]
  (dom/div
  (dom/span
   (if (or (= type "object") (= type "array"))
   (ui-icon {:name (if (comp/get-state this :open?) "angle down" "angle right")
             :link true
             :style {:position "absolute"
                     :left 0
                     :margin-top "2px"
                     :margin-left "0.5rem"}
             :onClick (fn [e]
                        (println "it: " items)
                          (comp/set-state! this {:open? (not (comp/get-state this :open?))}))}))
  (ui-popup {:content description
             :trigger 
             (dom/span
              (dom/text {:link "#"
                         :style {:margin-left "1rem" :white-space "pre"
                                 :outline "none" :border "none"}
                         :onMouseEnter (fn [e]
                                         (comp/set-state! this {:show-plus? true
                                                                :show-type? true}))
                         :onMouseLeave (fn [e]
                                         (comp/set-state! this {:show-type? false}))
                         :onClick (fn [e]
                                    (comp/set-state! this {:open? (not (comp/get-state this :open?))}))}
                        (let [margin (clojure.string/join (take (* 2 c) (repeat " ")))]
                                 (str margin (if (= parent-type "array") "- ") (clojure.string/replace-first (str name) #":" "") ": ")))
              (condp = type
                "integer" (dom/input {:style {:background-color "rgba(0, 0, 0, 0)"
                                              :font-family "'Roboto Mono', monospace"
                                              :color "#CCCCCC"
                                              :border "none"
                                              :outline "none"}})
                 "string" (dom/input {:style {:background-color "rgba(0, 0, 0, 0)"
                                              :font-family "'Roboto Mono', monospace"
                                              :color "#CCCCCC"
                                              :border "none"
                                              :outline "none"}
                                      :onFocus #(comp/set-state! this {:show-type? true})
                                      :onBlur #(comp/set-state! this {:show-type? false})})
                  "boolean" (ui-checkbox {:checked value
                                          :style {:top "3px"}
                                          :onChange (fn [e]
                                                      (m/toggle! this :property/value)
                                                      (println value))})
              nil)
              (if (comp/get-state this :show-type?) 
                (dom/text {:style {:color "#0f1330"
                                   :font-weight "bold"
                                   :position "absolute"
                                   :right "10rem"
                                   }} (str "(" type ")"))))})
  (let [it (if (:property/properties reference)
             (:property/properties reference)
             (:property/items reference))]
  (when (and (comp/get-state this :show-plus?) (not (empty? it)))
(dom/span
     (ui-icon {:name "plus"
               :link true
               :inverted true
               :size "small"
               :style {:margin-left "0.5rem"
                       :margin-bottom "2px"}
               :onClick (fn [e]
                          (println it)
                          (comp/set-state! this {:show-dropdown? true}))})
     (if (comp/get-state this :show-dropdown?)
      (ui-dropdown {:placeholder "Property ..."
                    :search true
                    :selection true
                    :onMouseLeave (fn [e]
                                         (comp/set-state! this {:show-dropdown? false}))
                    :style {:position "absolute"
                            :left "20rem"}
                    :options (mapv (fn [x] {:text (str (:property/name x) " " (:property/type x)) :value (:property/name x) :key (:property/name x)}) it)
                    :onChange (fn [e]
                                (let [k (keyword (clojure.string/replace-first (first (clojure.string/split (util/get-text e) #" ")) #":" ""))]
                                ;(println "it: " it)
                                ;(println "value: " )
 (println "copy: " (copy-property (first (filterv #(= (:property/name %) k) it))))
                                ;(println "b: " (first (filterv #(= (:property/name %) k) it)))
                                (comp/transact! this `[(app.model.property/add-property! ~{:property (copy-property (first (filterv #(= (:property/name %) k) it)))
                                                                                           :more id})]))
                                (comp/set-state! this {:show-dropdown? false})
                                )}))))))
  (when (and (not (empty? properties))
             (comp/get-state this :open?))
     (map #(ui-property % {:c (inc c)}) (reverse properties)))
  (when (and (not (empty? items))
             (comp/get-state this :open?))
     (ui-property items (if (= type "array") (conj {:parent-type "array"} {:c (inc (inc (:c c)))}) {:c (inc (:c c))}))
))))

(def ui-property (comp/computed-factory Property {:keyfn :property/id}))

#?(:cljs
   (do
   ;; (defn copy-property-id [id]
   ;;   (let [property (get-in SPA [:property/id id])]
   ;;    (if-let [p (get property :property/properties)]
   ;;      (map copy-property p)
   ;;      (comp/get-initial-state Property (assoc-in property [:property/id] (tempid/tempid))))))
))

(defn filter-r [properties filter-req]
  (vec (remove nil? (mapv (fn [x] (first (filterv (fn [y] (= (:property/name y) x)) properties))) filter-req))))

(defn copy-property [property & filter-req]
  (let [filter-req-2 (if filter-req (mapv keyword (:property/required property)))
        ;as(println filter-req)
        ;a (println filter-req-2)
        ;properties (:property/properties property)
        ;asd (println "a" (filter-r (:property/properties property) filter-req-2))
        p (-> property
              (assoc :property/id (tempid/uuid))
              (assoc :property/value "")
              (assoc :property/reference [:property/id (:property/id property)]))]
(if (:property/items p)
  (assoc p :property/items (copy-property (:property/items property))))
  (assoc p :property/properties (mapv #(copy-property % filter-req-2)
                                      (if filter-req-2
                                            (filter-r (:property/properties property) filter-req-2)
                                            (:property/properties property))))))

(defn crap [property & filter-req]
  (println property)
  (println filter-req))

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
             (println more)
             (merge/merge-component! app Property (comp/get-initial-state Property property)
                                     :append [:property/id more :property/properties]))))


             ;; (apply (partial (merge/merge-component! app Property (comp/get-initial-state Property property)))
             ;;        (flatten (into '() more)))
             ;; ))


#?(:clj
   (def resolvers [get-property add-property!]))
