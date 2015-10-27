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

(def search-terms
  "computers how do they even, how do computers even")

(def search-patterns
  #{#"[cC]omputers[,]?\show do they even[\?]?" #"[hH]ow do computers even[\?]?$"})

(def twitter-credentials 
  (make-oauth-creds (System/getenv "TWITTER_CONSUMER_KEY")
                    (System/getenv "TWITTER_CONSUMER_SECRET")
                    (System/getenv "TWITTER_ACCESS_TOKEN")
                    (System/getenv "TWITTER_ACCESS_TOKEN_SECRET")))

(def stream
  (client/create-twitter-stream 
    statuses-filter :oauth-creds twitter-credentials 
                    :params {:track search-terms}))

(defn response-text [screen_name]
  (str "@" screen_name " Computers even by setting the lowest order bit to False, duh!"))

(defn tweet-response [tweet]
  (let [screen-name (:screen_name (:user tweet))
        status-id (:id tweet)
        message (response-text screen-name)] 

    (log/info "Sending" message "to" screen-name ". status_id:" status-id)
    (statuses-update :oauth-creds twitter-credentials
                     :params {:status message :in_reply_to_status_id status-id}))) 

(defn about-computers-evening? [tweet]
  (let [message (:text tweet)]

    (not-empty 
      (select (fn [pattern] (re-find pattern message)) search-patterns))))

; TODO: figure out better names for all this.
(defn process-tweets [tweets]
  (let [filtered-tweets (select about-computers-evening? tweets)]
    (if-not (nil? filtered-tweets)
      (doseq [t filtered-tweets] (tweet-response t)))))

(defn -main [& args]
  (client/start-twitter-stream stream)

  (while true
    (process-tweets (:tweet (client/retrieve-queues stream)))
    (Thread/sleep (* 1024 5))))
