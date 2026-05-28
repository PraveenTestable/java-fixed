package com.testable.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.testable.bank.model.Account.AccountStatus;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AccountService}.
 *
 * <p>Coverage targets:
 * <ul>
 *   <li>Statement Coverage &gt;= 80% (Code Execution Verification)</li>
 *   <li>Branch Coverage &gt;= 70% (Decision Outcome Verification)</li>
 *   <li>Loop paths: zero-trip, one-trip, n-trip (Loop Condition Testing)</li>
 *   <li>Nested condition paths (Nested Condition Path Testing)</li>
 *   <li>Exception paths (Exception Path Handling)</li>
 *   <li>Mutation Kill Rate &gt;= 70% — assertions use exact expected values</li>
 *   <li>All-Uses Coverage — each variable used in both c-use and p-use</li>
 *   <li>Cross-Function Use — amount flows caller → validator → account balance</li>
 * </ul>
 */
class AccountServiceTest {

  // Non-PII identifiers used in tests (no real personal data)
  private static final String ID_ALPHA = "T-001";
  private static final String ID_BETA = "T-002";
  private static final String REF_ALPHA = "ACCT-REF-A";
  private static final String REF_BETA = "ACCT-REF-B";

  private AccountService service;

  @BeforeEach
  void setUp() {
    service = new AccountService();
  }

  // ── createAccount ────────────────────────────────────────────────────────

  @Test
  void createAccount_validArgs_returnsActiveAccount() {
    var account = service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(500));
    assertEquals(ID_ALPHA, account.getId());
    assertEquals(REF_ALPHA, account.getHolderRef());
    assertEquals(BigDecimal.valueOf(500), account.getBalance());
    assertEquals(AccountStatus.ACTIVE, account.getStatus());
  }

  @Test
  void createAccount_duplicateId_throwsIllegalState() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    assertThrows(IllegalStateException.class,
        () -> service.createAccount(ID_ALPHA, REF_BETA, BigDecimal.valueOf(50)));
  }

  @Test
  void createAccount_nullId_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createAccount(null, REF_ALPHA, BigDecimal.valueOf(100)));
  }

  @Test
  void createAccount_negativeBalance_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(-1)));
  }

  // ── deposit ──────────────────────────────────────────────────────────────

  @Test
  void deposit_validAmount_increasesBalance() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.deposit(ID_ALPHA, BigDecimal.valueOf(50));
    assertEquals(BigDecimal.valueOf(150), balance(ID_ALPHA));
  }

  @Test
  void deposit_loggedAfterDeposit_recordExists() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.deposit(ID_ALPHA, BigDecimal.valueOf(30));
    assertFalse(service.getLedger().isEmpty());
    assertEquals("DEPOSIT", service.getLedger().get(0).getType());
  }

  @Test
  void deposit_zeroAmount_throwsIllegalArgument() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    assertThrows(IllegalArgumentException.class,
        () -> service.deposit(ID_ALPHA, BigDecimal.ZERO));
  }

  @Test
  void deposit_nullAmount_throwsIllegalArgument() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    assertThrows(IllegalArgumentException.class,
        () -> service.deposit(ID_ALPHA, null));
  }

  @Test
  void deposit_suspendedAccount_throwsIllegalState() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.suspendAccount(ID_ALPHA);
    assertThrows(IllegalStateException.class,
        () -> service.deposit(ID_ALPHA, BigDecimal.TEN));
  }

  @Test
  void deposit_unknownAccount_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> service.deposit("NONE-999", BigDecimal.TEN));
  }

  // ── withdraw ─────────────────────────────────────────────────────────────

  @Test
  void withdraw_sufficientFunds_decreasesBalance() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.withdraw(ID_ALPHA, BigDecimal.valueOf(40));
    assertEquals(BigDecimal.valueOf(60), balance(ID_ALPHA));
  }

  @Test
  void withdraw_exactBalance_leavesZero() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(50));
    service.withdraw(ID_ALPHA, BigDecimal.valueOf(50));
    assertEquals(BigDecimal.ZERO, balance(ID_ALPHA));
  }

  @Test
  void withdraw_insufficientFunds_throwsIllegalState() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(20));
    assertThrows(IllegalStateException.class,
        () -> service.withdraw(ID_ALPHA, BigDecimal.valueOf(50)));
  }

  @Test
  void withdraw_inactiveAccount_throwsIllegalState() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.suspendAccount(ID_ALPHA);
    assertThrows(IllegalStateException.class,
        () -> service.withdraw(ID_ALPHA, BigDecimal.TEN));
  }

  // ── transfer ─────────────────────────────────────────────────────────────

  @Test
  void transfer_validAccounts_movesBalance() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(200));
    service.createAccount(ID_BETA, REF_BETA, BigDecimal.valueOf(50));
    service.transfer(ID_ALPHA, ID_BETA, BigDecimal.valueOf(80));
    assertEquals(BigDecimal.valueOf(120), balance(ID_ALPHA));
    assertEquals(BigDecimal.valueOf(130), balance(ID_BETA));
  }

  @Test
  void transfer_sameAccount_throwsIllegalArgument() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    assertThrows(IllegalArgumentException.class,
        () -> service.transfer(ID_ALPHA, ID_ALPHA, BigDecimal.TEN));
  }

  @Test
  void transfer_insufficientSource_throwsIllegalState() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(10));
    service.createAccount(ID_BETA, REF_BETA, BigDecimal.valueOf(50));
    assertThrows(IllegalStateException.class,
        () -> service.transfer(ID_ALPHA, ID_BETA, BigDecimal.valueOf(100)));
  }

  // ── batchDeposit — loop paths ─────────────────────────────────────────────

  @Test
  @Tag("loop")
  void batchDeposit_emptyMap_zeroTrip_returnsEmptyResult() {
    // zero-trip loop: body executes zero times
    Map<String, BigDecimal> result = service.batchDeposit(Collections.emptyMap());
    assertTrue(result.isEmpty());
  }

  @Test
  @Tag("loop")
  void batchDeposit_singleEntry_oneTrip_depositsAmount() {
    // one-trip loop: body executes exactly once
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    Map<String, BigDecimal> result = service.batchDeposit(Map.of(ID_ALPHA, BigDecimal.valueOf(25)));
    assertEquals(1, result.size());
    assertEquals(BigDecimal.valueOf(125), result.get(ID_ALPHA));
    assertEquals(BigDecimal.valueOf(125), balance(ID_ALPHA));
  }

  @Test
  @Tag("loop")
  void batchDeposit_multipleEntries_nTrip_depositsAll() {
    // n-trip loop: body executes multiple times
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.createAccount(ID_BETA, REF_BETA, BigDecimal.valueOf(200));
    Map<String, BigDecimal> requests = Map.of(
        ID_ALPHA, BigDecimal.valueOf(10),
        ID_BETA, BigDecimal.valueOf(20)
    );
    Map<String, BigDecimal> result = service.batchDeposit(requests);
    assertEquals(2, result.size());
    assertEquals(BigDecimal.valueOf(110), balance(ID_ALPHA));
    assertEquals(BigDecimal.valueOf(220), balance(ID_BETA));
  }

  @Test
  @Tag("loop")
  void batchDeposit_unknownAccount_skipsEntry() {
    // loop body: containsKey branch = false path
    Map<String, BigDecimal> result = service.batchDeposit(Map.of("GHOST-X", BigDecimal.TEN));
    assertTrue(result.isEmpty());
  }

  @Test
  @Tag("loop")
  void batchDeposit_suspendedAccount_skipsEntry() {
    // loop body: nested condition — account exists but not active
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.suspendAccount(ID_ALPHA);
    Map<String, BigDecimal> result = service.batchDeposit(Map.of(ID_ALPHA, BigDecimal.TEN));
    assertTrue(result.isEmpty());
    assertEquals(BigDecimal.valueOf(100), balance(ID_ALPHA));
  }

  @Test
  void batchDeposit_nullMap_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.batchDeposit(null));
  }

  // ── getTotalBalance — loop ────────────────────────────────────────────────

  @Test
  @Tag("loop")
  void getTotalBalance_emptyRegistry_returnsZero() {
    assertEquals(BigDecimal.ZERO, service.getTotalBalance());
  }

  @Test
  @Tag("loop")
  void getTotalBalance_multipleAccounts_returnsSum() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(300));
    service.createAccount(ID_BETA, REF_BETA, BigDecimal.valueOf(150));
    assertEquals(BigDecimal.valueOf(450), service.getTotalBalance());
  }

  // ── findActiveAccountIds — loop with filter ───────────────────────────────

  @Test
  @Tag("loop")
  void findActiveAccountIds_noAccounts_returnsEmpty() {
    assertTrue(service.findActiveAccountIds().isEmpty());
  }

  @Test
  @Tag("loop")
  void findActiveAccountIds_mixedStatuses_returnsOnlyActive() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.createAccount(ID_BETA, REF_BETA, BigDecimal.valueOf(50));
    service.suspendAccount(ID_BETA);
    List<String> active = service.findActiveAccountIds();
    assertEquals(1, active.size());
    assertTrue(active.contains(ID_ALPHA));
  }

  // ── getWithdrawableBalance — nested conditions ────────────────────────────

  @Test
  @Tag("nested")
  void getWithdrawableBalance_activeSufficientBalance_returnsSurplus() {
    // path: active=true, surplus>0
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(200));
    BigDecimal withdrawable = service.getWithdrawableBalance(ID_ALPHA, BigDecimal.valueOf(50));
    assertEquals(BigDecimal.valueOf(150), withdrawable);
  }

  @Test
  @Tag("nested")
  void getWithdrawableBalance_activeInsufficientBalance_returnsZero() {
    // path: active=true, surplus<=0
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(30));
    BigDecimal withdrawable = service.getWithdrawableBalance(ID_ALPHA, BigDecimal.valueOf(50));
    assertEquals(BigDecimal.ZERO, withdrawable);
  }

  @Test
  @Tag("nested")
  void getWithdrawableBalance_suspendedAccount_returnsZero() {
    // path: active=false (outer if not taken)
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(500));
    service.suspendAccount(ID_ALPHA);
    BigDecimal withdrawable = service.getWithdrawableBalance(ID_ALPHA, BigDecimal.ZERO);
    assertEquals(BigDecimal.ZERO, withdrawable);
  }

  @Test
  @Tag("nested")
  void getWithdrawableBalance_exactMinimum_returnsZero() {
    // boundary: balance == minimum → surplus == 0 → inner if not taken
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    BigDecimal withdrawable = service.getWithdrawableBalance(ID_ALPHA, BigDecimal.valueOf(100));
    assertEquals(BigDecimal.ZERO, withdrawable);
  }

  // ── findAccount ───────────────────────────────────────────────────────────

  @Test
  void findAccount_existing_returnsAccount() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    Optional<com.testable.bank.model.Account> result = service.findAccount(ID_ALPHA);
    assertTrue(result.isPresent());
    assertEquals(REF_ALPHA, result.get().getHolderRef());
  }

  @Test
  void findAccount_nonExisting_returnsEmpty() {
    assertFalse(service.findAccount("GHOST-999").isPresent());
  }

  // ── suspendAccount / closeAccount ─────────────────────────────────────────

  @Test
  void suspendAccount_active_changeStatus() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.suspendAccount(ID_ALPHA);
    assertEquals(AccountStatus.SUSPENDED, status(ID_ALPHA));
  }

  @Test
  void suspendAccount_unknown_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.suspendAccount("NONE-X"));
  }

  @Test
  void closeAccount_zeroBalance_changesStatusToClosed() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.ZERO);
    service.closeAccount(ID_ALPHA);
    assertEquals(AccountStatus.CLOSED, status(ID_ALPHA));
  }

  @Test
  void closeAccount_nonZeroBalance_throwsIllegalState() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    assertThrows(IllegalStateException.class, () -> service.closeAccount(ID_ALPHA));
  }

  @Test
  void closeAccount_unknown_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.closeAccount("NONE-X"));
  }

  // ── transaction log — cross-function data flow ────────────────────────────

  @Test
  void ledger_afterDepositAndWithdraw_hasCorrectEntries() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(200));
    service.deposit(ID_ALPHA, BigDecimal.valueOf(50));
    service.withdraw(ID_ALPHA, BigDecimal.valueOf(30));
    List<com.testable.bank.model.TransactionRecord> log = service.getLedger();
    assertEquals(2, log.size());
    assertEquals("DEPOSIT", log.get(0).getType());
    assertEquals("WITHDRAWAL", log.get(1).getType());
    assertEquals(BigDecimal.valueOf(250), log.get(0).getBalanceAfter());
    assertEquals(BigDecimal.valueOf(220), log.get(1).getBalanceAfter());
  }

  @Test
  void ledger_isCredit_trueForDeposit() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.deposit(ID_ALPHA, BigDecimal.valueOf(10));
    assertTrue(service.getLedger().get(0).isCredit());
  }

  @Test
  void ledger_isCredit_falseForWithdrawal() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.withdraw(ID_ALPHA, BigDecimal.valueOf(10));
    assertFalse(service.getLedger().get(0).isCredit());
  }

  @Test
  void ledger_toStringContainsAcctRef() {
    service.createAccount(ID_ALPHA, REF_ALPHA, BigDecimal.valueOf(100));
    service.deposit(ID_ALPHA, BigDecimal.valueOf(5));
    String repr = service.getLedger().get(0).toString();
    assertTrue(repr.contains(ID_ALPHA));
    assertNotNull(service.getLedger().get(0).getTimestamp());
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private BigDecimal balance(final String id) {
    return service.findAccount(id).orElseThrow().getBalance();
  }

  private AccountStatus status(final String id) {
    return service.findAccount(id).orElseThrow().getStatus();
  }
}
