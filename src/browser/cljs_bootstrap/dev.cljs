(ns cljs-bootstrap.dev
  (:require-macros [cljs.env.macros :refer [ensure with-compiler-env]]
                   [cljs.analyzer.macros :refer [no-warn]]
                   [cljs.core.async.macros :refer [go]])
  (:require [cljs.js :as cljs]
            [cljs.pprint :refer [pprint]]
            [cljs.tagged-literals :as tags]
            [cljs.tools.reader :as r]
            [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.analyzer :as ana]
            [cljs.compiler :as c]
            [cljs.env :as env]
            [cljs.reader :as edn]
            [cljs.core.async :refer [>! <! take! put! chan]]
            [goog.dom :as gdom]
            [goog.events :as events]
            [clojure.browser.repl :as repl])
  (:import [goog.net XhrIo]
           [goog.events EventType]))

(enable-console-print!)

(defonce conn (repl/connect "http://localhost:9000/repl"))

(def st (cljs/empty-state))

(defn get-file [url]
  (let [c (chan)]
    (.send XhrIo url
      (fn [e]
        (put! c (.. e -target getResponseText))))
    c))

(defn compile-file [f]
  (let [rdr (string-push-back-reader f)
        eof (js-obj)
        env (ana/empty-env)]
    (binding [ana/*cljs-ns* 'cljs.user
              *ns* (create-ns 'cljs.core)
              r/*data-readers* tags/*cljs-data-readers*]
      (cljs/with-state st
        (loop []
          (let [form (r/read {:eof eof} rdr)]
            (when-not (identical? eof form)
              (with-out-str
                (c/emit
                  (ana/analyze
                    (assoc env :ns (ana/get-namespace ana/*cljs-ns*))
                    form)))
              (recur))))))))

;; <1.6s on WebKit Nightly
;; 3.5s on Firefox
;; <3.0s on Canary
(defn analyze-core [core]
  (set! (. (gdom/getElement "time") -innerHTML)
    (with-out-str (time (compile-file core)))))

(defn main []
  (go
    (let [core (<! (get-file "resources/js/cljs/core.cljs"))]
      (analyze-core core))))

(when-let [button (gdom/getElement "run")]
  (events/listen button EventType.CLICK
    (fn [e]
      (main))))

(def libs
  {'hello-world.core   :cljs
   'hello-world.macros :clj})

(defn browser-load [{:keys [name macros]} cb]
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
    '[cljs.compiler :as comp]
    '[cljs.source-map :as sm]
    '[goog.object :as gobj])

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

  ;; missing paren, will cause an error
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

  ;; decode source map
  ;; 2 seconds under V8 (Node.js)
  (time
    (do
      (sm/decode (.parse js/JSON (:core-source-map-json @st)))
      nil))

  (cljs/file->ns "cljs/core.cljs")

  )