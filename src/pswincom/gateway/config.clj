(ns pswincom.gateway.config
    (:require [clj-yaml.core :as yaml]))

(def pswincom-config 
      (future 
        (yaml/parse-string
          (slurp
            (str (System/getProperty "user.home")
                 "\\.pswincom")))))
