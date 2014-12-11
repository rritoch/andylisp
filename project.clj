(defproject com.vnetpublishing/clojureplus "0.1.0-SNAPSHOT"
  :description "Clojure Proof of Concept projects"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clj"]
  :java-source-paths ["src/jvm"]
  :prep-tasks ["compile" "javac"]
  :resource-paths ["src/resources"]
  :dependencies [[org.clojure/clojure "1.7.0-master-SNAPSHOT"]
                 
                 ;[org.codehaus.jsr166-mirror/extra166y "1.7.0"]
                 [org.clojure/test.generative "0.5.1" :exclusions [org.clojure/clojure]]
                 [org.clojure/test.check "0.5.9" :exclusions [org.clojure/clojure]]]
                  
  :aot :all
  
  
  :profiles {:provided {:dependencies [[org.codehaus.jsr166-mirror/jsr166y "1.7.0"]]}})