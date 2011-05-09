(ns send-sms-sample
  (:use pswincom.gateway))

; Sending a simple message is really easy...

(with-authentication "user" "password"
   (send-sms 4712345678 "This is the message"))

; But you can also specify a lot of options, like this...

(with-authentication "user" "password"
   (send-sms 4712345678 "This is the message"             
             :tariff 500 ; cents
             :TTL 5 ; minutes
             :sender "ACME"))

; You can specify a different gateway...

(with-gateway "http://gw2-osl.pswin.com/"
  (with-authentication "user" "password"
    (send-sms 4712345678 "This is the message")))

; Sending multiple messages with some common properties...

(with-authentication "user" "password"
   (send-multiple-sms :text "This is the message"
                      :sender "ACME"
                      :messages [{:receiver 4700000001 :tariff 100}
                                 {:receiver 4700000002 :tariff 500}
                                 {:receiver 4700000003 :tariff 200}
                                 {:receiver 4700000004 :tariff 100}]))
