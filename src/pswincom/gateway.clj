(ns pswincom.gateway
    (:use pswincom.gateway.xml)
    (:require [clojure.contrib.http.agent :as http]))

(def *username* nil) ; The PSWinCom Gateway account username
(def *password* nil) ; The PSWinCom Gateway account password
(def *gateway* "http://gw2-fro.pswin.com:81/") ; service endpoint
(def *sender* nil) ; If not nil, this will be used as sender

; This var can be rebound to override the actual sending
(def *sender-function* #'http/http-agent)

(defn- set-sender-if-missing [messages]
  (map #(if (nil? (:sender %))
          (assoc % :sender *sender*)
          %) 
       messages))

(defn- internal-send [messages]
  (*sender-function* *gateway*
                     :method "POST"
                     :headers { "Content-Type" "text/xml" }
                     :body (request-xml { :user *username* 
                                          :password *password* 
                                          :messages (set-sender-if-missing messages)})))

(defmacro with-gateway [g & exprs]
  `(binding [*gateway* ~g] ~@exprs))

(defmacro with-authentication [u p & exprs]
  `(binding [*username* ~u, *password* ~p] ~@exprs))

(defmacro with-config [m & exprs]
  `(binding [*username* (:username ~m) 
             *password* (:password ~m)
             *sender*   (:from ~m)
             *gateway*  (if-let [host# (:host ~m)]
                           host#
                           *gateway*)] 
        ~@exprs))

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
  (letfn [(build-message 
            [receiver text & {:keys [sender TTL tariff service-code delivery-time]}]
            {:receiver      receiver
             :text          text
             :sender        sender
             :TTL           TTL
             :tariff        tariff
             :service-code  service-code
             :delivery-time delivery-time})]
     (-> [(apply build-message 
                 receiver 
                 text 
                 options)]       
         internal-send)))


(defn send-multiple-sms 
  "Sends multiple SMS messages in a single gateway request.
   Properties that will be common for all messages are specified
   once. Properties unique to each message are provided as part of
   a messages sequence.
   
   An example:
   
   (send-multiple-sms :text \"This is the message\"
                      :sender \"ACME\"
                      :messages [{:receiver 4700000001 :tariff 100}
                                 {:receiver 4700000002 :tariff 500}
                                 {:receiver 4700000003 :tariff 200}]))"
  [& options]
  {:pre [(even? (count options))]}
  (let [split-on (fn [k m] [(k m) (dissoc m k)])
        [messages options-map] (split-on :messages 
                                         (apply hash-map 
                                                options))]
    (->> messages 
        (map (partial merge options-map))
        vec        
        internal-send)))



