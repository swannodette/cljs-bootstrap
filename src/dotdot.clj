(ns dotdot
  (:require [clojure.java.io :as io]
            [cljs.env :as env]
            [cljs.analyzer :as ana]
            [cljs.compiler :as comp]
            [cljs.closure :as closure]
            [cljs.tagged-literals :as tags]
            [clojure.tools.reader :as r]
            [clojure.tools.reader.reader-types :refer [string-push-back-reader]]))

(def cenv (env/default-compiler-env))

(comment
  (env/with-compiler-env cenv
    (let [src (io/resource "cljs/core.cljc")]
      (closure/compile src
        {:output-file (closure/src-file->target-file src)
         :force       true
         :mode        :interactive})))

  (env/with-compiler-env cenv
    (comp/munge
      (ana/resolve-var {:ns {:name 'cljs.core$macros}}
        'cljs.core$macros/..)))

  (def f (slurp (io/resource "cljs/core.cljs")))

  (string? f)

  ;; 1.3s on work machine
  (time
    (dotimes [_ 10]
      (let [rdr (string-push-back-reader f)
            eof (Object.)]
        (binding [*ns* (create-ns 'cljs.analyzer)
                  r/*data-readers* tags/*cljs-data-readers*]
          (loop []
            (let [x (r/read {:eof eof} rdr)]
              (when-not (identical? eof x)
                (recur))))))))
  )
