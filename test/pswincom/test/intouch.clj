(ns pswincom.test.intouch
  (:use [pswincom.intouch] :reload)
  (:use [clojure.test]))

(deftest receivers-spec

   (is (= (receivers :numbers 12345678) "12345678"))
   (is (= (receivers :numbers [12345678 12345679]) "12345678;12345679"))
   (is (= (receivers :contacts 4567) "/contacts/4567"))
   (is (= (receivers :groups 23) "/groups/23"))
   (is (= (receivers :groups [23 24]) "/groups/23;/groups/24"))
   (is (= (receivers :contacts [1 2 3] :groups [23 24]) 
          "/groups/23;/groups/24;/contacts/1;/contacts/2;/contacts/3"))
   (is (= (receivers :numbers [11 22 33] :groups 123)
          "11;22;33;/groups/123")))

(deftest resource-path-spec
   (is (= (resource-path :groups) "groups"))
   (is (= (resource-path :groups 45) "groups/45"))
   (is (= (resource-path :groups 45 :contacts) "groups/45/contacts"))
   (is (= (resource-path "groups" 45 :contacts :3456) "groups/45/contacts/3456")))
