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

import junit.framework.*;
import net.sf.retrotranslator.runtime.java.lang._UnsupportedOperationException;

public class _UnsupportedOperationExceptionTestCase extends TestCase {

  public void testUnsupportedOperationExceptionOneParam() throws Exception {
      UnsupportedOperationException exception =
          new UnsupportedOperationException(new ClassNotFoundException("123"));
      assertEquals("java.lang.ClassNotFoundException: 123", exception.getMessage());
      Throwable cause = exception.getCause();
      assertTrue(cause instanceof ClassNotFoundException);
      assertEquals("123", cause.getMessage());
      class Ex extends UnsupportedOperationException {
          public Ex(Throwable cause) {
              super(cause);
          }
      }
      Ex ex = new Ex(new UnsupportedOperationException());
      assertEquals("java.lang.UnsupportedOperationException", ex.getMessage());
      assertTrue(ex.getCause() instanceof UnsupportedOperationException);

      UnsupportedOperationException nullCausedEx = new UnsupportedOperationException((Throwable) null);
      assertNull(nullCausedEx.getMessage());
      assertNull(nullCausedEx.getCause());
      try {
          nullCausedEx.initCause(new Throwable());
          fail();
      } catch (IllegalStateException e) {
          //ok
      }
  }

  public void testUnsupportedOperationExceptionTwoParam() throws Exception {
      UnsupportedOperationException exception =
          new UnsupportedOperationException("abc", new ClassNotFoundException("123"));
      assertEquals("abc", exception.getMessage());
      Throwable cause = exception.getCause();
      assertTrue(cause instanceof ClassNotFoundException);
      assertEquals("123", cause.getMessage());
      class Ex extends UnsupportedOperationException {
          public Ex(String message, Throwable cause) {
              super(message, cause);
          }
      }
      Ex ex = new Ex("qwerty", new UnsupportedOperationException());
      assertEquals("qwerty", ex.getMessage());
      assertTrue(ex.getCause() instanceof UnsupportedOperationException);
  }

}