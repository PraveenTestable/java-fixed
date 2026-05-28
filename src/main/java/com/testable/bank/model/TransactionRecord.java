package com.testable.bank.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record of a single account transaction.
 *
 * Whitebox metrics: Multiple Definitions Handling, Cross-Function Use Detection,
 * All-Defs Coverage, Variable Use Detection (c-use and p-use both exercised).
 */
public final class TransactionRecord {

  private final String acctRef;
  private final String txType;
  private final BigDecimal amount;
  private final BigDecimal balanceAfter;
  private final Instant timestamp;

  /**
   * Creates a new immutable transaction record.
   *
   * @param acctRef      opaque account reference (non-PII code)
   * @param txType       transaction type (DEPOSIT, WITHDRAWAL, BATCH_DEPOSIT)
   * @param amount       amount transacted
   * @param balanceAfter account balance immediately after this transaction
   */
  public TransactionRecord(
      final String acctRef,
      final String txType,
      final BigDecimal amount,
      final BigDecimal balanceAfter) {
    this.acctRef = Objects.requireNonNull(acctRef, "acctRef must not be null");
    this.txType = Objects.requireNonNull(txType, "txType must not be null");
    this.amount = Objects.requireNonNull(amount, "amount must not be null");
    this.balanceAfter = Objects.requireNonNull(balanceAfter, "balanceAfter must not be null");
    this.timestamp = Instant.now();
  }

  public String getAcctRef() {
    return acctRef;
  }

  public String getType() {
    return txType;
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

  /** Returns true when this record represents a credit (DEPOSIT or BATCH_DEPOSIT). */
  public boolean isCredit() {
    return txType.startsWith("DEPOSIT") || "BATCH_DEPOSIT".equals(txType);
  }

  @Override
  public String toString() {
    return String.format(
        "TransactionRecord[acctRef=%s, type=%s, amount=%s, balanceAfter=%s, at=%s]",
        acctRef, txType, amount, balanceAfter, timestamp);
  }
}
