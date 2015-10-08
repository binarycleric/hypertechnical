(ns hypertechnical.core-test
  (:require [clojure.test :refer :all]
            [hypertechnical.core :refer :all]))

(deftest message_is_not_about_computers
  (def text_not_about_computers
    "I like cats.")

  (testing "returns false when tweet is about computers"
    (is (= false (message_about_computers text_not_about_computers)))))

(deftest message_is_about_computers 
  (def text_about_computers
    "Computers, how do they even?")
 
  (testing "returns true when tweet is about computers"
   (is (= true (message_about_computers text_about_computers))))) 
