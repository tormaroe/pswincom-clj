(ns pswincom.gateway
    (:use pswincom.gateway.xml)
    (:require [clojure.contrib.http.agent :as http]))

(def *username* nil) ; The PSWinCom Gateway account username
(def *password* nil) ; The PSWinCom Gateway account password
(def *gateway* "http://gw2-fro.pswin.com:81/") ; service endpoint

(def *sender-function* #'http/http-agent)

(defmacro with-gateway [g & exprs]
  `(binding [*gateway* ~g]
      ~@exprs))

(defmacro with-authentication [u p & exprs]
  `(binding [*username* ~u
             *password* ~p]
      ~@exprs))

(defn- build-sms [receiver text & {:keys [sender TTL tariff service-code delivery-time]}]
       {:user     *username* 
        :password *password*
        :messages [{:receiver      receiver
                    :text          text
                    :sender        sender
                    :TTL           TTL
                    :tariff        tariff
                    :service-code  service-code
                    :delivery-time delivery-time}]})

(defn send-sms 
 "Sends SMS message with text to receiver. Supports optional
  modifiers: :sender, :TTL, :tariff, :service-code, 
  :delivery-time.

  (send-sms 4712345678 \"Test\" :sender \"ACME\")

  The vars *username* and *password* should have been set prior
  to calling send-sms, for example by using the with-authentication
  macro.

  Message will be sent to the service uri stored in the *gateway* 
  var, which can be changed with the with-gateway macro.

  Message will be sent using the function stored in *sender-function*."
  [receiver text & options]
  {:pre [(even? (count options))]}
  (*sender-function* *gateway*
                     :method "POST"
                     :headers { "Content-Type" "text/xml" }
                     :body (request-xml (apply build-sms 
                                               receiver 
                                               text 
                                               options))))
