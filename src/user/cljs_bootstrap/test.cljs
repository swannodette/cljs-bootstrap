(ns cljs-bootstrap.test
  (:require [cljs.js :as cljs]
            [cljs.reader :refer [read-string]]
            [cognitect.transit :as t]))

(set! *target* "nodejs")
(enable-console-print!)

(def vm (js/require "vm"))
(def fs (js/require "fs"))

;; -----------------------------------------------------------------------------
;; Cache loading

(def cache-path "resources/cache/cljs/core.cljs.cache.aot.edn")
(def cache-transit-path "resources/cache/cljs/core.cljs.cache.aot.json")

(defn convert-cache []
  (let [core-cache (read-string (.readFileSync fs cache-path "utf-8"))
        core-cache-transit (t/write (t/writer :json) core-cache)]
    (.writeFileSync fs cache-transit-path core-cache-transit "utf-8")))

;; 40ms to read
(defn read-transit-cache []
  (let [core-cache-transit (.readFileSync fs cache-transit-path "utf-8")]
    (t/read (t/reader :json) core-cache-transit)))

(defn load-cache [state]
  (assoc-in state [:cljs.analyzer/namespaces 'cljs.core]
    (read-transit-cache)))

;; -----------------------------------------------------------------------------
;; Main

(def st (cljs/empty-state load-cache))

(defn node-eval [{:keys [name source]}]
  (.runInThisContext vm source (str (munge name) ".js")))

(def libs
  {'hello-world.core   :cljs
   'hello-world.macros :clj})

(defn node-load [{:keys [name macros]} cb]
  (if (contains? libs name)
    (let [path (str "src/user/" (cljs/ns->relpath name)
                    "." (cljs.core/name (get libs name)))]
      (.readFile fs path "utf-8"
        (fn [err src]
          (cb (if-not err
                {:lang :clj :source src}
                (.error js/console err))))))
    (cb nil)))

(comment
  (require-macros '[cljs.env.macros :as env])
  (require '[cljs.pprint :as pp]
           '[cljs.env :as env]
           '[cljs.analyzer :as ana]
           '[cljs.compiler :as comp])

  (cljs/eval st '(defn foo [a b] (+ a b))
    {:eval cljs/js-eval}
    (fn [res]
      (println res)))

  (cljs/compile st "(defprotocol IFoo (foo [this]))"
    (fn [js-source]
      (println "Source:")
      (println js-source)))

  (cljs/eval-str st
    "(defn foo [a b] (+ a b))
     (defn bar [c d] (+ c d))"
    nil
    {:eval cljs/js-eval}
    (fn [res]
      (println res)))

  (cljs/eval-str st "1"
    nil
    {:eval cljs/js-eval
     :context :expr}
    (fn [res]
      (println res)))

  (cljs/eval-str st "(def x 1)"
    nil
    {:eval cljs/js-eval
     :context :expr
     :def-emits-var true}
    (fn [res]
      (println res)))

  (cljs/eval st '(ns foo.bar)
    {:eval cljs/js-eval}
    (fn [res]
      (println res)))

  (cljs/compile st "(defn foo\n[a b]\n(+ a b))" 'cljs.foo
    {:verbose true :source-map true}
    (fn [js-source]
      (println "Source:")
      (println js-source)))

  (cljs/eval-str st
    "(ns foo.bar (:require [hello-world.core]))\n(hello-world.core/bar 3 4)"
    'foo.bar
    {:verbose true
     :source-map true
     :eval node-eval
     :load node-load}
    (fn [ret]
      (println ret)))

  (cljs/eval-str st
    "(ns foo.bar (:require-macros [hello-world.macros :refer [mult]]))\n(mult 4 4)"
    'foo.bar
    {:verbose true
     :source-map true
     :eval node-eval
     :load node-load}
    (fn [{:keys [error] :as res}]
      (if error
        (do
          (println error)
          (println (.. error -cause -stack)))
        (println res))))

  (cljs/eval-str st
    "(ns foo.bar)\n(first [1 2 3])"
    'foo.bar
    {:verbose true
     :source-map true
     :eval node-eval
     :load node-load}
    (fn [{:keys [error] :as res}]
      (if error
        (do
          (println error)
          (println (.. error -cause -stack)))
        (println res))))

  (cljs/eval-str st
    "(ns foo.bar)\n(map inc [1 2 3])"
    'foo.bar
    {:verbose true
     :source-map true
     :eval node-eval
     :load node-load}
    (fn [{:keys [error] :as res}]
      (if error
        (do
          (println error)
          (println (.. error -cause -stack)))
        (println res))))
  )