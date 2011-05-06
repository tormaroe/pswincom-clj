(ns pswincom.test.gateway
  (:use [pswincom.gateway.xml])
  (:use [pswincom.gateway] :reload)
  (:use [clojure.test]))

(deftest with-authentication-spec
   (letfn [(assert-auth [u p]
               (is (= u *username*))
               (is (= p *password*)))]

      (assert-auth nil nil)
      (with-authentication "user1" "pa$$w0rd"         
         (assert-auth "user1" "pa$$w0rd"))   
      (assert-auth nil nil)))

(deftest send-sms-spec
 (testing "that send-sms dispatches to function stored in *sender-function*,
           that the value of *gateway* is used as host,
           that the method is POST,
           that the request body is as produced by pswincom.gateway.xml/request-xml"

   ; Mocking the sender function
   (let [sender-was-called (atom nil)]
     (binding [*sender-function* (fn [host & args]
                                     (swap! sender-was-called #(or % true))
                                     (doseq [[key val] (partition 2 args)]
                                        (case key
                                          :method (is (= "POST" val))
                                          :body (is (= (request-xml {:user nil :password nil
                                                                     :messages [{:receiver 4712345678
                                                                                 :text "Some text"
                                                                                 :sender "s3nd3r"}]}) val))
                                          :headers (is (= { "Content-Type" "text/xml" } val))))
                                     (is (= host *gateway*)))]
        (send-sms 4712345678 
                  "Some text" 
                  :sender "s3nd3r")
        (is (true? @sender-was-called))))))

