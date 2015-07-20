(ns cljs-bootstrap.core
  (:require-macros [cljs.core]
                   [cljs.env.macros :refer [ensure with-compiler-env]]
                   [cljs.analyzer.macros :refer [no-warn]])
  (:require [cljs.pprint :refer [pprint]]
            [cljs.tagged-literals :as tags]
            [cljs.tools.reader :as r]
            [cljs.tools.reader.reader-types :refer [string-push-back-reader]]
            [cljs.analyzer :as ana]
            [cljs.compiler :as c]
            [cljs.js]
            [cljs.env :as env]
            [cljs.reader :as edn]
            [cljs.nodejs :as nodejs]))

(enable-console-print!)
(set! *target* "nodejs")

(def cenv (env/default-compiler-env))
(def fs (js/require "fs"))
(def core (.readFileSync fs "./.cljs_node_repl/cljs/core.cljs" "utf8"))

;; 3.7s in Node.js with :simple :optimizations
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

(defn eval [s]
  (let [rdr (string-push-back-reader s)
        eof (js-obj)
        env (ana/empty-env)]
    (binding [ana/*cljs-ns* 'cljs.user
              *ns* (create-ns 'cljs.user)
              r/*data-readers* tags/*cljs-data-readers*]
      (with-compiler-env cenv
        (loop []
          (let [form (r/read {:eof eof} rdr)]
            (when-not (identical? eof form)
              (println
                (js/eval
                  (with-out-str
                    (c/emit
                      (ana/analyze
                        (assoc env :ns (ana/get-namespace ana/*cljs-ns*))
                        form)))))
              (recur))))))))

(defn -main [& args]
  (eval (first  args)))

(set! *main-cli-fn* -main)
