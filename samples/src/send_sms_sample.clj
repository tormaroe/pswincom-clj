(ns send-sms-sample
  (:use pswincom.gateway))

(with-authentication "user" "password"
   (send-sms 4712345678
             "This is the message"
             :sender "ACME"))
