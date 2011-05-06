(ns pswincom.gateway.xml
  (:use clojure.contrib.strint))

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
