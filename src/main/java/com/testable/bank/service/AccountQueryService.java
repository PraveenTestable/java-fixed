package com.testable.bank.service;

import com.testable.bank.model.Account;
import com.testable.bank.util.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Read-only query operations over the shared account registry.
 * Kept separate from AccountService to reduce per-class WMC/LOC.
 */
public class AccountQueryService {

  private final Map<String, Account> accounts;

  /**
   * Constructs the query service backed by the given registry.
   *
   * @param accounts shared account map (must not be null)
   */
  public AccountQueryService(final Map<String, Account> accounts) {
    this.accounts = accounts;
  }

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

  /**
   * Sums balances across all registered accounts.
   *
   * @return total balance (zero when no accounts exist)
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
   * Returns the withdrawable balance above a required minimum.
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
    Account account = accounts.get(acctId);
    if (account == null) {
      throw new IllegalArgumentException("Account not found: " + acctId);
    }
    if (account.isActive()) {
      BigDecimal surplus = account.getBalance().subtract(minBal);
      if (surplus.compareTo(BigDecimal.ZERO) > 0) {
        return surplus;
      }
    }
    return BigDecimal.ZERO;
  }
}
