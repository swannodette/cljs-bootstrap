(ns cljs-bootstrap.core
  (:require-macros [cljs.env.macros :refer [ensure]])
  (:require [cljs.tools.reader :as r]
            [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.analyzer :as ana]
            [cljs.compiler :as c]))

(comment
  (js/eval
    (with-out-str
      (c/emit
        (ensure
          (ana/analyze-keyword
            (assoc (ana/empty-env) :context :expr)
            :foo)))))

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