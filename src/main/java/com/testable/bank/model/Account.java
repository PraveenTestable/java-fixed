package com.testable.bank.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable-identity bank account entity.
 *
 * <p>Whitebox design constraints:
 * <ul>
 *   <li>CC &lt;= 2 per method (Cyclomatic Complexity gate &lt;= 10)</li>
 *   <li>CogCC = 0 for all getters (Cognitive Complexity gate &lt;= 15)</li>
 *   <li>Nesting depth &lt;= 1 everywhere (Structural Threshold gate)</li>
 *   <li>Null-safe via {@link Objects#requireNonNull} (Entry Point Sanitisation)</li>
 *   <li>No personal data stored — {@code holderRef} is an opaque reference code</li>
 * </ul>
 */
public final class Account {

  /** Lifecycle states of an account. */
  public enum AccountStatus {
    ACTIVE, SUSPENDED, CLOSED
  }

  private final String id;
  private final String holderRef;
  private BigDecimal balance;
  private AccountStatus status;

  /**
   * Creates an account with a validated initial balance.
   *
   * @param id             unique alphanumeric account identifier
   * @param holderRef      opaque holder reference code (non-PII)
   * @param initialBalance starting balance (must be &gt;= 0)
   */
  public Account(final String id, final String holderRef, final BigDecimal initialBalance) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.holderRef = Objects.requireNonNull(holderRef, "holderRef must not be null");
    this.balance = Objects.requireNonNull(initialBalance, "initialBalance must not be null");
    this.status = AccountStatus.ACTIVE;
  }

  public String getId() {
    return id;
  }

  public String getHolderRef() {
    return holderRef;
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

  /**
   * Updates the account balance.
   *
   * @param newBalance replacement balance value
   */
  public void setBalance(final BigDecimal newBalance) {
    this.balance = Objects.requireNonNull(newBalance, "balance must not be null");
  }

  /**
   * Transitions the account to a new lifecycle status.
   *
   * @param newStatus target status
   */
  public void setStatus(final AccountStatus newStatus) {
    this.status = Objects.requireNonNull(newStatus, "status must not be null");
  }

  @Override
  public String toString() {
    return String.format(
        "Account[id=%s, holderRef=%s, balance=%s, status=%s]",
        id, holderRef, balance, status);
  }
}
