package com.testable.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.testable.bank.model.Account;
import com.testable.bank.model.Account.AccountStatus;
import com.testable.bank.model.TransactionRecord;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Tests for {@link AccountBatchService}. Loop path coverage: zero/one/n-trip. */
class AccountBatchServiceTest {

  private static final String REF_A = "BS-001";
  private static final String REF_B = "BS-002";

  private Map<String, Account> accounts;
  private List<TransactionRecord> ledger;
  private AccountBatchService bs;

  @BeforeEach
  void setUp() {
    accounts = new HashMap<>();
    ledger = new ArrayList<>();
    bs = new AccountBatchService(accounts, ledger);
  }

  private Account addAccount(final String ref, final BigDecimal bal) {
    Account a = new Account(ref, "HOLD-" + ref, bal);
    accounts.put(ref, a);
    return a;
  }

  @Test
  @Tag("loop")
  void batchDeposit_emptyMap_zeroTrip_returnsEmpty() {
    assertTrue(bs.batchDeposit(Collections.emptyMap()).isEmpty());
  }

  @Test
  @Tag("loop")
  void batchDeposit_singleEntry_oneTrip_updatesBalance() {
    addAccount(REF_A, BigDecimal.valueOf(100));
    Map<String, BigDecimal> result =
        bs.batchDeposit(Map.of(REF_A, BigDecimal.valueOf(50)));
    assertEquals(BigDecimal.valueOf(150), result.get(REF_A));
    assertEquals(1, ledger.size());
    assertEquals("BATCH_DEPOSIT", ledger.get(0).getType());
  }

  @Test
  @Tag("loop")
  void batchDeposit_twoEntries_nTrip_bothUpdated() {
    addAccount(REF_A, BigDecimal.valueOf(100));
    addAccount(REF_B, BigDecimal.valueOf(200));
    Map<String, BigDecimal> result =
        bs.batchDeposit(Map.of(REF_A, BigDecimal.valueOf(10), REF_B, BigDecimal.valueOf(20)));
    assertEquals(2, result.size());
    assertEquals(BigDecimal.valueOf(110), result.get(REF_A));
    assertEquals(BigDecimal.valueOf(220), result.get(REF_B));
  }

  @Test
  @Tag("loop")
  void batchDeposit_unknownRef_skipped() {
    assertTrue(bs.batchDeposit(Map.of("GHOST", BigDecimal.TEN)).isEmpty());
    assertTrue(ledger.isEmpty());
  }

  @Test
  @Tag("loop")
  void batchDeposit_suspendedAccount_skipped() {
    Account a = addAccount(REF_A, BigDecimal.valueOf(100));
    a.setStatus(AccountStatus.SUSPENDED);
    Map<String, BigDecimal> result =
        bs.batchDeposit(Map.of(REF_A, BigDecimal.TEN));
    assertTrue(result.isEmpty());
    assertEquals(BigDecimal.valueOf(100), a.getBalance());
  }

  @Test
  void batchDeposit_nullMap_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> bs.batchDeposit(null));
  }

  @Test
  @Tag("loop")
  void batchDeposit_nullAmount_skipped() {
    addAccount(REF_A, BigDecimal.valueOf(100));
    Map<String, BigDecimal> req = new HashMap<>();
    req.put(REF_A, null);
    assertTrue(bs.batchDeposit(req).isEmpty());
  }
}
