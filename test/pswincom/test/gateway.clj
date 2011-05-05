(ns pswincom.test.gateway
  (:use [pswincom.gateway] :reload)
  (:use [clojure.test])
  (:import (java.util Calendar)))

(deftest with-authentication-spec
   (letfn [(assert-auth [u p]
               (is (= u *username*))
               (is (= p *password*)))]

      (assert-auth nil nil)
      (with-authentication "user1" "pa$$w0rd"         
         (assert-auth "user1" "pa$$w0rd"))   
      (assert-auth nil nil)))


(deftest send-sms-spec

     ; Mocking the sender function
   (let [sender-was-called (atom nil)]
     (binding [*sender-function* (fn [host & args]
                                     (swap! sender-was-called #(or % true))
                                     (doseq [[key val] (partition 2 args)]
                                        (case key
                                          :method (is (= "POST" val))
                                          :body (is (= (request-xml {:user nil :password nil
                                                                     :messages [{:receiver 4712345678
                                                                                 :text "Some text"
                                                                                 :sender "s3nd3r"}]}) val))
                                          :headers (is (= { "Content-Type" "text/xml" } val))))
                                     (is (= host *gateway*)))]
        (send-sms 4712345678 
                  "Some text" 
                  :sender "s3nd3r")
        (is (true? @sender-was-called)))))


(deftest request-xml-spec
  
   (testing "Request with no message"
     (is (= (request-xml {:user "u" :password "p"})
            (str "<?xml version=\"1.0\"?>\r\n"
                 "<SESSION><CLIENT>u</CLIENT><PW>p</PW><MSGLST></MSGLST></SESSION>"))))

   (testing "Request with simple message"
     (is (= (request-xml {:user "u" :password "p" 
                          :messages [{:receiver 4712345678 :text "Hi!"}]})
            (str "<?xml version=\"1.0\"?>\r\n"
                 "<SESSION><CLIENT>u</CLIENT><PW>p</PW><MSGLST>"
                 "<MSG><ID>1</ID><TEXT>Hi!</TEXT><RCV>4712345678</RCV></MSG>"
                 "</MSGLST></SESSION>"))))
   
   (testing "Request with complete message"
     (is (= (request-xml {:user "u" :password "p" 
                          :messages [{:receiver 4712345678 
                                      :text "Hi!"
                                      :sender "Foo"
                                      :TTL 4
                                      :tariff 500
                                      :service-code 15001
                                      :delivery-time (doto (Calendar/getInstance)
                                                           ; note that setting month to 1
                                                           ; actually means Februrary
                                                           ; (stupid Java!)
                                                           (.set 2011 1 15 13 30))}]})
            (str "<?xml version=\"1.0\"?>\r\n"
                 "<SESSION><CLIENT>u</CLIENT><PW>p</PW><MSGLST>"
                 "<MSG><ID>1</ID><TEXT>Hi!</TEXT><RCV>4712345678</RCV>"
                 "<SND>Foo</SND><TTL>4</TTL><TARIFF>500</TARIFF><SERVICECODE>15001</SERVICECODE>"
                 "<DELIVERYTIME>201102151330</DELIVERYTIME></MSG>"
                 "</MSGLST></SESSION>"))))
   
   (testing "Request with multiple messages"
     (is (= (request-xml {:user "u" :password "p" 
                          :messages [{:receiver 4712345678 :text "Hi!"}
                                     {:receiver 4712345679 :text "Ho!"}]})
            (str "<?xml version=\"1.0\"?>\r\n"
                 "<SESSION><CLIENT>u</CLIENT><PW>p</PW><MSGLST>"
                 "<MSG><ID>1</ID><TEXT>Hi!</TEXT><RCV>4712345678</RCV></MSG>"
                 "<MSG><ID>2</ID><TEXT>Ho!</TEXT><RCV>4712345679</RCV></MSG>"
                 "</MSGLST></SESSION>")))))
