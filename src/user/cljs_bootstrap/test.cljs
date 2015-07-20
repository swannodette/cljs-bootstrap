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
  (env/with-compiler-env (env/default-compiler-env)
    (ana/analyze (ana/empty-env) '(ns cljs.user)))

  ;; works
  (env/with-compiler-env (env/default-compiler-env)
    (ana/analyze (assoc (ana/empty-env) :ns (ana/get-namespace 'cljs.user))
      '(ns cljs.user)))

  ;; works
  (env/with-compiler-env (env/default-compiler-env)
    (with-out-str
      (comp/emit (ana/analyze (ana/empty-env) '(ns cljs.user)))))

  ;; works
  (env/with-compiler-env (env/default-compiler-env)
    (js/eval
      (with-out-str
        (comp/emit (ana/analyze (ana/empty-env) '(ns foo.bar))))))

  ;; works
  (cljs/eval cenv '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))

  ;; works
  (cljs/eval cenv '(ns foo.bar)
    (fn [res]
      (println res)))

  ;; works
  (cljs/compile cenv "(defn foo [a b] (+ a b))"
    (fn [js-source]
      (println "Source:")
      (println js-source)))

  ;; does not work
  (cljs/compile cenv "(ns foo.bar)"
    {:verbose true}
    (fn [js-source]
      (println "Source:")
      (println js-source)))

  ;; works
  (cljs/eval-str cenv
    "(defn foo [a b] (+ a b))
     (defn bar [c d] (+ c d))"
    (fn [res]
      (println res)))

  ;; doesn't work yet
  (try
    (cljs/compile cenv "(ns cljs.user)"
      (fn [js-source]
        (println "Source:")
        (println js-source)))
    (catch js/Error e
      (println e)))
  )