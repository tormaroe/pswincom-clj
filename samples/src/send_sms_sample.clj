(ns send-sms-sample
  (:use pswincom.gateway))

(with-authentication "tormar" "CrtSoCBngevzMZQvEFgU"
   (send-sms 4790696698
             "This is the message"
             :sender "ACME"))
