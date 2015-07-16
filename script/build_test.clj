(require '[cljs.build.api :as b])

(b/build "src/user"
  {:output-to "main.js"
   :target :nodejs
   :optimizations :simple
   :pretty-print true
   :cache-analysis true
   :static-fns true
   :optimize-constants true
   :verbose true})

(System/exit 0)