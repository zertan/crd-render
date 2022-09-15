(ns app.ui.element
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    #?(:clj [com.fulcrologic.fulcro.dom-server :as dom]
       :cljs [com.fulcrologic.fulcro.dom :as dom])
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item ui-text-area ui-icon ui-input ui-divider]]))

(declare ui-element)

(defsc Element [this {:keys [:element/id :element/elements :element/text] :as props}]
  {:query         (fn [] [:element/id
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
  (dom/span
   (if elements
   (ui-button {:icon true
               :inverted true
               :size :tiny
               :onClick (fn [e]
                          (comp/set-state! this {:open? (not (comp/get-state this :open?))}))}
              (ui-icon {:name (if (comp/get-state this :open?) "angle down" "angle right")})))
   ;(dom/span (ui-divider {:vertical true}))
   (condp = text
    ;":type" nil;(dom/text (str elements))
    ;":description" (dom/span text)
    (dom/text
     {;:className "ui inverted blue button"
      ;:classNmae "ui input"
      ;; :className (if (= text ":description")
      ;;                "ui inverted green button";(get elements :element/text)
      ;;                "ui inverted blue button")
     :style {:outline "none"
             :border "none"
             ;:width "80%"
             :margin-left (* 10 (:c c))}
      ;:value text}
    } 
     (str text)
     ))
   (when (and (not (empty? elements))
              (comp/get-state this :open?))
       (map #(ui-element % {:c (inc (:c c))}) (reverse elements)))
   ))))

(def ui-element (comp/computed-factory Element {:keyfn :element/id}))

