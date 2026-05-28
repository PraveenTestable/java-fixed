package com.testable.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.testable.bank.model.Account;
import com.testable.bank.model.Account.AccountStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AccountService} targeting all WB metric thresholds.
 *
 * <p>Each test exercises a distinct branch so that:
 * <ul>
 *   <li>Statement Coverage &gt;= 80%</li>
 *   <li>Branch Coverage &gt;= 70%</li>
 *   <li>Mutation Kill Rate &gt;= 70% (assertions check exact values)</li>
 *   <li>Loop/boundary paths covered (deposit edge = zero-amount guard)</li>
 *   <li>Exception paths covered for Error Flow Verification</li>
 * </ul>
 */
class AccountServiceTest {

  private AccountService service;

  @BeforeEach
  void setUp() {
    service = new AccountService();
  }

  // ── createAccount ──────────────────────────────────────────────────────

  @Test
  void createAccount_validArgs_returnsActiveAccount() {
    Account account = service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(500));
    assertEquals("ACC-1", account.getId());
    assertEquals("Alice", account.getOwner());
    assertEquals(BigDecimal.valueOf(500), account.getBalance());
    assertEquals(AccountStatus.ACTIVE, account.getStatus());
  }

  @Test
  void createAccount_duplicateId_throwsIllegalState() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    assertThrows(IllegalStateException.class,
        () -> service.createAccount("ACC-1", "Bob", BigDecimal.valueOf(50)));
  }

  @Test
  void createAccount_nullId_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createAccount(null, "Alice", BigDecimal.valueOf(100)));
  }

  @Test
  void createAccount_negativeBalance_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(-1)));
  }

  // ── deposit ─────────────────────────────────────────────────────────────

  @Test
  void deposit_validAmount_increasesBalance() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    service.deposit("ACC-1", BigDecimal.valueOf(50));
    assertEquals(BigDecimal.valueOf(150), balance("ACC-1"));
  }

  @Test
  void deposit_zeroAmount_throwsIllegalArgument() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    assertThrows(IllegalArgumentException.class,
        () -> service.deposit("ACC-1", BigDecimal.ZERO));
  }

  @Test
  void deposit_nullAmount_throwsIllegalArgument() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    assertThrows(IllegalArgumentException.class,
        () -> service.deposit("ACC-1", null));
  }

  @Test
  void deposit_suspendedAccount_throwsIllegalState() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    service.suspendAccount("ACC-1");
    assertThrows(IllegalStateException.class,
        () -> service.deposit("ACC-1", BigDecimal.TEN));
  }

  @Test
  void deposit_unknownAccount_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> service.deposit("NONE", BigDecimal.TEN));
  }

  // ── withdraw ────────────────────────────────────────────────────────────

  @Test
  void withdraw_sufficientFunds_decreasesBalance() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    service.withdraw("ACC-1", BigDecimal.valueOf(40));
    assertEquals(BigDecimal.valueOf(60), balance("ACC-1"));
  }

  @Test
  void withdraw_insufficientFunds_throwsIllegalState() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(20));
    assertThrows(IllegalStateException.class,
        () -> service.withdraw("ACC-1", BigDecimal.valueOf(50)));
  }

  @Test
  void withdraw_inactiveAccount_throwsIllegalState() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    service.suspendAccount("ACC-1");
    assertThrows(IllegalStateException.class,
        () -> service.withdraw("ACC-1", BigDecimal.TEN));
  }

  // ── transfer ────────────────────────────────────────────────────────────

  @Test
  void transfer_validAccounts_movesBalance() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(200));
    service.createAccount("ACC-2", "Bob", BigDecimal.valueOf(50));
    service.transfer("ACC-1", "ACC-2", BigDecimal.valueOf(80));
    assertEquals(BigDecimal.valueOf(120), balance("ACC-1"));
    assertEquals(BigDecimal.valueOf(130), balance("ACC-2"));
  }

  @Test
  void transfer_sameAccount_throwsIllegalArgument() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    assertThrows(IllegalArgumentException.class,
        () -> service.transfer("ACC-1", "ACC-1", BigDecimal.TEN));
  }

  @Test
  void transfer_insufficientSource_throwsIllegalState() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(10));
    service.createAccount("ACC-2", "Bob", BigDecimal.valueOf(50));
    assertThrows(IllegalStateException.class,
        () -> service.transfer("ACC-1", "ACC-2", BigDecimal.valueOf(100)));
  }

  // ── findAccount ─────────────────────────────────────────────────────────

  @Test
  void findAccount_existing_returnsAccount() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    Optional<Account> result = service.findAccount("ACC-1");
    assertTrue(result.isPresent());
    assertEquals("Alice", result.get().getOwner());
  }

  @Test
  void findAccount_nonExisting_returnsEmpty() {
    assertFalse(service.findAccount("GHOST").isPresent());
  }

  @Test
  void findAccount_nullId_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.findAccount(null));
  }

  // ── suspendAccount ──────────────────────────────────────────────────────

  @Test
  void suspendAccount_activeAccount_changeStatus() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    service.suspendAccount("ACC-1");
    assertEquals(AccountStatus.SUSPENDED, status("ACC-1"));
  }

  @Test
  void suspendAccount_unknownAccount_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.suspendAccount("NONE"));
  }

  // ── closeAccount ────────────────────────────────────────────────────────

  @Test
  void closeAccount_zeroBalance_changesStatusToClosed() {
    service.createAccount("ACC-1", "Alice", BigDecimal.ZERO);
    service.closeAccount("ACC-1");
    assertEquals(AccountStatus.CLOSED, status("ACC-1"));
  }

  @Test
  void closeAccount_nonZeroBalance_throwsIllegalState() {
    service.createAccount("ACC-1", "Alice", BigDecimal.valueOf(100));
    assertThrows(IllegalStateException.class, () -> service.closeAccount("ACC-1"));
  }

  @Test
  void closeAccount_unknownAccount_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.closeAccount("NONE"));
  }

  // ── helpers ─────────────────────────────────────────────────────────────

  private BigDecimal balance(final String id) {
    return service.findAccount(id).orElseThrow().getBalance();
  }

  private AccountStatus status(final String id) {
    return service.findAccount(id).orElseThrow().getStatus();
  }
}
