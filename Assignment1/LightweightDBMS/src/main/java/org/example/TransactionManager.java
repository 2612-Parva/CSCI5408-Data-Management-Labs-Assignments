
package org.example;

import java.util.*;

/**
 * This class handles transaction processing, ensuring A.C.I.D. properties.
 */
public class TransactionManager {

    private PersistentStorage storage;
    private List<String> transactionChanges;
    private boolean inTransaction;

    /**
     * Constructor for initializing the transaction manager.
     *
     * @param storage The persistent storage object.
     */
    public TransactionManager(PersistentStorage storage) {
        this.storage = storage;
        this.transactionChanges = new ArrayList<>();
        this.inTransaction = false;
    }

    /**
     * Begins a new transaction.
     */
    public void beginTransaction() {
        if (inTransaction) {
            System.out.println("A transaction is already in progress.");
        } else {
            inTransaction = true;
            transactionChanges.clear();
            System.out.println("Transaction started.");
        }
    }

    /**
     * Commits the current transaction, applying all changes.
     */
    public void commitTransaction() {
        if (!inTransaction) {
            System.out.println("No transaction to commit.");
        } else {
            if (transactionChanges.isEmpty()) {
                System.out.println("No changes to commit.");
            } else {

                for (String change : transactionChanges) {
                    System.out.println("Committing change: " + change);
                }
                storage.saveDataToFile();
                inTransaction = false;
                transactionChanges.clear();
                System.out.println("Transaction committed.");
            }
        }
    }

    /**
     * Rolls back the current transaction, discarding all changes.
     */
    public void rollbackTransaction() {
        if (!inTransaction) {
            System.out.println("No transaction to rollback.");
        } else {
            transactionChanges.clear();
            inTransaction = false;
            System.out.println("Transaction rolled back.");
        }
    }

    /**
     * Returns whether a transaction is currently in progress.
     *
     * @return true if a transaction is in progress, false otherwise.
     */
    public boolean isInTransaction() {
        return inTransaction;
    }

    /**
     * Adds a change to the current transaction's list of changes.
     *
     * @param change The change description (e.g., "INSERT INTO tableName ...").
     */
    public void addChange(String change) {
        if (inTransaction) {
            transactionChanges.add(change);
            System.out.println("Change added to the transaction: " + change);
        } else {
            System.out.println("No transaction in progress. Cannot add change.");
        }
    }
}


