/*********************************************************************
 *                                                                   *
 * Copyright (c) 2002-2006 by Survey Software Services, Inc.         *
 * All rights reserved.                                              *
 *                                                                   *
 * This computer program is protected by copyright law and           *
 * international treaties. Unauthorized reproduction or distribution *
 * of this program, or any portion of it, may result in severe civil *
 * and criminal penalties, and will be prosecuted to the maximum     *
 * extent possible under the law.                                    *
 *                                                                   *
 *********************************************************************/

package net.sf.retrotranslator.runtime.java.lang;

/**
 *
 * @author: Taras Puchko (taras.puchko). 
 * @date: Jan 28, 2008
 */
public class _UnsupportedOperationException {

  public static class UnsupportedOperationExceptionBuilder {

      private final String message;
      private final Throwable cause;

      protected UnsupportedOperationExceptionBuilder(String message, Throwable cause) {
          this.message = message;
          this.cause = cause;
      }

      public String argument1() {
          return message;
      }

      public void initialize(UnsupportedOperationException e) {
          e.initCause(cause);
      }

  }

  public static UnsupportedOperationExceptionBuilder createInstanceBuilder(String message, Throwable cause) {
      return new UnsupportedOperationExceptionBuilder(message, cause);
  }

  public static UnsupportedOperationExceptionBuilder createInstanceBuilder(Throwable cause) {
      return new UnsupportedOperationExceptionBuilder(cause == null ? null : cause.toString(), cause);
  }

}
