(ns nco-admin.store
  "NCO Admin Store — the append-only audit ledger and persistent
  state for the ISCO-08 0210 Non-Commissioned Officer administrative assistant
  actor. Implements the Store protocol for NCO/unit identity verification
  and administrative record management.

  Two backends implement the same `Store` protocol so the backend is a
  swap, not a rewrite (the itonami actor pattern's injection boundary,
  ADR-2607011000; mirrors `officer-admin.store`, cloud-itonami-isco-0110
  — the same domain family, already fixed):

    - `MemStore`     — a persistent (immutable) record wrapping plain
                       maps. The deterministic default for dev/tests/demo
                       (no deps); mutator methods return a NEW store.
    - `DatomicStore` — backed by `langchain.db`, a Datomic-API-compatible
                       EAV store (swappable to a kotoba-server pod in
                       production). NCO/unit records and ledger entries
                       carry free-form fields, so each is stored as an
                       EDN-blob payload via `langchain-store.core`
                       (`ls/enc`/`ls/dec*`), not a hand-rolled codec
                       (ADR-2607141600). Mutator methods return the SAME
                       store (the conn is already mutable), which is
                       still safe to `->`-thread the way the tests do.

  Both pass the same contract (test/nco_admin/store_contract_test.cljc).

  `add-record!`/`records` is the append-only audit ledger:
  `nco-admin.actor`'s `:commit`/`:hold` graph nodes append every
  committed administrative record AND every hard-governance-violation
  hold fact here, so an NCO's administrative history (every
  `:schedule-training` / `:log-readiness-report` /
  `:draft-correspondence` / `:process-leave-request` decision,
  committed or held) is always a query over an immutable log. Prior to
  this, `add-record!` was only ever called from tests — dead code from
  `nco-admin.actor`'s point of view."
  (:require [langchain.db :as d]
            [langchain-store.core :as ls]))

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

(defn- now-ms []
  #?(:clj (System/currentTimeMillis)
     :cljs (.getTime (js/Date.))))

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
    (let [record (assoc record-data :type record-type :timestamp (now-ms))]
      (MemStore. ncos units (conj ledger record))))
  (records [this]
    ledger))

(defn create-store
  "Create a new in-memory store for NCO admin records."
  []
  (MemStore. {} {} []))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  "DataScript/Datomic-style schema: only constraint attrs are declared.
  `:nco/payload`/`:unit/payload` are opaque EDN-string blobs (via
  `langchain-store.core`) so `langchain.db` doesn't try to expand a
  caller-defined record into sub-entities — same convention as
  `officer-admin.store`'s `:officer/payload`/`:unit/payload`."
  (ls/identity-schema [:nco/id :unit/id :record/seq]))

(defn- blob-lookup
  "Look up the EDN-blob payload for the entity uniquely identified by
  `id-attr`/`id` and stored under `payload-attr`."
  [conn id-attr payload-attr id]
  (when id
    (ls/dec* (d/q {:find '[?p .] :in '[$ ?id]
                   :where [['?e id-attr '?id] ['?e payload-attr '?p]]}
                  (d/db conn) id))))

(defrecord DatomicStore [conn]
  Store
  (nco [_ nco-id]
    (blob-lookup conn :nco/id :nco/payload nco-id))
  (unit [_ unit-id]
    (blob-lookup conn :unit/id :unit/payload unit-id))
  (register-nco! [s nco-id nco-data]
    (d/transact! conn [{:nco/id nco-id :nco/payload (ls/enc nco-data)}])
    s)
  (register-unit! [s unit-id unit-data]
    (d/transact! conn [{:unit/id unit-id :unit/payload (ls/enc unit-data)}])
    s)
  (add-record! [s record-type record-data]
    (let [record (assoc record-data :type record-type :timestamp (now-ms))]
      (ls/append-blob! conn :record/seq :record/payload (count (records s)) record))
    s)
  (records [_]
    (ls/read-stream conn :record/seq :record/payload)))

(defn datomic-store
  "Create a new DatomicStore (langchain.db-backed) for NCO admin
  records — the production-shaped backend for the same `Store`
  protocol `create-store`'s `MemStore` implements."
  []
  (->DatomicStore (d/create-conn schema)))
