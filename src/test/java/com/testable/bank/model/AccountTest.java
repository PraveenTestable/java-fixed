package com.testable.bank.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Account}.
 *
 * <p>Coverage targets (WB metrics):
 * <ul>
 *   <li>Statement Coverage &gt;= 80% (gate)</li>
 *   <li>Branch Coverage &gt;= 70% (gate)</li>
 *   <li>Each method has at least one dedicated test (Test Case Granularity)</li>
 * </ul>
 */
class AccountTest {

  private Account newAccount() {
    return new Account("A-01", "Alice", BigDecimal.valueOf(200));
  }

  @Test
  void constructor_nullId_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new Account(null, "Alice", BigDecimal.TEN));
  }

  @Test
  void constructor_nullOwner_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new Account("A-01", null, BigDecimal.TEN));
  }

  @Test
  void constructor_nullBalance_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new Account("A-01", "Alice", null));
  }

  @Test
  void constructor_validArgs_setsActiveStatus() {
    Account account = newAccount();
    assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
    assertTrue(account.isActive());
  }

  @Test
  void getters_returnConstructorValues() {
    Account account = newAccount();
    assertEquals("A-01", account.getId());
    assertEquals("Alice", account.getOwner());
    assertEquals(BigDecimal.valueOf(200), account.getBalance());
  }

  @Test
  void isActive_afterSuspend_returnsFalse() {
    Account account = newAccount();
    account.setStatus(Account.AccountStatus.SUSPENDED);
    assertFalse(account.isActive());
  }

  @Test
  void setBalance_nullArg_throwsNpe() {
    assertThrows(NullPointerException.class, () -> newAccount().setBalance(null));
  }

  @Test
  void toString_containsId() {
    assertTrue(newAccount().toString().contains("A-01"));
  }
}
