(ns collect-monthly-fee
  (:use pswincom.gateway
        clojure.contrib.strint
        [clojure.string :only [split]]))

(comment 
  " This could be a script to charge your members a monthly fee
    or to collect a monthly donation to charity.
  
    The script reads a list of phone number and amount pairs
    from a file and transforms it to SMS messages charging the
    owners of the numbers the correct amount."
    
    ; The contents of the file would look like this:

    47123450000 100
    47123450201 100
    47123450432 150
    47123451003 50

    ; and so on ...
    )

(defn subscription-message 
      "Converts a line like \"4700000001 150\" to a hash-map like
      {:receiver \"4700000001\"
       :tariff 15000
       :text \"You have been charged your monhtly amount of $150. Thank you!\"}"
      [line]
      (let [[number amount] (split line #"\s")
            tariff (* (Integer/parseInt amount) 100)]
        { :receiver number 
          :tariff tariff
          :text 
          (<< "You have been charged your monhtly amount of $~{amount}. Thank you!")}))

; The following expression reads the data file, converts it to messages,
; and sends them using the send-multiple-sms function:

(with-authentication "gw-user" "gw-passwd"
    (send-multiple-sms :sender "LiveAid"
                       :messages
                       (->> (split (slurp "data/subscriptions.txt") #"\r\n")
                            (map subscription-message))))
