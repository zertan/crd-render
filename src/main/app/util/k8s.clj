(ns app.util.k8s
(:require [kubernetes-api.core :as k8s]
          [clojure.inspector :as inspector])
 (:import [io.fabric8.kubernetes.api.model.apiextensions.v1 CustomResourceDefinition
           CustomResourceDefinitionList]
          io.fabric8.kubernetes.client.KubernetesClientBuilder))

(def oc (k8s/client "https://api.gxmpox00.westeurope.aroapp.io:6443" {:token "sha256~upWlnfYyG-aqwg-bHbhcej5j-liL3LW_XfTPFBrUH0A"}))

;; (k8s/explore k8s :Deployment)
;; (k8s/explore k8s :Deployment)

;; (keys (:properties (:realm (:properties (:spec (:properties (:openAPIV3Schema (:schema (first (get-in out [:spec :versions]))))))))))

