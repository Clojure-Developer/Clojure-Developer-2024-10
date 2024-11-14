(ns otus-02.homework.square-code-test
  (:require
    [clojure.test :refer :all]
    [otus-02.homework.square-code :as sut]))


(deftest encode-string-test
  (is (= "imtgdvsfearwermayoogoanouuiontnnlvtwttddesaohghnsseoau"
         (sut/encode-string "If man was meant to stay on the ground, god would have given us roots.")))

  (is (= "ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots"
         (sut/decode-string "imtgdvs fearwer mayoogo anouuio ntnnlvt wttddes aohghn  sseoau ")))

  (is (= "ifmanwasmeanttostayonthegroundgodwouldhavegivenusroots"
         (sut/decode-string
           (sut/encode-string "If man was meant to stay on the ground, god would have given us roots.")))))

