(ns dotdot
  (:require [clojure.java.io :as io]
            [cljs.env :as env]
            [cljs.analyzer :as ana]
            [cljs.compiler :as comp]
            [cljs.closure :as closure]))

(def cenv (env/default-compiler-env))

(comment
  (env/with-compiler-env cenv
    (let [src (io/resource "cljs/core.cljc")]
      (closure/compile src
        {:output-file (closure/src-file->target-file src)
         :force       true
         :mode        :interactive})))

  (env/with-compiler-env cenv
    (comp/munge
      (ana/resolve-var {:ns {:name 'cljs.core$macros}}
        'cljs.core$macros/..)))
  )
