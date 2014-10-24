(defproject oroboros "0.1.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :source-paths ["src/clj"]
  :test-paths ["test/clj" "test/jvm"]
  :java-source-paths ["src/jvm" "test/jvm"]
  :junit ["test/jvm"]
  :aot :all
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.5"]
                 [clj-http "1.0.0"]
                 [me.raynes/fs "1.4.6"]
                 [circleci/clj-yaml "0.5.3"]
                 [eggsby/stencil "0.3.4-eggsby"]
                 [matross/mapstache "0.3.1"]
                 [compojure "1.1.9"]]
  :plugins [[lein-ring "0.8.12"]
            [lein-junit "1.1.2"]]
  :ring {:handler oroboros.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [junit/junit "4.11"]
                        [ring-mock "0.1.5"]]}})
