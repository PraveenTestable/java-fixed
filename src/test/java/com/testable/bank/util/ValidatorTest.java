package com.testable.bank.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Validator}.
 *
 * <p>Every true/false branch of each guard is exercised to meet:
 * <ul>
 *   <li>Branch Coverage &gt;= 70% (Decision Outcome Verification gate)</li>
 *   <li>All-Defs Coverage &gt;= 75% (all input parameters reach a use point)</li>
 *   <li>All-Uses Coverage &gt;= 65% (every variable used in computation or predicate)</li>
 *   <li>Boundary Mutant Analysis — tests use exact threshold values</li>
 * </ul>
 */
class ValidatorTest {

  // ── validateAccountId ───────────────────────────────────────────────────

  @Test
  void validateAccountId_valid_noException() {
    assertDoesNotThrow(() -> Validator.validateAccountId("ACC-001"));
  }

  @Test
  void validateAccountId_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId(null));
  }

  @Test
  void validateAccountId_blank_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId("   "));
  }

  @Test
  void validateAccountId_atMaxLength_noException() {
    assertDoesNotThrow(() -> Validator.validateAccountId("A".repeat(Validator.MAX_ID_LENGTH)));
  }

  @Test
  void validateAccountId_exceedsMaxLength_throwsIllegalArgument() {
    String overLimit = "A".repeat(Validator.MAX_ID_LENGTH + 1);
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId(overLimit));
  }

  @Test
  void validateAccountId_invalidChars_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId("ACC@001!"));
  }

  // ── validateOwnerName ───────────────────────────────────────────────────

  @Test
  void validateOwnerName_valid_noException() {
    assertDoesNotThrow(() -> Validator.validateOwnerName("Alice Smith"));
  }

  @Test
  void validateOwnerName_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateOwnerName(null));
  }

  @Test
  void validateOwnerName_empty_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateOwnerName(""));
  }

  @Test
  void validateOwnerName_atMaxLength_noException() {
    assertDoesNotThrow(() -> Validator.validateOwnerName("A".repeat(Validator.MAX_OWNER_LENGTH)));
  }

  @Test
  void validateOwnerName_exceedsMaxLength_throwsIllegalArgument() {
    String overLimit = "A".repeat(Validator.MAX_OWNER_LENGTH + 1);
    assertThrows(IllegalArgumentException.class, () -> Validator.validateOwnerName(overLimit));
  }

  // ── validateAmount ──────────────────────────────────────────────────────

  @Test
  void validateAmount_positive_noException() {
    assertDoesNotThrow(() -> Validator.validateAmount(BigDecimal.valueOf(0.01)));
  }

  @Test
  void validateAmount_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAmount(null));
  }

  @Test
  void validateAmount_zero_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAmount(BigDecimal.ZERO));
  }

  @Test
  void validateAmount_negative_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> Validator.validateAmount(BigDecimal.valueOf(-5)));
  }

  // ── validateInitialBalance ──────────────────────────────────────────────

  @Test
  void validateInitialBalance_zero_noException() {
    assertDoesNotThrow(() -> Validator.validateInitialBalance(BigDecimal.ZERO));
  }

  @Test
  void validateInitialBalance_positive_noException() {
    assertDoesNotThrow(() -> Validator.validateInitialBalance(BigDecimal.valueOf(1000)));
  }

  @Test
  void validateInitialBalance_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateInitialBalance(null));
  }

  @Test
  void validateInitialBalance_negative_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> Validator.validateInitialBalance(BigDecimal.valueOf(-1)));
  }
}
