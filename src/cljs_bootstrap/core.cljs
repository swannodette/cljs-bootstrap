(ns cljs-bootstrap.core
  (:require-macros [cljs.env.macros :refer [ensure]])
  (:require [cljs.pprint :refer [pprint]]
            [cljs.tools.reader :as r]
            [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.analyzer :as ana]
            [cljs.compiler :as c]))

(set! *target* "nodejs")
(apply load-file ["./.cljs_node_repl/cljs/core$macros.js"])

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

  ;; fails, due to regular expression
  (ensure
    (c/emit
      (ana/analyze-seq
        (assoc (ana/empty-env) :context :expr)
        '(first [1 2 3]) nil nil)))

  (js/eval
    (with-out-str
      (c/emit
        (ensure
          (ana/analyze
            (assoc (ana/empty-env) :context :expr)
            '(first [1 2 3]))))))

  (pprint
    (ensure
      (ana/analyze
        (assoc (ana/empty-env) :context :expr)
        '(fn [a b] (+ a b)))))

  ;; ignore for now

  ;(def fs (js/require "fs"))
  ;
  ;(def ana (.readFileSync fs "resources/cljs/analyzer.cljc" "utf8"))
  ;
  ;(goog/isString ana)
  ;
  ;(let [rdr (string-push-back-reader ana)
  ;      eof (js-obj)]
  ;  (binding [*ns* (create-ns 'cljs.analyzer)]
  ;    (loop []
  ;      (let [x (r/read {:eof eof} rdr)]
  ;        (when-not (identical? eof x)
  ;          (prn x)
  ;          (recur))))))
  )