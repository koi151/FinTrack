package com.koi151.money.fintrack.common.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom Transactional annotation for FinTrack application.
 * <p>
 * This annotation serves as a replacement for the standard Spring {@link Transactional}.
 * It enforces strict consistency rules required for financial operations:
 * <ul>
 * <li><b>Rollback Strategy:</b> Automatically rolls back for {@link Exception} (checked and unchecked),
 * preventing partial commits when business exceptions occur.</li>
 * <li><b>Timeout Safety:</b> Enforces a default timeout to prevent long-running transactions
 * from locking database resources indefinitely.</li>
 * </ul>
 *
 * @author Koi151
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(rollbackFor = Exception.class, timeout = 60) // Default safety: 60 seconds
public @interface FinTransactional {

  /**
   * Alias for {@link Transactional#transactionManager}.
   */
  @AliasFor(annotation = Transactional.class)
  String value() default "";

  /**
   * Defines the transaction propagation type.
   * <p>
   * Default: {@link Propagation#REQUIRED} (Support current transaction, create new if none exists).
   */
  @AliasFor(annotation = Transactional.class)
  Propagation propagation() default Propagation.REQUIRED;

  /**
   * Defines the transaction isolation level.
   * <p>
   * Default: {@link Isolation#DEFAULT} (Uses the underlying database's default isolation, usually READ_COMMITTED).
   * For critical financial transfers, consider overriding this with {@link Isolation#SERIALIZABLE}.
   */
  @AliasFor(annotation = Transactional.class)
  Isolation isolation() default Isolation.DEFAULT;

  /**
   * Transaction timeout in seconds.
   * <p>
   * Default: <b>60 seconds</b>.
   * This prevents deadlocks or resource exhaustion from stalled queries.
   */
  @AliasFor(annotation = Transactional.class)
  int timeout() default 60;

  /**
   * A flag indicating that the transaction is read-only.
   * <p>
   * Default: {@code false}.
   * <p>
   * <b>Optimization Hint:</b> Set to {@code true} for fetch-only operations (e.g., getting transaction history).
   * This allows Spring and Hibernate to perform optimizations like skipping dirty checks
   * or routing to read-replicas.
   */
  @AliasFor(annotation = Transactional.class)
  boolean readOnly() default false;

  /**
   * Defines zero (0) or more exception classes that must <b>NOT</b> cause a rollback.
   * <p>
   * Useful for audit logging scenarios where the main operation fails, but the audit log entry
   * should still be persisted.
   */
  @AliasFor(annotation = Transactional.class)
  Class<? extends Throwable>[] noRollbackFor() default {};

}