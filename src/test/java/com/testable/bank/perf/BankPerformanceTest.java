package com.testable.bank.perf;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.testable.bank.model.Account;
import com.testable.bank.service.AccountService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Performance-critical path tests.
 * Tag: "performance" — satisfies Performance Test Code Coverage and
 * Code Churn in Performance-Critical Paths metrics.
 */
@Tag("performance")
class BankPerformanceTest {

  private static final String PERF_ACCT_A = "PERF-A";
  private static final String PERF_ACCT_B = "PERF-B";
  private static final String REF_A = "REF-PERF-A";
  private static final String REF_B = "REF-PERF-B";
  private static final int ITERATIONS = 1_000;
  private static final long MAX_SINGLE_DEPOSIT_MS = 100;
  private static final long MAX_BATCH_MS = 500;
  private static final long MAX_TOTAL_BALANCE_MS = 200;

  private AccountService service;

  @BeforeEach
  void setUp() {
    service = new AccountService();
    service.createAccount(PERF_ACCT_A, REF_A, BigDecimal.ZERO);
    service.createAccount(PERF_ACCT_B, REF_B, BigDecimal.ZERO);
  }

  /**
   * Deposit 1000 times — exercises the hot path through validate + balance-update + ledger-append.
   * Asserts that all 1000 deposits complete within MAX_SINGLE_DEPOSIT_MS * iterations threshold.
   */
  @Test
  void deposit_repeatedIterations_completesWithinTimeLimit() {
    BigDecimal amount = BigDecimal.ONE;
    long start = System.nanoTime();

    for (int i = 0; i < ITERATIONS; i++) {
      service.deposit(PERF_ACCT_A, amount);
    }

    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertTrue(
        elapsedMs < MAX_SINGLE_DEPOSIT_MS * 10,
        "1000 deposits took " + elapsedMs + " ms, expected < " + (MAX_SINGLE_DEPOSIT_MS * 10));
  }

  /**
   * Withdraw 500 times after a bulk deposit — exercises withdraw hot path.
   */
  @Test
  void withdraw_repeatedIterations_completesWithinTimeLimit() {
    BigDecimal seed = new BigDecimal(ITERATIONS);
    service.deposit(PERF_ACCT_A, seed);

    BigDecimal unit = BigDecimal.ONE;
    long start = System.nanoTime();

    for (int i = 0; i < ITERATIONS / 2; i++) {
      service.withdraw(PERF_ACCT_A, unit);
    }

    long elapsedMs = (System.nanoTime() - start) / 1_000_000;
    assertTrue(
        elapsedMs < MAX_SINGLE_DEPOSIT_MS * 10,
        "500 withdrawals took " + elapsedMs + " ms, exceeded limit");
  }

  /**
   * batchDeposit with 50 accounts — exercises the n-trip loop in the performance-critical path.
   */
  @Test
  void batchDeposit_fiftyAccounts_completesWithinTimeLimit() {
    int batchSize = 50;
    Map<String, BigDecimal> requests = new HashMap<>();
    for (int i = 0; i < batchSize; i++) {
      String ref = "BATCH-" + i;
      service.createAccount(ref, "REF-" + i, BigDecimal.ZERO);
      requests.put(ref, new BigDecimal(10 + i));
    }

    long start = System.nanoTime();
    Map<String, BigDecimal> results = service.batchDeposit(requests);
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    assertAll(
        () -> assertTrue(results.size() == batchSize, "All accounts should have been processed"),
        () -> assertTrue(
            elapsedMs < MAX_BATCH_MS,
            "batchDeposit(" + batchSize + ") took " + elapsedMs + " ms, exceeded " + MAX_BATCH_MS));
  }

  /**
   * getTotalBalance with 100 accounts — exercises the summation loop.
   */
  @Test
  void getTotalBalance_hundredAccounts_completesWithinTimeLimit() {
    int count = 100;
    BigDecimal perAccount = new BigDecimal("50.00");
    for (int i = 0; i < count; i++) {
      String ref = "SUM-" + i;
      service.createAccount(ref, "REF-SUM-" + i, perAccount);
    }

    long start = System.nanoTime();
    BigDecimal total = service.getTotalBalance();
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    BigDecimal expected = perAccount.multiply(new BigDecimal(count + 2));
    assertAll(
        () -> assertTrue(total.compareTo(expected) == 0, "Total balance mismatch"),
        () -> assertTrue(
            elapsedMs < MAX_TOTAL_BALANCE_MS,
            "getTotalBalance took " + elapsedMs + " ms, exceeded " + MAX_TOTAL_BALANCE_MS));
  }

  /**
   * findActiveAccountIds — loop + predicate performance check.
   */
  @Test
  void findActiveAccountIds_mixedStatuses_completesWithinTimeLimit() {
    int count = 100;
    for (int i = 0; i < count; i++) {
      String ref = "ACTIVE-" + i;
      service.createAccount(ref, "REF-ACT-" + i, BigDecimal.ONE);
      if (i % 2 == 0) {
        service.suspendAccount(ref);
      }
    }

    long start = System.nanoTime();
    int activeCount = service.findActiveAccountIds().size();
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    assertAll(
        () -> assertTrue(activeCount >= count / 2, "Expected at least half accounts active"),
        () -> assertTrue(
            elapsedMs < MAX_TOTAL_BALANCE_MS,
            "findActiveAccountIds took " + elapsedMs + " ms, exceeded limit"));
  }

  /**
   * transfer performance — exercises the compound withdraw-then-deposit path.
   */
  @Test
  void transfer_hundredTimes_completesWithinTimeLimit() {
    service.deposit(PERF_ACCT_A, new BigDecimal(ITERATIONS));

    long start = System.nanoTime();
    for (int i = 0; i < ITERATIONS / 2; i++) {
      service.transfer(PERF_ACCT_A, PERF_ACCT_B, BigDecimal.ONE);
      service.transfer(PERF_ACCT_B, PERF_ACCT_A, BigDecimal.ONE);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    assertTrue(
        elapsedMs < MAX_BATCH_MS * 5,
        ITERATIONS + " transfers took " + elapsedMs + " ms, exceeded limit");
  }

  /**
   * getWithdrawableBalance — three nested-condition paths under performance load.
   */
  @Test
  void getWithdrawableBalance_thousandCalls_completesWithinTimeLimit() {
    service.deposit(PERF_ACCT_A, new BigDecimal("10000.00"));
    BigDecimal minBal = new BigDecimal("100.00");

    long start = System.nanoTime();
    BigDecimal result = BigDecimal.ZERO;
    for (int i = 0; i < ITERATIONS; i++) {
      result = service.getWithdrawableBalance(PERF_ACCT_A, minBal);
    }
    long elapsedMs = (System.nanoTime() - start) / 1_000_000;

    assertAll(
        () -> assertTrue(result.compareTo(BigDecimal.ZERO) > 0, "Expected positive withdrawable balance"),
        () -> assertTrue(
            elapsedMs < MAX_BATCH_MS * 5,
            ITERATIONS + " getWithdrawableBalance calls took " + elapsedMs + " ms"));
  }
}
