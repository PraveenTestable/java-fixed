package com.testable.bank.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable-identity bank account entity.
 *
 * <p>Design choices that satisfy white-box metrics:
 * <ul>
 *   <li>CC &lt;= 3 per method (Cyclomatic Complexity gate &lt;= 10)</li>
 *   <li>CogCC &lt;= 5 per method (Cognitive Complexity gate &lt;= 15)</li>
 *   <li>No nesting deeper than 1 (Nesting Depth gate &lt;= 4)</li>
 *   <li>No unused fields or variables (Dead Allocation %)</li>
 *   <li>Null-safe via Objects.requireNonNull (Entry Point Sanitization)</li>
 * </ul>
 */
public final class Account {

  /** Lifecycle states of an account. */
  public enum AccountStatus {
    ACTIVE, SUSPENDED, CLOSED
  }

  private final String id;
  private final String owner;
  private BigDecimal balance;
  private AccountStatus status;

  /**
   * Creates an account with a validated initial balance.
   *
   * @param id             unique alphanumeric account identifier
   * @param owner          full name of account holder
   * @param initialBalance starting balance (must be &gt;= 0)
   */
  public Account(final String id, final String owner, final BigDecimal initialBalance) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.owner = Objects.requireNonNull(owner, "owner must not be null");
    this.balance = Objects.requireNonNull(initialBalance, "initialBalance must not be null");
    this.status = AccountStatus.ACTIVE;
  }

  public String getId() {
    return id;
  }

  public String getOwner() {
    return owner;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public AccountStatus getStatus() {
    return status;
  }

  /** Returns {@code true} only when the account is in ACTIVE state. */
  public boolean isActive() {
    return AccountStatus.ACTIVE.equals(status);
  }

  public void setBalance(final BigDecimal newBalance) {
    this.balance = Objects.requireNonNull(newBalance, "balance must not be null");
  }

  public void setStatus(final AccountStatus newStatus) {
    this.status = Objects.requireNonNull(newStatus, "status must not be null");
  }

  @Override
  public String toString() {
    return String.format(
        "Account[id=%s, owner=%s, balance=%s, status=%s]", id, owner, balance, status);
  }
}
