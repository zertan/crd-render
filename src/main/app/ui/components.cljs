(ns app.ui.components
  (:require
    [com.fulcrologic.fulcro.components :as prim :refer [defsc]]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.dom :as dom]))

(defsc PlaceholderImage
  "Generates an SVG image placeholder of the given size and with the given label
  (defaults to showing 'w x h'.

  ```
  (ui-placeholder {:w 50 :h 50 :label \"avatar\"})
  ```
  "
  [this {:keys [w h label]}]
  (let [label (or label (str w "x" h))]
    (dom/svg #js {:width w :height h}
      (dom/rect #js {:width w :height h :style #js {:fill        "rgb(200,200,200)"
                                                    :strokeWidth 2
                                                    :stroke      "black"}})
      (dom/text #js {:textAnchor "middle" :x (/ w 2) :y (/ h 2)} label))))

(def ui-placeholder (prim/factory PlaceholderImage))

(defsc Text [this {:keys [:txt/id :txt/text] :as props}]
  {:query         [:txt/id :txt/text]
   :ident         :txt/id
   :initial-state (fn [{:keys [id text]}] {:txt/id (or id 0)
                                            :txt/text (or text  "bla")})}
  (str text))

(def ui-text (comp/factory Text))
