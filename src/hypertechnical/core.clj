(ns hypertechnical.core
  (:use [clojure.string :only [join split blank?]])
  (:gen-class))

(defn message_about_computers [message]
  "Super gross function as far as I'm concerned. There's got to be a better way"
  (not (blank? (re-find #"how\sdo\sthey\seven|computers" message))))

(defn respond_with_computer_information [about_computers]
  (if about_computers
    (str "By checking the lowest order bit, duh!")
    (str "I got nothing..."))) 

(defn -main
  [& args]

  (println (respond_with_computer_information 
             (message_about_computers 
               (join "" args))))) 
