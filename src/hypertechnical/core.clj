(ns hypertechnical.core
  (:use 
    [clojure.string :only [join split blank?]]
    [twitter.oauth]
    [twitter-streaming-client.core :as client]
    [twitter.callbacks]
    [twitter.callbacks.handlers]
    [twitter.api.restful]
    [twitter.api.streaming]
    [clojure.pprint]
    [clojure.set :only [select]])
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
  (let [screen_name (:screen_name (:user tweet))
        status_id (:id tweet)
        message (str "@" screen_name " Computers even by setting the lowest order bit to False, duh!")]

    (log/info "Sending" message "to" screen_name ". status_id:" status_id)
    (statuses-update :oauth-creds twitter-credentials
                     :params {:status message :in_reply_to_status_id status_id}))) 

(defn about-computers-evening? [tweet]
  (let [message (:text tweet)
        patterns #{#"[cC]omputers[,]?\show do they even[\?]?" #"[hH]ow do computers even[\?]?$"}]

    (not-empty (select (fn [pattern] (re-find pattern message)) patterns))))

(defn -main [& args]
  (client/start-twitter-stream stream)

  (while 
    true
    (time (Thread/sleep (* 1024 5)))
    (let [raw-tweets (:tweet (client/retrieve-queues stream))
          filtered-tweets (select about-computers-evening? raw-tweets)]

      (if-not (nil? filtered-tweets)
        (doseq [t filtered-tweets] (handle-matching-tweet t))))))
