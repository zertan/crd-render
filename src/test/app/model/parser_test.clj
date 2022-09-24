(ns app.model.parser-test
  (:require
   [app.model.database :as db]
   [datascript.core :as d]
   [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
   [com.wsscode.pathom.core :as p]
   [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
   [taoensso.timbre :as log]
   [clojure.spec.alpha :as s]
   [app.model.main :as main]
   [app.model.property :as property]
   [spec-provider.provider :as sp]
   [com.fulcrologic.fulcro.algorithms.server-render :as sr]
   [com.fulcrologic.fulcro.components :as comp]))


(def resolvers [main/resolvers property/resolvers])

(def parser
  (p/parser
    {::p/env     {::p/reader               [p/map-reader
                                            pc/reader
                                            pc/open-ident-reader
                                            p/env-placeholder-reader]
                  ::p/placeholder-prefixes #{">"}}
     ::p/mutate  pc/mutate
     ::p/plugins [(pc/connect-plugin {::pc/register resolvers}) ; setup connect and use our resolvers
                  p/error-handler-plugin
                  p/request-cache-plugin
                  p/trace-plugin]}))

(comment 
(parser {} [{:main/crd-groups [:crd-group/id :crd-group/crds]}])

(parser {} [{:main/crd-groups [:crd-group/id :crd-group/crds]}])

(parser {} [[:crd-group/id "keycloak.io"]]))

;; (parser {} [{:main/element [{:element/text [:txt/text]}]}])

;; (parser {} [[:element/id 0]])

;; (parser {} [{[:element/id 1] [{:element/text [:txt/id :txt/text]}]}])


;; (parser {} [{:main/element [:element/id {:element/text [:txt/id :txt/text]} {:element/elements '...}]}])


;; (parser {} [{ :main/element [ :element/id { :element/text [ :txt/id :txt/text ] } { :element/elements '... } ] }] )

;; (def server-state (sr/build-initial-state (nth dbs/st-db 7) app.ui.element/Element))
