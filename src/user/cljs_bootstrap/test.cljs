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
  (pp/pprint
    (binding [env/*compiler* cenv
              ana/*cljs-ns* 'cljs.user]
      (ana/get-namespace ana/*cljs-ns*)))

  (pp/pprint
    (binding [env/*compiler* cenv
              ana/*cljs-ns* 'cljs.user]
      (assoc (ana/empty-env)
        :ns (ana/get-namespace ana/*cljs-ns*))))

  (binding [env/*compiler* cenv
            ana/*cljs-ns* 'cljs.user]
    (assoc (ana/empty-env)
      :ns {:name ana/*cljs-ns*}))

  ;; works
  ;; cljs.user.foo = ...
  (binding [env/*compiler* cenv
            ana/*cljs-ns*  'cljs.user]
    (with-out-str
      (comp/emit
        (ana/analyze (assoc (ana/empty-env)
                       :ns (ana/get-namespace ana/*cljs-ns*))
          '(defn foo [a b] (+ a b))))))

  ;; does not work yet
  (cljs/eval cenv '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))
  )