(ns hypertechnical.core
  (:use 
    [clojure.string :only [join split blank?]]
    [twitter.oauth]
    [twitter-streaming-client.core :as client]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.restful]
    [twitter.api.streaming]
    [clojure.pprint])
  (:require
   [clojure.data.json :as json]
   [http.async.client :as ac]
   [clojure.tools.logging :as log])
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)
   (twitter.callbacks.protocols SyncSingleCallback))
  (:gen-class))

(def twitter-credentials 
  (make-oauth-creds (System/getenv "TWITTER_CONSUMER_KEY")
                    (System/getenv "TWITTER_CONSUMER_SECRET")
                    (System/getenv "TWITTER_ACCESS_TOKEN")
                    (System/getenv "TWITTER_ACCESS_TOKEN_SECRET")))

(def stream
  (let [search_terms "computers how do they even, how do computers even"] 
    (client/create-twitter-stream statuses-filter :oauth-creds twitter-credentials 
                                                  :params {:track search_terms})))

(defn handle-matching-tweet [tweet]
  (let [screen_name (get (get tweet :user) :screen_name)
        status_id (get tweet :id)
        message (str "@" screen_name " Computers even by setting the lowest order bit to False, duh!")]

    (log/info "Sending" message "to" screen_name ". status_id:" status_id)
    (statuses-update :oauth-creds twitter-credentials
                     :params {:status message :in_reply_to_status_id status_id}))) 

(defn -main [& args]
  (client/start-twitter-stream stream)

  (while 
    true
    (time (Thread/sleep (* 1024 5)))
    (let [tweets (get (client/retrieve-queues stream) :tweet)]
      (if-not (nil? tweets)
        (doseq [t tweets] (handle-matching-tweet t)))))) 
