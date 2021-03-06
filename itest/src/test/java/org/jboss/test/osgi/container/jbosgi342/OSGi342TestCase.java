/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.test.osgi.container.jbosgi342;

import static org.junit.Assert.assertTrue;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * [JBOSGI-342] Bundle resolution depends on install order
 *
 * https://jira.jboss.org/jira/browse/JBOSGI-342
 * 
 * @author thomas.diesler@jboss.com
 * @since 11-Jun-2010
 */
public class OSGi342TestCase extends OSGiFrameworkTest
{
   @After
   public void tearDown() throws Exception
   {
      shutdownFramework();
      super.tearDown();
   }

   @Test
   public void testCompendiumFirst() throws Exception
   {
      Bundle cmpd = installBundle("bundles/org.osgi.compendium.jar");
      Bundle eventadmin = installBundle("bundles/org.apache.felix.eventadmin.jar");
      try
      {
         assertBundleState(Bundle.INSTALLED, cmpd.getState());
         assertBundleState(Bundle.INSTALLED, eventadmin.getState());

         PackageAdmin pa = getPackageAdmin();
         assertTrue("All bundles resolved", pa.resolveBundles(null));

         assertBundleState(Bundle.RESOLVED, eventadmin.getState());
         assertBundleState(Bundle.RESOLVED, cmpd.getState());
      }
      finally
      {
         eventadmin.uninstall();
         cmpd.uninstall();
      }
   }

   @Test
   public void testCompendiumLast() throws Exception
   {
      Bundle eventadmin = installBundle("bundles/org.apache.felix.eventadmin.jar");
      Bundle cmpd = installBundle("bundles/org.osgi.compendium.jar");
      try
      {
         assertBundleState(Bundle.INSTALLED, cmpd.getState());
         assertBundleState(Bundle.INSTALLED, eventadmin.getState());

         PackageAdmin pa = getPackageAdmin();
         assertTrue("All bundles resolved", pa.resolveBundles(null));

         assertBundleState(Bundle.RESOLVED, eventadmin.getState());
         assertBundleState(Bundle.RESOLVED, cmpd.getState());
      }
      finally
      {
         eventadmin.uninstall();
         cmpd.uninstall();
      }
   }
}
