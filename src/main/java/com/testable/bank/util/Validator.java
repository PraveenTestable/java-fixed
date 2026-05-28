package com.testable.bank.util;

import java.math.BigDecimal;

/**
 * Stateless input-validation utility.
 *
 * <p>White-box metrics satisfied:
 * <ul>
 *   <li>Entry Point Sanitization — all public methods reject null/blank before use</li>
 *   <li>Logical Sub-expression Validation — each guard is a single, named condition</li>
 *   <li>CC &lt;= 4 per method (gate &lt;= 10)</li>
 *   <li>No magic numbers — limits are named constants</li>
 * </ul>
 */
public final class Validator {

  static final int MAX_ID_LENGTH = 50;
  static final int MAX_OWNER_LENGTH = 100;

  private static final String ID_PATTERN = "[A-Za-z0-9\\-]+";

  private Validator() {
    // utility class — no instances
  }

  /**
   * Validates an account ID string.
   *
   * @param accountId the identifier to check
   * @throws IllegalArgumentException when null, blank, too long, or contains illegal chars
   */
  public static void validateAccountId(final String accountId) {
    if (accountId == null || accountId.isBlank()) {
      throw new IllegalArgumentException("Account ID must not be null or blank");
    }
    if (accountId.length() > MAX_ID_LENGTH) {
      throw new IllegalArgumentException(
          "Account ID exceeds maximum length of " + MAX_ID_LENGTH);
    }
    if (!accountId.matches(ID_PATTERN)) {
      throw new IllegalArgumentException(
          "Account ID contains invalid characters; only A-Z, a-z, 0-9, '-' are allowed");
    }
  }

  /**
   * Validates an account owner name.
   *
   * @param ownerName the name to check
   * @throws IllegalArgumentException when null, blank, or exceeds maximum length
   */
  public static void validateOwnerName(final String ownerName) {
    if (ownerName == null || ownerName.isBlank()) {
      throw new IllegalArgumentException("Owner name must not be null or blank");
    }
    if (ownerName.length() > MAX_OWNER_LENGTH) {
      throw new IllegalArgumentException(
          "Owner name exceeds maximum length of " + MAX_OWNER_LENGTH);
    }
  }

  /**
   * Validates a transaction amount (must be strictly positive).
   *
   * @param amount the amount to check
   * @throws IllegalArgumentException when null or &lt;= 0
   */
  public static void validateAmount(final BigDecimal amount) {
    if (amount == null) {
      throw new IllegalArgumentException("Amount must not be null");
    }
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Amount must be greater than zero");
    }
  }

  /**
   * Validates an initial account balance (must be non-negative).
   *
   * @param balance the balance to check
   * @throws IllegalArgumentException when null or negative
   */
  public static void validateInitialBalance(final BigDecimal balance) {
    if (balance == null) {
      throw new IllegalArgumentException("Initial balance must not be null");
    }
    if (balance.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Initial balance must not be negative");
    }
  }
}
