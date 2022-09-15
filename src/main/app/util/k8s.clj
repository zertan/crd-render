(ns app.util.k8s
(:require [kubernetes-api.core :as k8s]
          [clojure.inspector :as inspector]
          [mount.core :refer [defstate]])
 (:import [io.fabric8.kubernetes.api.model.apiextensions.v1 CustomResourceDefinition
           CustomResourceDefinitionList]
          io.fabric8.kubernetes.client.KubernetesClientBuilder))

(defn get-crds [oc]
  (:items (k8s/invoke oc {:kind :CustomResourceDefinition :action :list :request {:name ""}})))

(defn get-crd-groups [crds]
  (vec (sort-by first (distinct (map #(-> % (:spec) (:group)) crds)))))

(defn get-crds-in-group [crds group]
  (vec (sort-by first (remove nil? (map #(if (= group (-> % (:spec) (:group)))
                           (-> % (:spec) (:names) (:kind))) crds)))))

;; (defn get-crd [oc name]
;;   (k8s/invoke oc {:kind :CustomResourceDefinition :action :get :request {:name name}}))

(defn get-crd [crds name]
  (first (filter #(= name (-> % (:spec) (:names) (:kind))) crds)))


(defstate oc :start (k8s/client "https://api.gxmpox00.westeurope.aroapp.io:6443" {:token "sha256~Kljzi3Zxx1NrhuPLN9kcHWCSxLnh3VB84ZZrJQxQ_s4"}))

(defstate crds :start (get-crds oc))

;  oc get crd -o=jsonpath='{.items[*].spec.group}'

;; (k8s/explore k8s :Deployment)
;; (k8s/explore k8s :Deployment)

;; (keys (:properties (:realm (:properties (:spec (:properties (:openAPIV3Schema (:schema (first (get-in out [:spec :versions]))))))))))

