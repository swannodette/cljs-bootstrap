(require '[cljs.build.api :as b])

(b/build "dev"
  {:output-to "main.js"
   :optimizations :simple
   :static-fns true
   :optimize-constants true
   :pretty-print true
   :verbose true})

(System/exit 0)