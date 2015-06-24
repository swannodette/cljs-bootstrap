(ns cljs-bootstrap.core
  (:require-macros [cljs.env.macros :refer [ensure with-compiler-env]]
                   [cljs.analyzer.macros :refer [no-warn]])
  (:require [cljs.pprint :refer [pprint]]
            [cljs.tagged-literals :as tags]
            [cljs.tools.reader :as r]
            [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.analyzer :as ana]
            [cljs.compiler :as c]
            [cljs.env :as env]
            [cljs.reader :as edn]))

(set! *target* "nodejs")
(apply load-file ["./.cljs_node_repl/cljs/core$macros.js"])

(def cenv (env/default-compiler-env))

(comment
  ;; NOTE: pprint'ing the AST seems to fail

  ;; works
  (js/eval
    (with-out-str
      (c/emit
        (ensure
          (ana/analyze-keyword
            (assoc (ana/empty-env) :context :expr)
            :foo)))))

  ;; works
  (js/eval
    (with-out-str
      (c/emit
        (ensure
          (ana/analyze
            (assoc (ana/empty-env) :context :expr)
            '(+ 1 2))))))

  ;; works
  (ensure
    (ana/get-expander
      (first '(first [1 2 3]))
      (assoc (ana/empty-env) :context :expr)))

  ;; works
  (let [form  '(second [1 2 3])
        mform (ensure
                (ana/macroexpand-1
                  (assoc (ana/empty-env) :context :expr) form))]
    (identical? form mform))

  ;; get the expected error if we use quote instead of syntax
  ;; quote since cljs.core not yet analyzed
  (ensure
    (ana/parse-invoke
      (assoc (ana/empty-env) :context :expr) `(second [1 2 3])))

  ;; works
  (ensure
    (ana/analyze-seq
      (assoc (ana/empty-env) :context :expr)
      '(first [1 2 3]) nil nil))

  ;; works
  ;; includes warning if not suppressed via no-warn
  (js/eval
    (with-out-str
      (ensure
        (c/emit
          (no-warn
            (ana/analyze-seq
              (assoc (ana/empty-env) :context :expr)
              `(first [1 2 3]) nil nil))))))

  ;; works, same as above
  (js/eval
    (with-out-str
      (ensure
        (c/emit
          (no-warn
            (ana/analyze
              (assoc (ana/empty-env) :context :expr)
              `(first [1 2 3])))))))

  ;; works
  (js/eval
    (with-out-str
      (ensure
        (c/emit
          (no-warn
            (ana/analyze
              (assoc (ana/empty-env) :context :expr)
              `((fn [a# b#] (+ a# b#)) 1 2)))))))

  (def fs (js/require "fs"))

  ;; load cache files

  (def core-edn (.readFileSync fs "resources/cljs/core.cljs.cache.aot.edn" "utf8"))

  (goog/isString core-edn)

  (swap! cenv assoc-in [::ana/namespaces 'cljs.core]
    (edn/read-string core-edn))

  (def macros-edn (.readFileSync fs ".cljs_node_repl/cljs/core$macros.cljc.cache.edn" "utf8"))

  (goog/isString macros-edn)

  (swap! cenv assoc-in [::ana/namespaces 'cljs.core$macros]
    (edn/read-string macros-edn))

  ;; load standard lib

  (def f (.readFileSync fs "resources/cljs/core.cljs" "utf8"))

  (goog/isString f)

  ;; 2.5second on work machine
  (time
    (let [rdr (string-push-back-reader f)
          eof (js-obj)]
     (binding [*ns* (create-ns 'cljs.analyzer)
               r/*data-readers* tags/*cljs-data-readers*]
       (loop []
         (let [x (r/read {:eof eof} rdr)]
           (when-not (identical? eof x)
             (recur)))))))

  (ensure
    (ana/get-expander 'defn (ana/empty-env)))

  ;; doesn't work because let is a JavaScript keyword
  ;; munged to let$
  (ensure
    (ana/get-expander 'let (ana/empty-env)))

  (ensure
    (ana/core-name? (ana/empty-env) 'defn))

  ;; doesn't work yet
  ;; slow, probably because of naive ns code building mappings
  ;; every time
  ;; for some reason js ns not handled correctly
  (time
    (let [rdr (string-push-back-reader f)
          eof (js-obj)
          env (ana/empty-env)]
      (binding [ana/*cljs-ns* 'cljs.user
                *ns* (create-ns 'cljs.analyzer)
                r/*data-readers* tags/*cljs-data-readers*]
        (with-compiler-env cenv
          (loop []
            (let [form (r/read {:eof eof} rdr)]
              (when-not (identical? eof form)
                ;;(prn form)
                (ana/analyze (assoc env :ns *cljs-ns*) form)
                (recur))))))))
  )
