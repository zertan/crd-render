(ns app.ui.crd
  (:require
    [app.model.session :as session]
    [app.model.txt :as txt]
    [app.ui.components :as my-comps]
    [app.ui.element :as element]
    [app.model.main :as main]
    [clojure.string :as str]
    [com.fulcrologic.fulcro.dom :as dom :refer [div ul li p h3 button b]]
    [com.fulcrologic.fulcro.dom.html-entities :as ent]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [taoensso.timbre :as log]
    [com.fulcrologic.semantic-ui.factories :refer [ui-button ui-menu ui-dropdown ui-dropdown-menu ui-dropdown-item]]))


(defsc Crd [this {:keys [:crd/id :crd/group :crd/name]}]
  {:id [:crd/id]
   :query [:crd/id :crd/group :crd/name]})
