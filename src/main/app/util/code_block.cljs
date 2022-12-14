(ns app.util.code-block
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
            [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
            ["react-syntax-highlighter/dist/esm/languages/prism/yaml$default" :as yaml-prism]
            ["react-syntax-highlighter/dist/esm/prism.js$default" :as SyntaxHighlighter]
            ;["react-code-blocks" :default CodeBlock]
            ;["remark-gfm" :default remarkGfm]
            ;["rehype-highlight" :default rehypeHighlight]
            ;["highlight.js/lib/languages/yaml.js" :as yaml]
            ))

(def ui-code-block (interop/react-factory SyntaxHighlighter))

(.registerLanguage SyntaxHighlighter "yaml" yaml-prism)

;; https://github.com/rajinwonderland/react-code-blocks#supported-languages

;; (def languages {:clojure clojure
;;                 :yaml })

;; (defsc Render [_this {:keys [body]}]
;;   {}
;;   (when body
;;     (ui-markdown {:children body
;;                   ;:remarkPlugins [remarkGfm]
;;                   :rehypePlugins [(partial rehypeHighlight (clj->js {:languages languages}))]})))

;; (def render (comp/factory Render))
