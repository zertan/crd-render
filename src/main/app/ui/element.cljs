(ns app.ui.element
  (:require
   [app.ui.components :as my-comps]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.dom :as dom]))

(declare ui-element)

(defsc Element [this {:keys [:element/id :element/elements :element/text] :as props}]
  {:query         (fn [p] [:element/id
                           {:element/text (comp/get-query my-comps/Text)}
                           {:element/elements '...}
                           ])
   :ident         :element/id
   :initial-state (fn [{:keys [id text elements]}] {:element/id (or id 0)
                                                    :element/text (or text (comp/get-initial-state my-comps/Text))
                                                    :element/elements elements})
   :initLocalState (fn [this props]
                     {:open? true})}
  (dom/div
   (dom/button
    {:onClick (fn [e]
                (comp/set-state! this {:open? (not (comp/get-state this :open?))}))}
    (:txt/text text))
   (when (and (not (empty? elements))
              (comp/get-state this :open?))
       (map #(ui-element %) (reverse elements)))
   ))

(def ui-element (comp/computed-factory Element {:keyfn :element/id}))
