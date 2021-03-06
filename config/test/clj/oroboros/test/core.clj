(ns oroboros.test.core
  (:require [clojure.test :refer :all]
            [me.raynes.fs :as fs]
            [oroboros.core :refer :all]))

(deftest test-config

  (testing "can template regular maps"
    (is (= {:x "foo" :y "foo"} (template-map {:x "foo" :y "{{ x }}"}))))

  (testing "templating resolves to nearest ancestor"
    (let [m {:x 1 :y {:x 2, :y "{{ x }}"}}]
      (is (= 2 (get-in (template-map m) [:y :y])))))

  (testing "can template across several contexts"
    (let [m (template-map {:y "{{x}}"})]
      (is (= {:y "{{x}}"} m))
      (is (= {:x 23 :sub {:y 23}}
             (assoc (template-map {:x 23}) :sub m)))))

  (testing "supports env var template values"
    (let [m (template-map {:pwd "{{ env/PWD }}"})]
      (is (= {:pwd (System/getenv "PWD")} m))))
  
  (testing "supports system properties"
    (let [m (template-map {:tmp "{{ props/java.io.tmpdir }}"})]
      (is (= {:tmp (System/getProperty "java.io.tmpdir")} m))))
  
  (testing "can overlay one context onto another"
    (let [x (template-map {:foo [{:bar "{{ x }}"}]})
          y {:x "baz"}]
      (is (= {:foo [{:bar "baz"}]} (overlay x y)))))
  
  (testing "can find config names in a directory"
    (is (= #{"jerry"} (find-names "../examples/simple"))))

  (testing "can find config files in a directory"
    (is (= [(fs/file "../examples/simple/config.yaml")]
           (find-configs "../examples/simple")))

    (is (= [(fs/file "../examples/simple/config.yaml")
            (fs/file "../examples/simple/jerry.yaml")]
           (find-configs "../examples/simple" "foobar" "jerry"))))

  (defn get-uris [& files]
    (map #(.toURI (clojure.java.io/resource %) ) files))
  
  (testing "can find config files on the classpath"
    (is (= (get-uris "examples/simple/config.yaml")
           (find-configs-from-classpath "examples/simple")))
    (is (= (get-uris "examples/simple/config.yaml" "examples/simple/jerry.yaml")
           (find-configs-from-classpath "examples/simple" "jerry"))))

  (testing "can get a cursor for a file"
    (let [f (fs/file "../examples/simple/config.yaml")]
      (is (= [:simple] (config-to-cursor "../examples" f)))))
  
  (testing "can get a cursor for a resource uri"
    (let [uri (clojure.java.io/resource "examples/simple/config.yaml")]
      (is (= [:simple] (resource-to-cursor "examples" uri)))))
  
  (testing "can load config files as template maps"
    (let [config (load-config "../examples/simple" "jerry")]
      (is (= {:cat "tom", :mouse "jerry", :name "jerry & tom"} config))))

  (testing "can recursively load templated configs"
    (is (= {:advanced {:web {:port 1337, :protocol "http", :host "web.example.com:1337",
                       :api, "http://web.example.com:1337/v/1.2.3",
                       :command "./bin/start --db db.example.com"}
                       :db {:host "db.example.com"},
                       :version "1.2.3"}
            :simple {:cat "tom", :mouse "jerry", :name "tom & jerry"}}
           (load-config "../examples")))

    (is (= {:web {:port 1337, :protocol "https", :host "expensive-server.example.com",
                  :api "https://expensive-server.example.com/v/1.2.3",
                  :command "./bin/start --db prod-db.example.com"},
            :db {:host "prod-db.example.com"}, :version "1.2.3"}
           (load-config "../examples/advanced" "production"))))

  (testing "resources and filesystem configs are equivalent"
    (is (= (load-config "../examples/advanced" "production")
           (resource-config "examples/advanced" "production")))))
