(ns nco-admin.store-test
  (:require [clojure.test :refer [deftest is testing]]
            [nco-admin.store :as store]))

(deftest test-create-store
  (testing "Creating a store produces an empty MemStore"
    (let [s (store/create-store)]
      (is (= [] (store/records s))))))

(deftest test-register-nco
  (testing "Registering an NCO adds them to the store"
    (let [s (-> (store/create-store)
               (store/register-nco! "nco-001" {:name "Test NCO" :rank "Sergeant"}))
          nco (store/nco s "nco-001")]
      (is (not (nil? nco)))
      (is (= "Test NCO" (:name nco))))))

(deftest test-register-unit
  (testing "Registering a unit adds it to the store"
    (let [s (-> (store/create-store)
               (store/register-unit! "unit-001" {:name "Alpha Company"}))
          unit (store/unit s "unit-001")]
      (is (not (nil? unit)))
      (is (= "Alpha Company" (:name unit))))))

(deftest test-add-record
  (testing "Adding a record appends it to the audit ledger"
    (let [s (-> (store/create-store)
               (store/add-record! :training {:training-id "tr-001" :duration 8}))
          records (store/records s)]
      (is (= 1 (count records)))
      (is (= :training (:type (first records)))))))

(deftest test-immutable-records
  (testing "Records are immutable (append-only ledger)"
    (let [s1 (-> (store/create-store)
                (store/add-record! :readiness {:status :nominal}))
          s2 (store/add-record! s1 :correspondence {:subject "Letter"})
          records1 (store/records s1)
          records2 (store/records s2)]
      (is (= 1 (count records1)))
      (is (= 2 (count records2))))))

(deftest test-nco-not-found
  (testing "Querying a non-existent NCO returns nil"
    (let [s (store/create-store)
          nco (store/nco s "nonexistent")]
      (is (nil? nco)))))

(deftest test-unit-not-found
  (testing "Querying a non-existent unit returns nil"
    (let [s (store/create-store)
          unit (store/unit s "nonexistent")]
      (is (nil? unit)))))
