(ns app.model.spec
  (:require
   [app.model.mock-database :as dbs :refer [db]]
    [datascript.core :as d]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]))

;(s/def :KeycloakRealm/apiVersion str?)

