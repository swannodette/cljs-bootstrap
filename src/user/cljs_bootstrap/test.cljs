(ns cljs-bootstrap.test
  (:require [cljs.js :as cljs]
            [cljs.analyzer :as ana]))

(enable-console-print!)

(set! *target* "nodejs")
(def cenv (cljs/empty-env))

(comment
  (require-macros '[cljs.env.macros :as env])
  (require '[cljs.pprint :as pp]
           '[cljs.env :as env]
           '[cljs.analyzer :as ana])

  ;; works
  (cljs/eval cenv '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))

  (cljs/compile cenv "(defn foo [a b] (+ a b))"
    (fn [js-source]
      (println js-source)))

  (cljs/eval-str cenv
    "(defn foo [a b] (+ a b))
     (defn bar [c d] (+ c d))"
    (fn [res]
      (println res)))
  )