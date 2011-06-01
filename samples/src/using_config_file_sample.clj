(ns using-config-file-sample
    (:use pswincom.gateway
          pswincom.gateway.config)
    (:require clojure.string))

(defn remove-excess-whitespace [s]
    (clojure.string/replace 
      (clojure.string/replace s "\n" " ")
      #"\ \ +"
      " "))

(with-config @pswincom-config 
    (send-sms 4790696698 
              (remove-excess-whitespace 
                "Message sent with sender, username, 
                password and (possibly) host from .pswincom 
                yaml-config file in users home directory.")))
