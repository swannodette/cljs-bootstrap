(require '[cljs.build.api :as b])
(require '[cljs.repl :as repl])
(require '[cljs.repl.browser :as browser])

(b/build (b/inputs "src" "dev")
  {:main 'om.dev
   :asset-path "js"
   :output-to "resources/js/app.js"
   :output-dir "resources/js"
   :verbose true})

(cljs.repl/repl
  (browser/repl-env
    :static-dir ["resources/html" "resources"])
  :output-dir "resources/js"
  :asset-path "js"