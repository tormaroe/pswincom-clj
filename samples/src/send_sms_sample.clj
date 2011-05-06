(ns send-sms-sample
  (:use pswincom.gateway))

(with-authentication "user" "password"
   (send-sms 4790696698
             "This is the message"
             :sender "ACME"))
