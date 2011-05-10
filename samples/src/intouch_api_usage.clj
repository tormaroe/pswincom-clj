(ns intouch-api-usage
    (:use pswincom.intouch))

(with-intouch "http://intouchapi.pswin.com/1/" ; overrides the default...

  (with-authentication "username" "password"

     ;; Querying for data ------------------------------------------------------------------------

     (resources "groups")                        ;=> Returns all groups
     (resources "groups/45")                     ;=> Returns group 45
     (resources "groups/45/contacts")            ;=> Returns all contacts in groups 45

     ;; Deleting data ----------------------------------------------------------------------------

     (delete-resource "groups/45/contacts/2039") ;=> Removes contact 2039 from group 45

     ;; Creating new data ------------------------------------------------------------------------

     (new-resource "contacts"
                   { :CPAAccepted false
                     :Description "Some description"
                     :Firstname   "William"
                     :Lastname    "Gates"
                     :IsPrivate   false
                     :PhoneNumber "90555812" })   ;=> Creates a new contact

     ;; Updating existing data -------------------------------------------------------------------
     
     (update-resource "contacts/345"
                      { :Description "Updated description"
                        :Email       "bill@microsoft.com" }) ;=> Updates two fields on contact 345

     ;; Helper function for sending message ------------------------------------------------------

     (send-message 12345678 "This is a message!") ;=> Send a message to number with defaults

     ; You can specify additional options, like this:

     (send-message 12345678 "This is a message!"
                   :SenderNumber "FooBar"         ;=> Override sender number (if account allows)
                   :HandledDate (json-date (minutes-from-now 30)))   ;=> Postpone for 30 minutes
))