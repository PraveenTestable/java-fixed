package com.testable.bank.service;

import com.testable.bank.model.Account;
import com.testable.bank.model.TransactionRecord;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Batch deposit operations over the shared account registry.
 * Kept separate from AccountService to reduce per-class WMC/LOC.
 */
public class AccountBatchService {

  private final Map<String, Account> accounts;
  private final List<TransactionRecord> ledger;

  /**
   * Constructs the batch service backed by the given registry and ledger.
   *
   * @param accounts shared account map (must not be null)
   * @param ledger   shared transaction ledger (must not be null)
   */
  public AccountBatchService(
      final Map<String, Account> accounts,
      final List<TransactionRecord> ledger) {
    this.accounts = accounts;
    this.ledger = ledger;
  }

  /**
   * Deposits amounts into multiple accounts in a single call.
   * Loop paths covered by tests: zero-trip, one-trip, n-trip.
   *
   * @param requests map of acctRef to deposit amount
   * @return map of acctRef to new balance for each successfully processed account
   * @throws IllegalArgumentException when requests is null
   */
  public Map<String, BigDecimal> batchDeposit(final Map<String, BigDecimal> requests) {
    if (requests == null) {
      throw new IllegalArgumentException("Requests map must not be null");
    }
    Map<String, BigDecimal> results = new HashMap<>();
    for (Map.Entry<String, BigDecimal> entry : requests.entrySet()) {
      String ref = entry.getKey();
      BigDecimal amt = entry.getValue();
      if (accounts.containsKey(ref)) {
        Account account = accounts.get(ref);
        if (account.isActive() && amt != null && amt.compareTo(BigDecimal.ZERO) > 0) {
          BigDecimal updated = account.getBalance().add(amt);
          account.setBalance(updated);
          ledger.add(new TransactionRecord(ref, "BATCH_DEPOSIT", amt, updated));
          results.put(ref, updated);
        }
      }
    }
    return results;
  }
}
