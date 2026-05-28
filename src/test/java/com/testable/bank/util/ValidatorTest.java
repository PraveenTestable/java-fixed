package com.testable.bank.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Validator}.
 *
 * <p>Every true/false branch of each guard is exercised to meet:
 * <ul>
 *   <li>Branch Coverage &gt;= 70% (Decision Outcome Verification gate)</li>
 *   <li>All-Defs Coverage &gt;= 75% — all parameters reach a use point</li>
 *   <li>All-Uses Coverage &gt;= 65% — every variable used in computation or predicate</li>
 *   <li>Boundary Mutant Analysis &gt;= 80% — tests use exact threshold values</li>
 *   <li>Edge Case Detection — tests at exactly MAX_ID_LENGTH and MAX_HOLDER_LENGTH</li>
 * </ul>
 */
class ValidatorTest {

  // ── validateAccountId ─────────────────────────────────────────────────────

  @Test
  void validateAccountId_valid_noException() {
    assertDoesNotThrow(() -> Validator.validateAccountId("T-001"));
  }

  @Test
  void validateAccountId_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId(null));
  }

  @Test
  void validateAccountId_blankSpaces_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId("   "));
  }

  @Test
  void validateAccountId_emptyString_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId(""));
  }

  @Test
  void validateAccountId_atMaxLength_noException() {
    // boundary: exactly at limit — should NOT throw
    String atLimit = "A".repeat(Validator.MAX_ID_LENGTH);
    assertDoesNotThrow(() -> Validator.validateAccountId(atLimit));
  }

  @Test
  void validateAccountId_exceedsMaxLength_throwsIllegalArgument() {
    // boundary: one over limit — must throw
    String overLimit = "A".repeat(Validator.MAX_ID_LENGTH + 1);
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId(overLimit));
  }

  @Test
  void validateAccountId_invalidSpecialChars_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId("ID@001!"));
  }

  @Test
  void validateAccountId_containsSpace_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAccountId("ID 001"));
  }

  // ── validateHolderRef ─────────────────────────────────────────────────────

  @Test
  void validateHolderRef_valid_noException() {
    assertDoesNotThrow(() -> Validator.validateHolderRef("REF-ALPHA"));
  }

  @Test
  void validateHolderRef_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateHolderRef(null));
  }

  @Test
  void validateHolderRef_blank_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateHolderRef(""));
  }

  @Test
  void validateHolderRef_atMaxLength_noException() {
    String atLimit = "R".repeat(Validator.MAX_HOLDER_LENGTH);
    assertDoesNotThrow(() -> Validator.validateHolderRef(atLimit));
  }

  @Test
  void validateHolderRef_exceedsMaxLength_throwsIllegalArgument() {
    String overLimit = "R".repeat(Validator.MAX_HOLDER_LENGTH + 1);
    assertThrows(IllegalArgumentException.class, () -> Validator.validateHolderRef(overLimit));
  }

  // ── validateAmount ────────────────────────────────────────────────────────

  @Test
  void validateAmount_smallPositive_noException() {
    // boundary: smallest meaningful positive
    assertDoesNotThrow(() -> Validator.validateAmount(BigDecimal.valueOf(0.01)));
  }

  @Test
  void validateAmount_largePositive_noException() {
    assertDoesNotThrow(() -> Validator.validateAmount(BigDecimal.valueOf(1_000_000)));
  }

  @Test
  void validateAmount_null_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAmount(null));
  }

  @Test
  void validateAmount_zero_throwsIllegalArgument() {
    // boundary: exactly zero — must throw (not > 0)
    assertThrows(IllegalArgumentException.class, () -> Validator.validateAmount(BigDecimal.ZERO));
  }

  @Test
  void validateAmount_negativeOne_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> Validator.validateAmount(BigDecimal.valueOf(-1)));
  }

  @Test
  void validateAmount_negativeSmall_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> Validator.validateAmount(BigDecimal.valueOf(-0.01)));
  }

  // ── validateInitialBalance ────────────────────────────────────────────────

  @Test
  void validateInitialBalance_zero_noException() {
    // boundary: zero is a valid initial balance
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
  void validateInitialBalance_negativeOne_throwsIllegalArgument() {
    // boundary: exactly -1 — must throw
    assertThrows(IllegalArgumentException.class,
        () -> Validator.validateInitialBalance(BigDecimal.valueOf(-1)));
  }

  @Test
  void validateInitialBalance_negativeSmall_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> Validator.validateInitialBalance(BigDecimal.valueOf(-0.01)));
  }

  // ── sanitise ──────────────────────────────────────────────────────────────

  @Test
  void sanitise_null_returnsEmpty() {
    assertEquals("", Validator.sanitise(null));
  }

  @Test
  void sanitise_cleanInput_unchanged() {
    assertEquals("hello world", Validator.sanitise("hello world"));
  }

  @Test
  void sanitise_specialChars_stripped() {
    // Injection prevention: < > ; are stripped
    assertEquals("hello", Validator.sanitise("hello<script>"));
  }

  @Test
  void sanitise_hyphenPreserved() {
    assertEquals("T-001", Validator.sanitise("T-001"));
  }
}
