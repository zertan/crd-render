(ns app.model.property
  (:require
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   #?(:clj
      [app.model.mock-database :as db])
   #?(:clj
      [app.util.k8s :as k8s :refer [oc]])
   #?(:clj
      [datascript.core :as d])
    [taoensso.timbre :as log]
    [app.ui.element :as element]
    [com.wsscode.pathom.connect :as pc]
    #?(:cljs [com.fulcrologic.fulcro.mutations :as m])
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    #?(:clj
       [com.fulcrologic.fulcro.algorithms.server-render :as server-render])
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [app.util.parse-crds :as parse-crds])
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div]])
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item ui-icon ui-popup ui-grid ui-grid-column ui-grid-row]]))

(declare ui-property)

(defsc Property [this {:keys [:property/id :property/properties :property/name :property/type :property/description :property/items :property/required] :as props}]
  {:query         (fn [] [:property/id
                          :property/name
                          :property/type               
                          :property/items
                          :property/description
                          :property/required
                          {:property/properties '...}
                          ])
   :ident         :property/id
   :initial-state (fn [{:keys [id name type description properties]}] {:property/id (or id 0)
                                                                       :property/name ""
                                                                       :property/type ""
                                                                       :property/required false

                                                                       :property/items []
                                                                       :property/description ""
                                                                 
      :property/properties properties}

)
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
       (map #(ui-property % {:c (inc (:c c))}) (reverse properties)))
   ))))

(def ui-property (comp/computed-factory Property {:keyfn :property/id}))

;; #?(:clj
;;    (do
;;      (def db #:crd{:id "Keycloak" :description "Scheme" :type "object" :property []})
;;      (def db-r (server-render/build-initial-state #:property{:id :xyz :properties [(parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds "Keycloak")) {:name :spec})]} Property))))

#?(:clj
   (pc/defmutation get-crd! [{:keys [db] :as env} {:keys [crd/id]}]
     {::pc/input #{:crd/id}
      ::pc/output [:property/id :property/name 
                   {:property/properties [:property/id]}
                   :property/description :property/type :property/items
                   :property/required]}
     (log/info "load crd: " id)
     (let [db-r (server-render/build-initial-state {:property/id "blah" :property/properties (parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds id)) {:name :spec})} Property)]
(d/transact db/conn (vec (vals (:property/id db-r))))
     (println {:main/property (:property/properties db-r)})
     (:property/properties db-r)
                    ))
   :cljs
   (m/defmutation get-crd! [{:keys [:crd/id]}]
     (action [{:keys [app state]}] true)
     (remote [env] 
             (-> env
                 (m/with-target [:component/id :main :main/property])
                 (m/returning Property)
      ))
     (ok-action [{:keys [app state result]}]
                (comp/transact! app `[(app.model.property/get-property ~(get-in result [:body 'app.model.property/get-crd!]))]))
     ))
   
#?(:clj 
   (pc/defresolver get-property [{:keys [db] :as env} {:keys [property/id]}]
     {::pc/input #{:property/id}
      ::pc/output [:property/id :property/name 
                   {:property/properties [:property/id]}
                   :property/description :property/type :property/items
                   :property/required]}
     ;(log/info "id: " id)
     (d/pull db [:property/id :property/name :property/properties
                 :property/description :property/type :property/items
                 :property/required] [:property/id id]))
   :cljs
   (m/defmutation get-property [{:keys [:property/id]}]
     (action [{:keys [app state]}]
             (df/load! app [:property/id id] Property
                       {:target (targeting/replace-at [:component/id :main :main/property])}))))

#?(:clj
   (def resolvers [get-property get-crd!]))


(comment

(d/transact! db/conn [[:db/add eid :aliases ["X" "Y" "Z"]]])

 (d/pull @db/conn [:property/id] [:property/id 0])

(d/q '[:find ?id
       :where [?p :property/id ?id]]
     @db/conn)

(d/q '[:find  ?r, ?t
       :where [?p :property/id #uuid "51a40c81-853f-443f-86f1-c38f6d46b0a5"]
       [?p :property/name :spec]
       [?p :property/required ?r]
       [?p :property/type ?t]
              ;[?p :property/required true]
       ]
     @db/conn)

(d/q '[:find ?p, ?r ;?id
       :where [?c :crd/id "APIManager"]
       [?c :crd/property ?p]
       ;[?p :property/name ?na]
       [?p :property/required ?r]
       ;[?p :property/properties ?ps]
       ;[?ps :property/properties ?pss]
       ;[?pss :property/required ?r]
       ;[?p :property/type ?t]
              ;[?p :property/required true]
       ]
     @db/conn)

(d/q '[:find ?p
       :where [?c :crd/id "APIManager"]
       ;[?c :crd/property ?p]
       ;[?p :property/name ?na]
       [?p :property/name :wildcardDomain]
       ;[?p :property/properties ?ps]
       ;[?ps :property/properties ?pss]
       ;[?pss :property/required ?r]
       ;[?p :property/type ?t]
              ;[?p :property/required true]
       ]
     @db/conn)

; (d/pull @db/conn '[:property/_properties {:property/_id [:db/id]}] #uuid "73756528-dbe6-4105-8b1a-2e39c504ee8d")

; (d/pull @db/conn '[:property/_properties {:property/_id [:db/id]}] #uuid "73756528-dbe6-4105-8b1a-2e39c504ee8d")

(d/pull @db/conn '[{:property/properties [:property/id]}] (:crd/property (d/pull @db/conn '[:crd/property] [:crd/id "APIManager"])))

(d/pull @db/conn '[:crd/property] [:crd/id "APIManager"])

(def parsed-c (parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds "Keycloak")) {:name :spec}))

;(def db-crd (server-render/build-initial-state {:property/id "blah" :property/properties (parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds "Keycloak")) {:name :spec})} Property))

;(d/transact db/conn (vec (vals (:property/id db-t))))

;(d/transact db/conn  (vec (vals (:property/id db-crd))))

(let [crd (parse-crds/build-crd-data "APIManager" Property)]
(d/transact db/conn (:properties crd))
(d/transact db/conn [(:crd crd)]))

 (d/transact db/conn [ {:property/id 1 :property/name :crab}])

(let [parsed ])

(d/transact db/conn [{:crd/id "APIManager"
                      :crd/property (parse-crds/parse-cr (parse-crds/get-schema (k8s/get-crd k8s/crds "APIManager")) {:name :spec})}])

(def test-prop #:property{:id (tempid/uuid) :name :blah :properties [ #:property{:id (tempid/uuid)
                                                                                  :name :asd
                                                                                  :description "Factor is a factor to multiply the base duration after each failed retry",
                                                                                  :properties [#:property{:id (tempid/uuid) :name :bastu :type "integer"}]
                                                                                  :type "integer",
                                                                                  :format "int64"
                
                                                                                  }]})

:property/id :property/properties :property/name :property/type :property/description :property/items :property/required
(def db-t (server-render/build-initial-state test-prop Property))

)
