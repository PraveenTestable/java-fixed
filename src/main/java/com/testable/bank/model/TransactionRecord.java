/*
 * Copyright 2026 Testable.cloud
 * Licensed under the Apache License, Version 2.0 — see LICENSE.
 */
package com.testable.bank.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record of a single account transaction.
 *
 * <p>Whitebox metrics addressed:
 * <ul>
 *   <li>Multiple Definitions Handling — {@code balanceAfter} is defined once per
 *       transaction and then used in computations and predicates across service calls</li>
 *   <li>Cross-Function Use Detection — fields flow from AccountService into tests</li>
 *   <li>All-Defs Coverage — every field assigned in constructor is read in at least one test</li>
 *   <li>Variable Use Detection — {@code type}, {@code amount}, {@code balanceAfter}
 *       used in both c-use (computations) and p-use (assertions)</li>
 * </ul>
 */
public final class TransactionRecord {

  private final String accountId;
  private final String type;
  private final BigDecimal amount;
  private final BigDecimal balanceAfter;
  private final Instant timestamp;

  /**
   * Creates a new immutable transaction record.
   *
   * @param accountId    account this transaction belongs to
   * @param type         transaction type (DEPOSIT, WITHDRAWAL, BATCH_DEPOSIT)
   * @param amount       amount transacted
   * @param balanceAfter account balance immediately after this transaction
   */
  public TransactionRecord(
      final String accountId,
      final String type,
      final BigDecimal amount,
      final BigDecimal balanceAfter) {
    this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
    this.type = Objects.requireNonNull(type, "type must not be null");
    this.amount = Objects.requireNonNull(amount, "amount must not be null");
    this.balanceAfter = Objects.requireNonNull(balanceAfter, "balanceAfter must not be null");
    this.timestamp = Instant.now();
  }

  public String getAccountId() {
    return accountId;
  }

  public String getType() {
    return type;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getBalanceAfter() {
    return balanceAfter;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  /** Returns {@code true} when this record represents a credit (DEPOSIT or BATCH_DEPOSIT). */
  public boolean isCredit() {
    return type.startsWith("DEPOSIT") || "BATCH_DEPOSIT".equals(type);
  }

  @Override
  public String toString() {
    return String.format(
        "TransactionRecord[account=%s, type=%s, amount=%s, balanceAfter=%s, at=%s]",
        accountId, type, amount, balanceAfter, timestamp);
  }
}
