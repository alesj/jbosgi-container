/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.test.osgi.framework.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.osgi.framework.classloader.support.a.A;
import org.junit.After;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Test the default resolver integration.
 * 
 * @author thomas.diesler@jboss.com
 * @since 31-May-2010
 */
public class DefaultResolverTestCase extends OSGiFrameworkTest
{
   @After
   public void tearDown() throws Exception
   {
      // Make sure we have a new framework for every test
      shutdownFramework();
      super.tearDown();
   }

   @Test
   public void testSimpleImport() throws Exception
   {
      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testSimpleImportPackageFails() throws Exception
   {
      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Verify that the class load
         assertLoadClassFail(bundleA, A.class.getName());

         // Verify bundle states
         assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testExplicitBundleResolve() throws Exception
   {
      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Only resolve BundleB
            PackageAdmin packageAdmin = getPackageAdmin();
            boolean allResolved = packageAdmin.resolveBundles(new Bundle[] { bundleB });
            assertTrue("All resolved", allResolved);

            // Verify bundle states
            assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());

            // Verify that the class can be loaded
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testSelfImportPackage() throws Exception
   {
      // Bundle-SymbolicName: selfimport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/selfimport", A.class);
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Verify that the class load
         assertLoadClass(bundleA, A.class.getName(), bundleA);

         // Verify bundle states
         assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testVersionImportPackage() throws Exception
   {
      //Bundle-SymbolicName: packageimportversion
      //Import-Package: org.jboss.test.osgi.classloader.support.a;version="[0.0.0,1.0.0]"
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageimportversion");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: packageexportversion100
         //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageexportversion100", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testVersionImportPackageFails() throws Exception
   {
      //Bundle-SymbolicName: packageimportversionfails
      //Import-Package: org.jboss.test.osgi.classloader.support.a;version="[3.0,4.0)"
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageimportversionfails");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: packageexportversion100
         //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageexportversion100", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClassFail(bundleA, A.class.getName());
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testOptionalImportPackage() throws Exception
   {
      //Bundle-SymbolicName: packageimportoptional
      //Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageimportoptional");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Verify that the class load
         assertLoadClassFail(bundleA, A.class.getName());

         // Verify bundle states
         assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testOptionalImportPackageWired() throws Exception
   {
      //Bundle-SymbolicName: packageimportoptional
      //Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageimportoptional");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testOptionalImportPackageNotWired() throws Exception
   {
      //Bundle-SymbolicName: packageimportoptional
      //Import-Package: org.jboss.test.osgi.classloader.support.a;resolution:=optional
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageimportoptional");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Resolve the installed bundles
         PackageAdmin packageAdmin = getPackageAdmin();
         boolean allResolved = packageAdmin.resolveBundles(new Bundle[] { bundleA });
         assertTrue("All resolved", allResolved);

         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class cannot be loaded from bundleA
            // because the wire could not be established when bundleA was resolved
            assertLoadClassFail(bundleA, A.class.getName());
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testBundleNameImportPackage() throws Exception
   {
      //Bundle-SymbolicName: bundlenameimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/bundlenameimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: simpleexport
         //Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testBundleNameImportPackageFails() throws Exception
   {
      //Bundle-SymbolicName: bundlenameimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-symbolic-name=simpleexport
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/bundlenameimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: sigleton;singleton:=true
         //Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/singleton", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClassFail(bundleA, A.class.getName());
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testBundleVersionImportPackage() throws Exception
   {
      //Bundle-SymbolicName: bundleversionimport
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[0.0.0,1.0.0)"
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/bundleversionimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testBundleVersionImportPackageFails() throws Exception
   {
      //Bundle-SymbolicName: bundleversionimportfails
      //Import-Package: org.jboss.test.osgi.classloader.support.a;bundle-version="[1.0.0,2.0.0)"
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/bundleversionimportfails");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClassFail(bundleA, A.class.getName());
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testRequireBundle() throws Exception
   {
      // [TODO] require bundle visibility

      //Bundle-SymbolicName: requirebundle
      //Require-Bundle: simpleexport
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/requirebundle");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testRequireBundleFails() throws Exception
   {
      //Bundle-SymbolicName: requirebundle
      //Require-Bundle: simpleexport
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/requirebundle");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Verify that the class load
         assertLoadClassFail(bundleA, A.class.getName());

         // Verify bundle states
         assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testRequireBundleOptional() throws Exception
   {
      //Bundle-SymbolicName: requirebundleoptional
      //Require-Bundle: simpleexport;resolution:=optional
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/requirebundleoptional");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Resolve the installed bundles
         PackageAdmin packageAdmin = getPackageAdmin();
         boolean allResolved = packageAdmin.resolveBundles(null);
         assertTrue("All resolved", allResolved);

         // Verify bundle states
         assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testRequireBundleVersion() throws Exception
   {
      //Bundle-SymbolicName: requirebundleversion
      //Require-Bundle: simpleexport;bundle-version="[0.0.0,1.0.0]"
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/requirebundleversion");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleB);
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testRequireBundleVersionFails() throws Exception
   {
      //Bundle-SymbolicName: versionrequirebundlefails
      //Require-Bundle: simpleexport;bundle-version="[1.0.0,2.0.0)"
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/requirebundleversionfails");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: simpleexport
         // Export-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClassFail(bundleA, A.class.getName());
            assertLoadClass(bundleB, A.class.getName(), bundleB);

            // Verify bundle states
            assertEquals("BundleA INSTALLED", Bundle.INSTALLED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPreferredExporterResolved() throws Exception
   {
      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleexport", A.class);

      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexportother", A.class);

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("bundleC", "/bundles/resolver/simpleimport");

      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Resolve the installed bundles
         PackageAdmin packageAdmin = getPackageAdmin();
         boolean allResolved = packageAdmin.resolveBundles(null);
         assertTrue("All resolved", allResolved);

         // Verify bundle states
         assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());

         Bundle bundleB = installBundle(assemblyB);
         try
         {
            Bundle bundleC = installBundle(assemblyC);
            try
            {
               // Verify that the class load
               assertLoadClass(bundleA, A.class.getName(), bundleA);
               assertLoadClass(bundleB, A.class.getName(), bundleB);
               assertLoadClass(bundleC, A.class.getName(), bundleA);

               // Verify bundle states
               assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
               assertEquals("BundleC RESOLVED", Bundle.RESOLVED, bundleC.getState());
            }
            finally
            {
               bundleC.uninstall();
            }
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPreferredExporterResolvedReverse() throws Exception
   {
      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleexport", A.class);

      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexportother", A.class);

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("bundleC", "/bundles/resolver/simpleimport");

      Bundle bundleB = installBundle(assemblyB);
      try
      {
         // Resolve the installed bundles
         PackageAdmin packageAdmin = getPackageAdmin();
         boolean allResolved = packageAdmin.resolveBundles(null);
         assertTrue("All resolved", allResolved);

         // Verify bundle states
         assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());

         Bundle bundleA = installBundle(assemblyA);
         try
         {
            Bundle bundleC = installBundle(assemblyC);
            try
            {
               allResolved = packageAdmin.resolveBundles(null);
               assertTrue("All resolved", allResolved);

               // Verify bundle states
               assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
               assertEquals("BundleC RESOLVED", Bundle.RESOLVED, bundleC.getState());

               // Verify that the class load
               assertLoadClass(bundleA, A.class.getName(), bundleA);
               assertLoadClass(bundleB, A.class.getName(), bundleB);
               assertLoadClass(bundleC, A.class.getName(), bundleB);
            }
            finally
            {
               bundleC.uninstall();
            }
         }
         finally
         {
            bundleA.uninstall();
         }
      }
      finally
      {
         bundleB.uninstall();
      }
   }

   //@Test
   public void testPreferredExporterHigherVersion() throws Exception
   {
      //Bundle-SymbolicName: packageexportversion100
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageexportversion100", A.class);

      //Bundle-SymbolicName: packageexportversion200
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
      Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageexportversion200", A.class);

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("bundleC", "/bundles/resolver/simpleimport");

      Bundle bundleA = installBundle(assemblyA);
      try
      {
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            Bundle bundleC = installBundle(assemblyC);
            try
            {
               // Verify that the class load
               assertLoadClass(bundleA, A.class.getName(), bundleA);
               assertLoadClass(bundleB, A.class.getName(), bundleB);
               assertLoadClass(bundleC, A.class.getName(), bundleB);

               // Verify bundle states
               assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
               assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
               assertEquals("BundleC RESOLVED", Bundle.RESOLVED, bundleC.getState());
            }
            finally
            {
               bundleC.uninstall();
            }
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPreferredExporterHigherVersionReverse() throws Exception
   {
      //Bundle-SymbolicName: packageexportversion200
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=2.0.0
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageexportversion200", A.class);

      //Bundle-SymbolicName: packageexportversion100
      //Export-Package: org.jboss.test.osgi.classloader.support.a;version=1.0.0
      Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageexportversion100", A.class);

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("bundleC", "/bundles/resolver/simpleimport");

      Bundle bundleA = installBundle(assemblyA);
      try
      {
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            Bundle bundleC = installBundle(assemblyC);
            try
            {
               // Verify that the class load
               assertLoadClass(bundleA, A.class.getName(), bundleA);
               assertLoadClass(bundleB, A.class.getName(), bundleB);
               assertLoadClass(bundleC, A.class.getName(), bundleA);

               // Verify bundle states
               assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
               assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
               assertEquals("BundleC RESOLVED", Bundle.RESOLVED, bundleC.getState());
            }
            finally
            {
               bundleC.uninstall();
            }
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPreferredExporterLowerId() throws Exception
   {
      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleexport", A.class);

      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexportother", A.class);

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("bundleC", "/bundles/resolver/simpleimport");

      Bundle bundleA = installBundle(assemblyA);
      try
      {
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Resolve the installed bundles
            PackageAdmin packageAdmin = getPackageAdmin();
            boolean allResolved = packageAdmin.resolveBundles(null);
            assertTrue("All resolved", allResolved);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());

            Bundle bundleC = installBundle(assemblyC);
            try
            {
               allResolved = packageAdmin.resolveBundles(null);
               assertTrue("All resolved", allResolved);

               // Verify bundle states
               assertEquals("BundleC RESOLVED", Bundle.RESOLVED, bundleC.getState());

               // Verify that the class load
               assertLoadClass(bundleA, A.class.getName(), bundleA);
               assertLoadClass(bundleB, A.class.getName(), bundleB);
               assertLoadClass(bundleC, A.class.getName(), bundleA);
            }
            finally
            {
               bundleC.uninstall();
            }
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPreferredExporterLowerIdReverse() throws Exception
   {
      // Bundle-SymbolicName: simpleexportother
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/simpleexportother", A.class);

      // Bundle-SymbolicName: simpleexport
      // Export-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleexport", A.class);

      // Bundle-SymbolicName: simpleimport
      // Import-Package: org.jboss.test.osgi.classloader.support.a
      Archive<?> assemblyC = assembleArchive("bundleC", "/bundles/resolver/simpleimport");

      Bundle bundleA = installBundle(assemblyA);
      try
      {
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Resolve the installed bundles
            PackageAdmin packageAdmin = getPackageAdmin();
            boolean allResolved = packageAdmin.resolveBundles(null);
            assertTrue("All resolved", allResolved);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());

            Bundle bundleC = installBundle(assemblyC);
            try
            {
               allResolved = packageAdmin.resolveBundles(null);
               assertTrue("All resolved", allResolved);

               // Verify bundle states
               assertEquals("BundleC RESOLVED", Bundle.RESOLVED, bundleC.getState());

               // Verify that the class load
               assertLoadClass(bundleA, A.class.getName(), bundleA);
               assertLoadClass(bundleB, A.class.getName(), bundleB);
               assertLoadClass(bundleC, A.class.getName(), bundleA);
            }
            finally
            {
               bundleC.uninstall();
            }
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPackageAttribute() throws Exception
   {
      //Bundle-SymbolicName: packageexportattribute
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageexportattribute", A.class);
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: simpleimport
         //Import-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleimport");
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleA);
            assertLoadClass(bundleB, A.class.getName(), bundleA);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }

         //Bundle-SymbolicName: packageimportattribute
         //Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
         assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageimportattribute");
         bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleA);
            assertLoadClass(bundleB, A.class.getName(), bundleA);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPackageAttributeFails() throws Exception
   {
      //Bundle-SymbolicName: packageexportattribute
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageexportattribute", A.class);
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: packageimportattributefails
         //Import-Package: org.jboss.test.osgi.classloader.support.a;test=y
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageimportattributefails");
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleA);
            assertLoadClassFail(bundleB, A.class.getName());

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB INSTALLED", Bundle.INSTALLED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPackageAttributeMandatory() throws Exception
   {
      //Bundle-SymbolicName: packageexportattributemandatory
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageexportattributemandatory", A.class);
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: packageimportattribute
         //Import-Package: org.jboss.test.osgi.classloader.support.a;test=x
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/packageimportattribute");
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleA);
            assertLoadClass(bundleB, A.class.getName(), bundleA);

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB RESOLVED", Bundle.RESOLVED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testPackageAttributeMandatoryFails() throws Exception
   {
      //Bundle-SymbolicName: packageexportattributemandatory
      //Export-Package: org.jboss.test.osgi.classloader.support.a;test=x;mandatory:=test
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/packageexportattributemandatory", A.class);
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         //Bundle-SymbolicName: simpleimport
         //Import-Package: org.jboss.test.osgi.classloader.support.a
         Archive<?> assemblyB = assembleArchive("bundleB", "/bundles/resolver/simpleimport");
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            // Verify that the class load
            assertLoadClass(bundleA, A.class.getName(), bundleA);
            assertLoadClassFail(bundleB, A.class.getName());

            // Verify bundle states
            assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
            assertEquals("BundleB INSTALLED", Bundle.INSTALLED, bundleB.getState());
         }
         finally
         {
            bundleB.uninstall();
         }
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   //@Test
   public void testSystemPackageImport() throws Exception
   {
      //Bundle-SymbolicName: systempackageimport
      //Import-Package: org.osgi.framework;version=1.4
      Archive<?> assemblyA = assembleArchive("bundleA", "/bundles/resolver/systempackageimport");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Resolve the installed bundles
         PackageAdmin packageAdmin = getPackageAdmin();
         boolean allResolved = packageAdmin.resolveBundles(null);
         assertTrue("All resolved", allResolved);

         // Verify bundle states
         assertEquals("BundleA RESOLVED", Bundle.RESOLVED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }
}