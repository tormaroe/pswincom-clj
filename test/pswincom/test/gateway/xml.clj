(ns pswincom.test.gateway.xml
  (:use [pswincom.gateway.xml] :reload)
  (:use [clojure.test])
  (:import (java.util Calendar)))

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
