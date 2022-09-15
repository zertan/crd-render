(ns app.model.parser-test
  (:require
   [app.model.mock-database :as dbs :refer [db]]
   [app.model.element :as ele]
   [app.model.txt :as txt]
    [datascript.core :as d]
    [com.fulcrologic.guardrails.core :refer [>defn => | ?]]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [taoensso.timbre :as log]
    [clojure.spec.alpha :as s]
    [spec-provider.provider :as sp]
    [com.fulcrologic.fulcro.algorithms.server-render :as sr]
    [com.fulcrologic.fulcro.components :as comp]
    [app.ui.element :as elem]))

(def resolvers [ele/resolvers txt/resolvers])

;; Create a parser that uses the resolvers:
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



;; (parser {} [{:main/element [{:element/text [:txt/text]}]}])

;; (parser {} [[:element/id 0]])

;; (parser {} [{[:element/id 1] [{:element/text [:txt/id :txt/text]}]}])


;; (parser {} [{:main/element [:element/id {:element/text [:txt/id :txt/text]} {:element/elements '...}]}])


;; (parser {} [{ :main/element [ :element/id { :element/text [ :txt/id :txt/text ] } { :element/elements '... } ] }] )

;; (def server-state (sr/build-initial-state (nth dbs/st-db 7) app.ui.element/Element))


