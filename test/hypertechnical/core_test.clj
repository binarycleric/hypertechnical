(ns hypertechnical.core-test
  (:require [clojure.test :refer :all]
            [hypertechnical.core :refer :all]))

(def tweets-about-computers-evening
  '["computers how do they even. seriously!"
    "Computers! How do they even?!" 
    "Computers! How do they even?" 
    "Computers, how do they even?"
    "Computers how do they even?"
    "Computers, how do they even"
    "Computers how do they even"
    "Computers. How do they even."
    "How do computers even?"
    "how do computers even?"
    "How do computers even"
    "how do computers even"])

(deftest testing-language-checker
  (testing "Tweet is not about computers evening"
    (is (not (about-computers-evening? 
          {:text "Something about Call of Duty"})))) 

  (testing "Tweets are about computers evening"
    (doseq [message tweets-about-computers-evening]
      (is (about-computers-evening? {:text message}))))) 
