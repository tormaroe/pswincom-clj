(ns pswincom.intouch
    "
    This lib uses the PSWinCom Intouch REST API. You will need an
    Intouch account with the API-feature enabled to use it.

    Technical specifications are located at:
    http://wiki.pswin.com/Intouch-REST-API.ashx
    "
    (:use clojure.contrib.json
          [clojure.string :only [join]]
          [clojure.contrib.core :only [seqable?]])
    (:require [clojure.contrib.http.agent :as http]
              [clojure.contrib.base64     :as b64 ])
    (:import (java.util Calendar)))

(def *username* nil) ; The PSWinCom Intouch account username
(def *password* nil) ; The PSWinCom Intouch account password
(def *domain*   nil) ; The PSWinCom Intouch account domain
(def *api-url* "http://intouchapi.pswin.com/1/") ; service endpoint
(def *debug* (atom false))

(defn toggle-debug! 
      "Sets value of *debug* to (not @*debug*).
       Retuns the new value. *debug* starts out as false.
       
       When *debug* is true, some debug information will
       be printed to *out* during API usage."
      []
      (swap! *debug* #(not %)))

(defmacro with-intouch [g & exprs]
  `(binding [*api-url* ~g] ~@exprs))

(defmacro with-authentication [u p d & exprs]
  `(binding [*username* ~u, *password* ~p, *domain* ~d] ~@exprs))

(defn- authorization 
       "Prepares/encodes token for basic authentication"
       []
       (->> (str *username* "@" *domain* ":" *password*)
            b64/encode-str
            (str "Basic ")))

(defn api-headers []
      { "Authorization" (authorization)
        "Accept"        "application/json"
        "Content-Type"  "application/json" })

(defn json-date [d]
      (format "/Date(%ts000+0100)/" d)) ; TODO: this assumes a one hour offset!

(defn minutes-from-now [n]
      (let [now (Calendar/getInstance)]
        (. now add Calendar/MINUTE n)
        now))

(defn resources 
      "Main API function which can be used on any resource with any http verb."
      ([r          ] (resources r "GET" nil))
      ([r verb     ] (resources r verb  nil))
      ([r verb body]
        (let [agnt (http/http-agent (str *api-url* r)
                                    :method verb
                                    :body (if (map? body)
                                            (json-str body)
                                            body)
                                    :headers (api-headers))]
          ; Calling result on agnt below is a fix to a bug, see: 
          ; http://www.mail-archive.com/clojure@googlegroups.com/msg20018.html
          (http/result agnt)
          (when @*debug*
            (println "Status" (http/status agnt))
					  (println (http/headers agnt)))
          (if (= "application/json" 
                 ((http/headers agnt) :content-type))
            (read-json (http/string agnt))
            (http/string agnt)))))

(defn delete-resource [r]
      (resources r "DELETE" nil))

(defn new-resource [r data]
      (resources r "POST" data))

(defn update-resource 
      "Does a GET on resource, merges in new values, 
       then PUTs the result back to the same resource."
      [r new-values]
      (resources r "PUT" 
           (json-str (merge (resources r) 
                            new-values))))

(defn- receivers-part [r-key data]
       (letfn [(create-part [x] 
                            (if (= :numbers r-key)
                              (str x)
                              (str "/" (name r-key) "/" x)))]
         (let [data (r-key data)]
           (when data
             (if (seqable? data)
               (join ";" (map create-part data))
               (create-part data))))))

(defn receivers [& args]
      (let [args-map (apply hash-map args)]
        (join ";" 
               (filter (complement nil?) 
                       (list (receivers-part :numbers args-map)
                             (receivers-part :groups args-map)
                             (receivers-part :contacts args-map))))))

(defn send-message [to text & options]
      (resources "messages" "POST"
           (merge {:Text    text
                   :Receivers      to
                   :Tariff         0
                   :IsMergeMessage false
                   :MessageStatus  "Unknown"
                   :IsPrivate      false} 
                   (apply hash-map options))))
