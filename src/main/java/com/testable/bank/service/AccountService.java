package com.testable.bank.service;

import com.testable.bank.model.Account;
import com.testable.bank.model.Account.AccountStatus;
import com.testable.bank.model.TransactionRecord;
import com.testable.bank.util.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core business-logic service for bank account operations.
 * Handles account lifecycle, credits, debits, and transfers.
 */
public class AccountService {

  private final Map<String, Account> accounts = new HashMap<>();
  private final List<TransactionRecord> ledger = new ArrayList<>();

  // ── Account lifecycle ────────────────────────────────────────────────────

  /**
   * Opens a new account and stores it.
   *
   * @param acctId  unique account reference code
   * @param holderRef opaque holder reference (non-PII)
   * @param initialBalance opening balance (may be zero)
   * @return newly created {@link Account}
   * @throws IllegalStateException when account with same reference already exists
   */
  public Account createAccount(
      final String acctId,
      final String holderRef,
      final BigDecimal initialBalance) {
    Validator.validateAccountId(acctId);
    Validator.validateHolderRef(holderRef);
    Validator.validateInitialBalance(initialBalance);
    if (accounts.containsKey(acctId)) {
      throw new IllegalStateException("Account already exists: " + acctId);
    }
    Account account = new Account(acctId, holderRef, initialBalance);
    accounts.put(acctId, account);
    return account;
  }

  /**
   * Credits a positive amount to an active account.
   *
   * @param acctId reference of the target account
   * @param amount positive amount to credit
   * @throws IllegalStateException when the account is not active
   */
  public void deposit(final String acctId, final BigDecimal amount) {
    Validator.validateAccountId(acctId);
    Validator.validateAmount(amount);
    Account account = requireActive(acctId);
    BigDecimal updated = account.getBalance().add(amount);
    account.setBalance(updated);
    ledger.add(new TransactionRecord(acctId, "DEPOSIT", amount, updated));
  }

  /**
   * Debits a positive amount from an active account.
   *
   * @param acctId reference of the target account
   * @param amount positive amount to debit
   * @throws IllegalStateException when funds are insufficient or account is not active
   */
  public void withdraw(final String acctId, final BigDecimal amount) {
    Validator.validateAccountId(acctId);
    Validator.validateAmount(amount);
    Account account = requireActive(acctId);
    if (account.getBalance().compareTo(amount) < 0) {
      throw new IllegalStateException("Insufficient funds: " + acctId);
    }
    BigDecimal updated = account.getBalance().subtract(amount);
    account.setBalance(updated);
    ledger.add(new TransactionRecord(acctId, "WITHDRAWAL", amount, updated));
  }

  /**
   * Moves funds atomically between two distinct active accounts.
   *
   * @param fromRef source account reference
   * @param toRef   destination account reference
   * @param amount  positive amount to move
   * @throws IllegalArgumentException when source and destination refs are identical
   */
  public void transfer(
      final String fromRef, final String toRef, final BigDecimal amount) {
    Validator.validateAccountId(fromRef);
    Validator.validateAccountId(toRef);
    Validator.validateAmount(amount);
    if (fromRef.equals(toRef)) {
      throw new IllegalArgumentException("Source and destination accounts must differ");
    }
    withdraw(fromRef, amount);
    deposit(toRef, amount);
  }

  // ── Batch / aggregate operations (loop paths) ────────────────────────────

  /**
   * Deposits amounts into multiple accounts in a single call.
   * Loop paths covered by tests: zero-trip, one-trip, n-trip.
   *
   * @param requests map of acctRef to amount
   * @return map of acctRef to new balance for each processed account
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

  /**
   * Sums balances across all accounts (loop over every entry).
   *
   * @return total balance across all registered accounts
   */
  public BigDecimal getTotalBalance() {
    BigDecimal total = BigDecimal.ZERO;
    for (Account account : accounts.values()) {
      total = total.add(account.getBalance());
    }
    return total;
  }

  /**
   * Returns references of accounts currently in ACTIVE state.
   * Loop with predicate filter covers both taken and not-taken branches.
   *
   * @return unmodifiable list of active account references
   */
  public List<String> findActiveAccountIds() {
    List<String> active = new ArrayList<>();
    for (Map.Entry<String, Account> entry : accounts.entrySet()) {
      if (entry.getValue().isActive()) {
        active.add(entry.getKey());
      }
    }
    return Collections.unmodifiableList(active);
  }

  /**
   * Returns the withdrawable balance above a required minimum (nested condition path).
   * Covers three nested-if paths: not-active, active-insufficient, active-surplus.
   *
   * @param acctId  target account reference
   * @param minBal  reserve that must remain after withdrawal
   * @return withdrawable surplus, or {@link BigDecimal#ZERO} when none
   */
  public BigDecimal getWithdrawableBalance(
      final String acctId, final BigDecimal minBal) {
    Validator.validateAccountId(acctId);
    Validator.validateInitialBalance(minBal);
    Account account = requireExists(acctId);
    if (account.isActive()) {
      BigDecimal surplus = account.getBalance().subtract(minBal);
      if (surplus.compareTo(BigDecimal.ZERO) > 0) {
        return surplus;
      }
    }
    return BigDecimal.ZERO;
  }

  // ── Query ────────────────────────────────────────────────────────────────

  /**
   * Looks up an account by reference without mutating state.
   *
   * @param acctId account reference to search
   * @return {@link Optional} wrapping the account, empty when not found
   */
  public Optional<Account> findAccount(final String acctId) {
    Validator.validateAccountId(acctId);
    return Optional.ofNullable(accounts.get(acctId));
  }

  /** Returns an unmodifiable view of the transaction ledger. */
  public List<TransactionRecord> getLedger() {
    return Collections.unmodifiableList(ledger);
  }

  // ── Status management ────────────────────────────────────────────────────

  /**
   * Transitions an active account to SUSPENDED state.
   *
   * @param acctId target account reference
   * @throws IllegalStateException when the account is not currently active
   */
  public void suspendAccount(final String acctId) {
    requireActive(acctId).setStatus(AccountStatus.SUSPENDED);
  }

  /**
   * Permanently closes an account that has a zero balance.
   *
   * @param acctId target account reference
   * @throws IllegalStateException when balance is non-zero
   */
  public void closeAccount(final String acctId) {
    Account account = requireExists(acctId);
    if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
      throw new IllegalStateException(
          "Cannot close account with non-zero balance: " + acctId);
    }
    account.setStatus(AccountStatus.CLOSED);
  }

  // ── Private helpers ──────────────────────────────────────────────────────

  private Account requireActive(final String acctId) {
    Account account = requireExists(acctId);
    if (!account.isActive()) {
      throw new IllegalStateException("Account is not active: " + acctId);
    }
    return account;
  }

  private Account requireExists(final String acctId) {
    return Optional.ofNullable(accounts.get(acctId))
        .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));
  }
}
