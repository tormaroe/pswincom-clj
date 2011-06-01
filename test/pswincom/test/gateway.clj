(ns pswincom.test.gateway
  (:use [pswincom.gateway.xml])
  (:use [pswincom.gateway] :reload)
  (:use [clojure.test]))

(defn- assert-auth [u p]
      (is (= u *username*))
      (is (= p *password*)))

(defn- create-sender-mock [expected-body flag-to-set]
      (fn [host & args]
          (swap! flag-to-set #(or % true))
          (doseq [[key val] (partition 2 args)]
                 (case key
                   :method (is (= "POST" val))
                   :body (is (= expected-body val))
                   :headers (is (= { "Content-Type" "text/xml" } val))))))

(deftest with-authentication-spec
      (assert-auth nil nil)
      (with-authentication "user1" "pa$$w0rd"         
         (assert-auth "user1" "pa$$w0rd"))   
      (assert-auth nil nil))

(deftest with-config-spec
         (with-config {:username "foo", :password "bar"}
              (assert-auth "foo" "bar"))
         (with-config {:username "foo", :password "bar", :from 47012345678}
              (assert-auth "foo" "bar")
              (is (= *sender* 47012345678))

              ; Verify that sender is set from config
              (let [sender-was-called (atom nil)]
                (binding [*sender-function* 
                    (create-sender-mock 
                      (request-xml {:user "foo" 
                                    :password "bar"
                                    :messages [{:receiver 4788888888
                                                :text "Hi"
                                                :sender 47012345678}]})
                      sender-was-called)]
                   (send-sms 4788888888 "Hi")
                   (is (true? @sender-was-called))))))

(deftest send-sms-spec
 (testing "that send-sms dispatches to function stored in *sender-function*,
           that the value of *gateway* is used as host,
           that the method is POST,
           that the request body is as produced by pswincom.gateway.xml/request-xml"

   (let [sender-was-called (atom nil)]
     (binding [*sender-function* 
                (create-sender-mock 
                  (request-xml {:user nil 
                                :password nil
                                :messages [{:receiver 4712345678
                                            :text "Some text"
                                            :sender "s3nd3r"}]})
                  sender-was-called)]
        (send-sms 4712345678 
                  "Some text" 
                  :sender "s3nd3r")
        (is (true? @sender-was-called))))))

(deftest send-multiple-sms-spec
  (let [sender-was-called (atom nil)]
     (binding [*sender-function* 
                (create-sender-mock 
                  (request-xml {:user nil 
                                :password nil
                                :messages [{:receiver 4700000001 :text "Foo" :sender "ACME" :tariff 100}
                                           {:receiver 4700000002 :text "Foo" :sender "ACME" :tariff 500}]})
                  sender-was-called)]
        (send-multiple-sms :text "Foo"
                           :sender "ACME"
                           :messages [{:receiver 4700000001 :tariff 100}
                                      {:receiver 4700000002 :tariff 500}])
        (is (true? @sender-was-called)))))

