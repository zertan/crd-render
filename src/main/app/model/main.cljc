(ns app.model.main
  (:require
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   #?(:clj
      [app.model.database :as db])
   #?(:clj
      [datascript.core :as d])
   #?(:clj
      [app.util.k8s :as k8s :refer [oc]])
   #?(:clj
      [clojure.string])
    [taoensso.timbre :as log]
    [app.model.property :as property]
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    #?(:cljs [com.fulcrologic.fulcro.mutations :as m])
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [app.util.parse-crds :as parse-crds])
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom :refer [div]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div]])
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item]]))


#?(:cljs
   (do
   (defsc Crd [this {:keys [:crd/id :crd/group :crd/version :crd/property]}]
     {:ident :crd/id
      :query         [:crd/id :crd/group
                      :crd/version
                      {:crd/property (comp/get-query property/Property)}]
      :initial-state (fn [_] {:crd/id "RandomThing"
                              :crd/group "random.io"
                              :crd/version "v1"
                              :crd/property (comp/get-initial-state property/Property)})})

   (defsc CrdGroup [this {:keys [:crd-group/id :crd-group/crds] :as props}]
     {:ident         :crd-group/id
      :query         [:crd-group/id
                      :crd-group/crds]
      :initial-state (fn [_] {:crd-group/id "random.io"
                              :group/crds []})}
     (dom/text crds))

   (defsc Cr [this {:keys [:cr/id :cr/metadata :cr/crd :cr/property]}]
     {:ident         :cr/id
      :query         [:cr/id
                      {:cr/metadata (comp/get-query property/Property)}
                      {:cr/crd (comp/get-query Crd)}
                      {:cr/property (comp/get-query property/Property)}]
      :initial-state (fn [id] {:cr/id (or id 0)
                              :cr/metadata (comp/get-initial-state property/Property)
                              :cr/crd (comp/get-initial-state Crd)
                              :cr/property (comp/get-initial-state property/Property)})}
     (dom/div
      (dom/text (str "apiVersion: " (get crd :version) "/" (get crd :id)))
      (map #(property/ui-property % {:c 0}) metadata)
      (map #(property/ui-property % {:c 0}) property)))

   (def ui-cr (comp/computed-factory Cr))

   (defsc Main [this {:keys [:main/cr :main/crd-groups] :as props}]
     {:query         [{:main/crd-groups (comp/get-query CrdGroup)}
                      {:main/cr (comp/get-query Cr)}]
      :initial-state (fn [_] {:main/crd-groups []
                              :main/cr (comp/get-initial-state Cr)})
      :initLocalState (fn [this props]
                        {:selected-group nil
                         :selected-crd nil})
      :ident         (fn [] [:component/id :main])
      :route-segment ["main"]
      :componentDidMount (fn [this]
                           (comp/transact! this `[(app.model.main/add-crd-groups! {})]))}
     (div :.ui.container
          (ui-dropdown {:placeholder "Select CustomResourceGroup ..."
                        :fluid true
                        :search true
                        :selection true
                        :options (mapv (fn [x] (let [crdg (:crd-group/id x)] {:text crdg :value crdg :key crdg})) crd-groups)
                        :onChange (fn [e]
                                    (comp/set-state! this {:selected-group (str e.target.textContent)}))})
          (ui-dropdown {:placeholder "Select CustomResourceDefinition ..."
                        :fluid true
                        :selection true
                        :options (mapv (fn [x] {:text x :value x :key x}) (:crd-group/crds (first (filter (fn [x] (= (:crd-group/id x) (comp/get-state this :selected-group))) crd-groups))))
                        :onChange (fn [e]
                                    (comp/set-state! this {:selected-crd (str e.target.textContent)})
                                    (comp/transact! this `[(app.model.main/add-crd! ~{:crd/id (str (comp/get-state this :selected-group) "/" e.target.textContent)})]))})
          (div
           (ui-menu {:attached "top"}
                    (ui-button {:icon "plus"
                                :item true
                                :simple true
                                :size :small
                                :onClick (fn [e] (comp/transact! this `[(app.model.main/add-cr! ~{:crd/id (str (comp/get-state this :selected-group) "/" e.target.textContent)})]))}))
           
           (if cr
            (ui-cr cr)))))

(def ui-main (comp/computed-factory Main))))

#?(:clj
   (pc/defresolver get-crd-group [{:keys [db] :as env} {:keys [:crd-group/id]}]
     {::pc/input  #{:crd-group/id}
      ::pc/output [:crd-group/id :crd-group/crds]}
     (d/pull @db/conn '[:crd-group/id :crd-group/crds] [:crd-group/id id])))

#?(:clj
   (do
   (pc/defresolver get-crd-groups [{:keys [db] :as env} _]
     {::pc/output [{:main/crd-groups [:crd-group/id]}]}
     (log/debug "get-crd-groups")
     (let [req (d/q '[:find ?id
                      :where [?c :crd-group/id ?id]]
                 @db/conn)]
        {:main/crd-groups (mapv (fn [x] [ :crd-group/id (first x)]) req)}))
   
   (pc/defmutation add-crd-groups! [{:keys [db] :as env} _]
     {}
     (let [groups (k8s/create-crd-groups k8s/crds)
           ids (mapv (fn [x] [:crd-group/id (:crd-group/id x)]) groups)]
     (d/transact! db/conn groups))
     {}))

    :cljs
     (m/defmutation add-crd-groups! [{:keys []}]
       (action [{:keys [app state]}]
             ;(df/load! app :main/crd-groups nil {:target (targeting/replace-at [:component/id :main :main/crd-groups])})
             true)
       (remote [env]
               true
         ;; (-> env
         ;;     (m/with-target [:component/id :main :main/crd-groups])
         ;;     ;(m/returning CrdGroup)
         ;;     )
         )
       (ok-action [{:keys [app state result]}]
         (df/load! app :main/crd-groups CrdGroup {:target (targeting/replace-at [:component/id :main :main/crd-groups])}))
       ))

#?(:clj
   (pc/defresolver get-crd-group [{:keys [db] :as env} {:keys [:crd-group/id]}]
     {::pc/input  #{:crd-group/id}
      ::pc/output [:crd-group/id :crd-group/crds]}
     (d/pull @db/conn '[:crd-group/id :crd-group/crds] [:crd-group/id id])))


;; #?(:clj
;;    (pc/defresolver add-crds! [env {:keys [:crd-group/id]}]
;;      {::pc/input  #{:crd-group/id}
;;       ::pc/output [:crd-group/id :crd-group/crds ;:crd/group :crd/id :crd/name
;;                    ]}
;;      (log/info "add-crds!")
;;      {:crd-group/id id :crd-group/crds (k8s/get-crds-in-group k8s/crds id)})
;;    :cljs
;;    (m/defmutation add-crds! [{:keys [:crd-group/id]}]
;;      (action [{:keys [app state]}]
;;              (df/load! app [:crd-group/id id] CrdGroup {:target (targeting/replace-at [:component/id :main :main/crd-group])}))))


#?(:clj
   (pc/defmutation add-crd! [{:keys [db] :as env} {:keys [:crd/id]}]
     {::pc/input #{:crd/id}}
     (let [ng (clojure.string/split id #"/")
          crd-temp (k8s/get-crd k8s/crds (first ng) (second ng))]
     (d/transact db/conn [{:crd/id id
                           :crd/version (parse-crds/get-version-name crd-temp)
                           :crd/property (parse-crds/parse-cr (parse-crds/get-schema crd-temp) {:name :spec})}]))
     {})

   :cljs
   (m/defmutation add-crd! [{:keys [:crd/id]}]
     (action [{:keys [app state]}] true)
     (remote [env] true)
     (ok-action [{:keys [app state result]}]
                (comp/transact! app `[(app.model.main/create-cr! ~{:crd/id id})]))))

#?(:clj
   (pc/defmutation add-cr! [{:keys [db] :as env} {:keys [:crd/id]}]
     {::pc/input #{:crd/id}
      ;::pc/output []
      }
     (let [required (d/q '[:find ?r
                           :where [?c :crd/id id]
                           [?c :crd/property ?p]
                           [?p :property/required ?r]]
                         @db/conn)
           req-prop-ids (d/q [:find '?id
                              :where ['?c :crd/id id]
                              ['?c :crd/property '?p]
                              ['?p :property/properties '?p2]
                              (into (map (fn [x] ['?p2 :property/name (keyword (first x))]) (first (vec req))) '(or))
                              ['?p2 :property/id '?id]]
                             @db/conn)]
     (d/transact db/conn [{:cr/id (tempid/tempid)
                           :cr/metadata [{:property/id (tempid/tempid)
                                          :property/name :my-cr}]
                           :cr/crd [:crd/id id]
                           :cr/property (map copy-property req-prop-ids)}]))
     {}))


;; we should propably do this in a client side mutation merge/comp
(defn copy-property [{:keys [:property/id]}]
  
  )

#?(:clj 
   (pc/defmutation get-cr [{:keys [db] :as env} {:keys [:cr/id]}]
     {::pc/input #{:cr/id}
      ::pc/output [:cr/id
                   {:cr/metadata [:property/id]}
                   {:cr/crd [:crd/id]} {:cr/property [:property/id]}]}
     (d/pull db/conn [:cr/id :cr/metadata :cr/crd :cr/property] [:cr/id id]))
   :cljs
   (m/defmutation get-cr [{:keys [:property/id]}]
     (action [{:keys [app state]}]
             (merge/merge-component! app Cr (comp/get-initial-state Cr (tempid/tempid))
                                     :replace [:component/id :main :main/cr])
             ;; (df/load! app [:crd/id id] Cr
             ;;           {:target (targeting/replace-at [:component/id :main :main/cr])})
             true)))
 

#?(:clj
   (pc/defresolver get-cr! [{:keys [db] :as env} {:keys [:crd/id :crd-group/id]}]
     {::pc/input  #{:crd/id}
      ::pc/output [:property/id]}
     (let [req (d/q '[:find ?r
                      :where [?c :crd/id id]
                             [?c :crd/property ?p]
                             [?p :property/required ?r]]
                    @db/conn)
           ])
     {})
   :cljs
   (m/defmutation get-cr! [{:keys [:crd/id]}]
     (action [{:keys [app state]}]
             ;(df/load! app [:crd/group group] CrdGroups {:target (targeting/replace-at [:component/id :main :main/crds])})
             true)))



#?(:clj
   (def resolvers [add-crd-groups! add-crd! get-cr get-cr! get-crd-groups get-crd-group]))
