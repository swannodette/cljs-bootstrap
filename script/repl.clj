(require '[cljs.repl :as repl])
(require '[cljs.repl.node :as node])

(repl/repl (node/repl-env)
  :static-fns true
  :verbose true)