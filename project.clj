(defproject cljs-bootstrap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3616"]
                 [org.clojure/tools.reader "0.10.0-SNAPSHOT"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :source-paths ["src/clojure" "src/browser" "src/node" "src/user"]
  :plugins [[lein-npm "0.5.0"]]
  :node-dependencies [[source-map-support "0.3.1"]])
