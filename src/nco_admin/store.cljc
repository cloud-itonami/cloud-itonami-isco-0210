(ns nco-admin.store
  "NCO Admin Store — the append-only audit ledger and persistent
  state for the ISCO-08 0210 Non-Commissioned Officer administrative assistant
  actor. Implements the Store protocol for NCO/unit identity verification
  and administrative record management.")

(defprotocol Store
  "Store protocol for NCO admin actor state and audit ledger."
  (nco [store nco-id]
    "Retrieve an NCO record by ID. Returns nil if not found.")
  (unit [store unit-id]
    "Retrieve a unit record by ID. Returns nil if not found.")
  (register-nco! [store nco-id nco-data]
    "Register an NCO (adds to store, returns updated store).")
  (register-unit! [store unit-id unit-data]
    "Register a unit (adds to store, returns updated store).")
  (add-record! [store record-type record-data]
    "Append an immutable administrative record to the audit ledger.")
  (records [store]
    "Return all records in the audit ledger (immutable)."))

(defrecord MemStore [ncos units ledger]
  Store
  (nco [this nco-id]
    (get ncos nco-id))
  (unit [this unit-id]
    (get units unit-id))
  (register-nco! [this nco-id nco-data]
    (MemStore. (assoc ncos nco-id nco-data) units ledger))
  (register-unit! [this unit-id unit-data]
    (MemStore. ncos (assoc units unit-id unit-data) ledger))
  (add-record! [this record-type record-data]
    (let [record (assoc record-data :type record-type :timestamp (System/currentTimeMillis))]
      (MemStore. ncos units (conj ledger record))))
  (records [this]
    ledger))

(defn create-store
  "Create a new in-memory store for NCO admin records."
  []
  (MemStore. {} {} []))
