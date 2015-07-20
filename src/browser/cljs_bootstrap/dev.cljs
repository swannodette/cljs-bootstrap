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
            #_[clojure.browser.repl :as repl])
  (:import [goog.net XhrIo]
           [goog.events EventType]))

(enable-console-print!)

#_(defonce conn (repl/connect "http://localhost:9000/repl"))

(def cenv (env/default-compiler-env))

(defn get-file [url]
  (let [c (chan)]
    (.send XhrIo url
      (fn [e]
        (put! c (.. e -target getResponseText))))
    c))

(def loc js/window.location)

(defn compile-file [f]
  (let [rdr (string-push-back-reader f)
        eof (js-obj)
        env (ana/empty-env)]
    (binding [ana/*cljs-ns* 'cljs.user
              *ns* (create-ns 'cljs.core)
              r/*data-readers* tags/*cljs-data-readers*]
      (with-compiler-env cenv
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
    (let [core-edn   (<! (get-file
                           "resources/cache/cljs/core.cljs.cache.aot.edn"))
          macros-edn (<! (get-file
                           "resources/js/cljs/core$macros.cljc.cache.edn"))
          core       (<! (get-file
                           "resources/js/cljs/core.cljs"))]
      (swap! cenv assoc-in [::ana/namespaces 'cljs.core]
        (edn/read-string core-edn))
      (swap! cenv assoc-in [::ana/namespaces 'cljs.core$macros]
        (edn/read-string macros-edn))
      (analyze-core core))))

(events/listen (gdom/getElement "run") EventType.CLICK
  (fn [e]
    (main)))
