package com.testable.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.testable.bank.model.Account;
import com.testable.bank.model.Account.AccountStatus;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Tests for {@link AccountQueryService}. */
class AccountQueryServiceTest {

  private static final String REF_A = "QS-001";
  private static final String REF_B = "QS-002";
  private static final String HOLDER_A = "REF-QS-A";
  private static final String HOLDER_B = "REF-QS-B";

  private Map<String, Account> accounts;
  private AccountQueryService qs;

  @BeforeEach
  void setUp() {
    accounts = new HashMap<>();
    qs = new AccountQueryService(accounts);
  }

  private Account addAccount(final String ref, final BigDecimal bal) {
    Account a = new Account(ref, "HOLD-" + ref, bal);
    accounts.put(ref, a);
    return a;
  }

  @Test
  void findAccount_existing_returnsPresent() {
    addAccount(REF_A, BigDecimal.valueOf(100));
    assertTrue(qs.findAccount(REF_A).isPresent());
  }

  @Test
  void findAccount_missing_returnsEmpty() {
    assertFalse(qs.findAccount("NONE").isPresent());
  }

  @Test
  void findAccount_nullRef_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> qs.findAccount(null));
  }

  @Test
  @Tag("loop")
  void getTotalBalance_empty_returnsZero() {
    assertEquals(BigDecimal.ZERO, qs.getTotalBalance());
  }

  @Test
  @Tag("loop")
  void getTotalBalance_twoAccounts_returnsSum() {
    addAccount(REF_A, BigDecimal.valueOf(300));
    addAccount(REF_B, BigDecimal.valueOf(150));
    assertEquals(BigDecimal.valueOf(450), qs.getTotalBalance());
  }

  @Test
  @Tag("loop")
  void findActiveAccountIds_allActive_returnsAll() {
    addAccount(REF_A, BigDecimal.ONE);
    addAccount(REF_B, BigDecimal.TEN);
    assertEquals(2, qs.findActiveAccountIds().size());
  }

  @Test
  @Tag("loop")
  void findActiveAccountIds_suspended_excluded() {
    Account a = addAccount(REF_A, BigDecimal.ONE);
    addAccount(REF_B, BigDecimal.TEN);
    a.setStatus(AccountStatus.SUSPENDED);
    assertEquals(1, qs.findActiveAccountIds().size());
    assertTrue(qs.findActiveAccountIds().contains(REF_B));
  }

  @Test
  @Tag("nested")
  void getWithdrawableBalance_surplus_returnsPositive() {
    addAccount(REF_A, BigDecimal.valueOf(200));
    assertEquals(BigDecimal.valueOf(150),
        qs.getWithdrawableBalance(REF_A, BigDecimal.valueOf(50)));
  }

  @Test
  @Tag("nested")
  void getWithdrawableBalance_noSurplus_returnsZero() {
    addAccount(REF_A, BigDecimal.valueOf(30));
    assertEquals(BigDecimal.ZERO,
        qs.getWithdrawableBalance(REF_A, BigDecimal.valueOf(50)));
  }

  @Test
  @Tag("nested")
  void getWithdrawableBalance_suspended_returnsZero() {
    Account a = addAccount(REF_A, BigDecimal.valueOf(500));
    a.setStatus(AccountStatus.SUSPENDED);
    assertEquals(BigDecimal.ZERO,
        qs.getWithdrawableBalance(REF_A, BigDecimal.ZERO));
  }

  @Test
  void getWithdrawableBalance_notFound_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class,
        () -> qs.getWithdrawableBalance("GHOST", BigDecimal.ZERO));
  }
}
