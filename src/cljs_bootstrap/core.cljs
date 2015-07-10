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
            [cljs.reader :as edn]
            [cljs.nodejs :as nodejs]))

#_(set! *target* "nodejs")
(apply load-file ["./.cljs_node_repl/cljs/core$macros.js"])

(def cenv (env/default-compiler-env))

(def fs (js/require "fs"))

;; load cache files

(def core-edn (.readFileSync fs "resources/cljs/core.cljs.cache.aot.edn" "utf8"))

(swap! cenv assoc-in [::ana/namespaces 'cljs.core]
  (edn/read-string core-edn))

(def macros-edn (.readFileSync fs ".cljs_node_repl/cljs/core$macros.cljc.cache.edn" "utf8"))

(swap! cenv assoc-in [::ana/namespaces 'cljs.core$macros]
  (edn/read-string macros-edn))

;; load standard lib

(def core (.readFileSync fs "resources/cljs/core.cljs" "utf8"))

(defn analyze-file [f]
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
              (ana/analyze
                (assoc env :ns (ana/get-namespace ana/*cljs-ns*))
                form)
              (recur))))))))

(defn -main [& args]
  (analyze-file core))

(set! *main-cli-fn* -main)

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

  (println
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

  ;; ~60ms Node.js
  (dotimes [_ 10]
    (time
      (let [rdr (string-push-back-reader f)
            eof (js-obj)]
        (binding [*ns* (create-ns 'cljs.analyzer)
                  r/*data-readers* tags/*cljs-data-readers*]
          (loop []
            (let [x (r/read {:eof eof} rdr)]
              (when-not (identical? eof x)
                (recur))))))))

  ;; 4.3s Nodej.s
  (time
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
                #_(prn form)
                (ana/analyze
                  (assoc env :ns (ana/get-namespace ana/*cljs-ns*))
                  form)
                (recur))))))))
  )
