(ns cljs-bootstrap.test
  (:require [cljs.js :as cljs]))

(enable-console-print!)

(set! *target* "nodejs")
(def cenv (cljs/empty-env))

(comment
  ;; works
  (cljs/eval cenv '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))
  )