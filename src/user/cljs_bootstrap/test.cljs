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
           '[cljs.analyzer :as ana]
           '[cljs.compiler :as comp])

  ;; works
  (cljs/eval cenv '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))

  ;; works
  (cljs/compile cenv "(defn foo [a b] (+ a b))"
    (fn [js-source]
      (println "Source:")
      (println js-source)))

  ;; works
  (cljs/eval-str cenv
    "(defn foo [a b] (+ a b))
     (defn bar [c d] (+ c d))"
    (fn [res]
      (println res)))

  ;; works
  (cljs/eval cenv '(ns foo.bar)
    (fn [res]
      (println res)))


  (binding [cljs/*load-fn*
            (fn [lib]
              (println lib)
              "function hello() { console.log(\"Hello!\"); };")]
    (cljs/compile cenv "(ns foo.bar (:require [hello-world.core]))"
     {:verbose true}
     (fn [js-source]
       (println "Source:")
       (println js-source))))
  )