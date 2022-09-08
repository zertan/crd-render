(ns app.model.test
  (:require
    #?(:clj  [clojure.core.async :as async :refer [go <!]]
       :cljs [cljs.core.async :as async :refer-macros [go]])
    [clojure.test :refer [is are testing]]
    [clojure.walk :as walk]
    #?(:clj
       [com.wsscode.async.async-clj :refer [go-catch go-promise <!maybe <?]])
    [com.wsscode.pathom.connect :as pc]
    [com.wsscode.pathom.connect.test :as pct]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.parser :as pp]
    [com.wsscode.pathom.sugar :as ps]
    [com.wsscode.pathom.trace :as pt]
    ;[nubank.workspaces.core :refer [deftest]]
    )
  #?(:clj
     (:import
       (clojure.lang
         ExceptionInfo))))

(declare quick-parser)

(def base-indexes (atom {}))

(defonce quick-parser-trace* (atom []))

(defmulti resolver-fn pc/resolver-dispatch)
(def defresolver (pc/resolver-factory resolver-fn base-indexes))

(defmulti mutate-fn pc/mutation-dispatch)
(def defmutation (pc/mutation-factory mutate-fn base-indexes))

(def users
  {1 {:user/id 1 :user/name "Mel" :user/age 26 :user/login "meel"}
   2 {:user/id 2 :user/name "Gin" :user/age 22 :user/login "gin"}})


#?(:clj
   (defn quick-parser [{::p/keys  [env]
                        ::pc/keys [register]} query]
     (let [trace  (atom [])
           parser (p/parallel-parser {::p/env     (merge {::p/reader               [p/map-reader
                                                                                    pc/parallel-reader
                                                                                    pc/open-ident-reader
                                                                                    p/env-placeholder-reader]
                                                          ::pt/trace*              trace
                                                          ::p/placeholder-prefixes #{">"}}
                                                         env)
                                      ::p/mutate  pc/mutate-async
                                      ::p/plugins [(pc/connect-plugin {::pc/register register})
                                                   p/error-handler-plugin
                                                   p/request-cache-plugin
                                                   p/trace-plugin]})
           res    (async/<!! (parser {} query))]
       (reset! quick-parser-trace* @trace)
       res)))

;(quick-parser )

;; (pc/defresolver person-resolver [{:keys [database] :as env} {:keys [person/id]}]
;;   {::pc/input #{:person/id}
;;    ::pc/output [:person/first-name :person/age]}
;;   (let [person (db/get-person database id)]
;;     {:person/age        (:age person)
;;      :person/first-name (:first-name person)}))
