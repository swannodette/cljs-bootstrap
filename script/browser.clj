(require '[cljs.build.api :as b])

(b/build "src"
  {:output-to "main.js"
   :optimizations :simple
   :static-fns true
   :optimize-constants true
   :verbose true})

(System/exit 0)