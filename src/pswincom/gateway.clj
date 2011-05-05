(ns pswincom.gateway
    (:use clojure.contrib.strint)
    (:require [clojure.contrib.http.agent :as http]))

(defn- datetime-format [d]
       ; The ugliest format string ever?
       (format "%1$tY%1$tm%1$td%1$tH%1$tM" d))

(defn- <> 
  "This must be the simplest XML emitter in history :)"
  [tag & content]
  (<< "<~(name tag)>~(apply str content)</~(name tag)>"))

(defn- <optional> 
  ([tag content]
    (<optional> tag content identity))
  ([tag content formater]
    (when content
      (<> tag (formater content)))))

(defn request-xml 
  "Generate the XML for a send message request"
  [args]
  (str "<?xml version=\"1.0\"?>\r\n"
       (<> :SESSION 
            (<> :CLIENT (:user args))
            (<> :PW (:password args))
            (<> :MSGLST 
                (when-let [messages (:messages args)]
                   (apply str 
                          (for [i (range (count messages))
                                :let [m (messages i)]]
                             (<> :MSG
                                 (<> :ID (inc i))
                                 (<> :TEXT (:text m))
                                 (<> :RCV (:receiver m))
                                 (<optional> :SND (:sender m))
                                 (<optional> :TTL (:TTL m))
                                 (<optional> :TARIFF (:tariff m))
                                 (<optional> :SERVICECODE (:service-code m))
                                 (<optional> :DELIVERYTIME (:delivery-time m)
                                             datetime-format)))))))))

(def *username* nil)
(def *password* nil)
(def *gateway* "http://gw2-fro.pswin.com:81/")

(def *sender-function* http/http-agent)

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
      "doc todo"
      [receiver text & options]
      (*sender-function* *gateway*
                         :method "POST"
                         :headers { "Content-Type" "text/xml" }
                         :body (request-xml (apply build-sms 
                                                   receiver 
                                                   text 
                                                   options))))
