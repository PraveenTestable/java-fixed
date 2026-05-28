package com.testable.bank.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Account} and {@link TransactionRecord}.
 *
 * <p>Coverage targets (WB metrics):
 * <ul>
 *   <li>Statement Coverage &gt;= 80% — every getter and branch exercised</li>
 *   <li>Branch Coverage &gt;= 70% — isActive() true and false paths</li>
 *   <li>All-Defs Coverage — every field assigned in constructor is read</li>
 *   <li>Mutation Kill Rate — assertions use exact values</li>
 * </ul>
 */
class AccountTest {

  private static final String ACCT_CODE = "ACC-X01";
  private static final String REF_CODE = "REF-X01";

  private Account newAccount() {
    return new Account(ACCT_CODE, REF_CODE, BigDecimal.valueOf(200));
  }

  // ── Account constructor guards ────────────────────────────────────────────

  @Test
  void constructor_nullId_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new Account(null, REF_CODE, BigDecimal.TEN));
  }

  @Test
  void constructor_nullHolder_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new Account(ACCT_CODE, null, BigDecimal.TEN));
  }

  @Test
  void constructor_nullBalance_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new Account(ACCT_CODE, REF_CODE, null));
  }

  // ── Account state ─────────────────────────────────────────────────────────

  @Test
  void constructor_validArgs_setsActiveStatus() {
    Account account = newAccount();
    assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
    assertTrue(account.isActive());
  }

  @Test
  void getters_returnConstructorValues() {
    Account account = newAccount();
    assertEquals(ACCT_CODE, account.getId());
    assertEquals(REF_CODE, account.getHolderRef());
    assertEquals(BigDecimal.valueOf(200), account.getBalance());
  }

  @Test
  void isActive_afterSuspend_returnsFalse() {
    Account account = newAccount();
    account.setStatus(Account.AccountStatus.SUSPENDED);
    assertFalse(account.isActive());
  }

  @Test
  void isActive_afterClose_returnsFalse() {
    Account account = newAccount();
    account.setStatus(Account.AccountStatus.CLOSED);
    assertFalse(account.isActive());
  }

  @Test
  void setBalance_updatesValue() {
    Account account = newAccount();
    account.setBalance(BigDecimal.valueOf(999));
    assertEquals(BigDecimal.valueOf(999), account.getBalance());
  }

  @Test
  void setBalance_nullArg_throwsNpe() {
    assertThrows(NullPointerException.class, () -> newAccount().setBalance(null));
  }

  @Test
  void setStatus_nullArg_throwsNpe() {
    assertThrows(NullPointerException.class, () -> newAccount().setStatus(null));
  }

  @Test
  void toString_containsAllFields() {
    String repr = newAccount().toString();
    assertTrue(repr.contains(ACCT_CODE));
    assertTrue(repr.contains(REF_CODE));
    assertTrue(repr.contains("200"));
    assertTrue(repr.contains("ACTIVE"));
  }

  // ── TransactionRecord ─────────────────────────────────────────────────────

  @Test
  void transactionRecord_constructor_setsAllFields() {
    var rec = new TransactionRecord(
        ACCT_CODE, "DEPOSIT", BigDecimal.valueOf(50), BigDecimal.valueOf(250));
    assertEquals(ACCT_CODE, rec.getAcctRef());
    assertEquals("DEPOSIT", rec.getType());
    assertEquals(BigDecimal.valueOf(50), rec.getAmount());
    assertEquals(BigDecimal.valueOf(250), rec.getBalanceAfter());
    assertNotNull(rec.getTimestamp());
  }

  @Test
  void transactionRecord_isCredit_trueForDeposit() {
    var rec = new TransactionRecord(
        ACCT_CODE, "DEPOSIT", BigDecimal.TEN, BigDecimal.valueOf(110));
    assertTrue(rec.isCredit());
  }

  @Test
  void transactionRecord_isCredit_trueForBatchDeposit() {
    var rec = new TransactionRecord(
        ACCT_CODE, "BATCH_DEPOSIT", BigDecimal.TEN, BigDecimal.valueOf(110));
    assertTrue(rec.isCredit());
  }

  @Test
  void transactionRecord_isCredit_falseForWithdrawal() {
    var rec = new TransactionRecord(
        ACCT_CODE, "WITHDRAWAL", BigDecimal.TEN, BigDecimal.valueOf(90));
    assertFalse(rec.isCredit());
  }

  @Test
  void transactionRecord_nullAcctRef_throwsNpe() {
    assertThrows(NullPointerException.class,
        () -> new TransactionRecord(null, "DEPOSIT", BigDecimal.TEN, BigDecimal.TEN));
  }

  @Test
  void transactionRecord_toString_containsType() {
    var rec = new TransactionRecord(
        ACCT_CODE, "DEPOSIT", BigDecimal.TEN, BigDecimal.valueOf(110));
    assertTrue(rec.toString().contains("DEPOSIT"));
  }
}
