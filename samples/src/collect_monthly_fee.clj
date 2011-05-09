(ns collect-monthly-fee
  (:use pswincom.gateway))

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

; TODO TODO TODO
