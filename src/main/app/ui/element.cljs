(ns app.ui.element
  (:require
   [app.ui.components :as my-comps]
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.dom :as dom]))

(declare ui-element)

(defsc Element [this {:keys [:element/id :element/elements :element/text] :as props}]
  {:query         (fn [p] [:element/id
                           :element/text
                           ;{:element/text (comp/get-query my-comps/Text)}
                           {:element/elements '...}
                           ])
   :ident         :element/id
   :initial-state (fn [{:keys [id text elements]}] (if elements (conj {:element/id (or id 0)
                                                                        :element/text ""
                                        ;:element/text (or text (comp/get-initial-state my-comps/Text))
                                                                        }
                                                                       {:element/elements elements})
{:element/id (or id 0)
                                                                        :element/text ""
                                        ;:element/text (or text (comp/get-initial-state my-comps/Text))
                                                                        }))
   :initLocalState (fn [this props]
                     {:open? false})}
  (let [c (comp/get-computed this)]
  (dom/div
   (dom/text
    {;:className "ui inverted blue button"
     :onClick (fn [e]
                (comp/set-state! this {:open? (not (comp/get-state this :open?))}))
     :style {:margin-left (* 10 (:c c))}}
    (str text))
   (when (and (not (empty? elements))
              (comp/get-state this :open?))
       (map #(ui-element % {:c (inc (:c c))}) (reverse elements)))
   )))

(def ui-element (comp/computed-factory Element {:keyfn :element/id}))
