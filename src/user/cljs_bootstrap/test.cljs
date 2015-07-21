(ns cljs-bootstrap.test
  (:require [cljs.js :as cljs]))

(enable-console-print!)

(set! *target* "nodejs")
(def st (cljs/empty-state))

(comment
  (require-macros '[cljs.env.macros :as env])
  (require '[cljs.pprint :as pp]
           '[cljs.env :as env]
           '[cljs.analyzer :as ana]
           '[cljs.compiler :as comp])

  ;; works
  (cljs/eval st '(defn foo [a b] (+ a b))
    (fn [res]
      (println res)))

  ;; works
  (cljs/compile st "(defprotocol IFoo (foo [this]))"
    (fn [js-source]
      (println "Source:")
      (println js-source)))

  ;; works
  (cljs/eval-str st
    "(defn foo [a b] (+ a b))
     (defn bar [c d] (+ c d))"
    (fn [res]
      (println res)))

  ;; works
  (cljs/eval st '(ns foo.bar)
    (fn [res]
      (println res)))

  (binding [cljs/*load-fn*
            (fn [lib cb]
              (println lib)
              (cb {:lang :js
                   :source "function hello() { console.log(\"Hello!\"); };"}))]
    (cljs/compile st "(ns foo.bar (:require [hello-world.core]))"
      {:verbose true}
      (fn [js-source]
        (println "Source:")
        (println js-source))))

  (def vm (js/require "vm"))

  (cljs/eval-str st "(ns foo.bar (:require [hello-world.core]))"
    {:verbose true
     :eval-fn (fn [source] (.runInThisContext vm source "wat.js"))
     :load-fn (fn [lib cb]
                (println lib)
                (cb {:lang :js
                     :source "function hello() { console.log(\"Hello!\"); };"}))}
    (fn [js-source]
      (println "Source:")
      (println js-source)))
  )