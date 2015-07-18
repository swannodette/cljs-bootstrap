;; IMPORTANT: to load this file in a REPL or compile it you must first
;; delete src/clojure/cljs/core$macros.js

(ns cljs-bootstrap.test
  (:require-macros [cljs.env.macros :as env])
  (:require [cljs.js :as cljs]
            [cljs.pprint :as pp]
            [cljs.env :as env]
            [cljs.analyzer :as ana]
            [cljs.compiler :as comp]))

(enable-console-print!)

(set! *target* "nodejs")
(def cenv (cljs/empty-env))

(comment
  ;; works
  (cljs/eval cenv '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))
  )