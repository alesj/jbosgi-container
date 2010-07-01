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
package org.jboss.test.osgi.framework.jbosgi323;

// $Id: $

import java.io.InputStream;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.Archives;
import org.jboss.shrinkwrap.api.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.framework.classloader.support.a.A;
import org.jboss.test.osgi.framework.classloader.support.b.B;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * [JBOSGI-323] DynamicImport-Package takes presendence over embedded classes
 *
 * https://jira.jboss.org/jira/browse/JBOSGI-323
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-May-2010
 */
@Ignore
public class OSGi323TestCase extends OSGiFrameworkTest
{
   @After
   public void tearDown() throws Exception
   {
      // Make sure we have a new framework for every test
      shutdownFramework();
      super.tearDown();
   }
   
   @Test
   public void testStaticImport() throws Exception
   {
      // Bundle-SymbolicName: jbosgi323-bundleA
      // DynamicImport-Package: org.jboss.test.osgi.classloader.support.a
      final JavaArchive archiveA = Archives.create("jbosgi323-bundleA", JavaArchive.class);
      archiveA.addClass(A.class);
      archiveA.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archiveA.getName());
            builder.addImportPackages("org.jboss.test.osgi.classloader.support.a");
            return builder.openStream();
         }
      });

      // Bundle-SymbolicName: jbosgi323-bundleB
      // Export-Package: org.jboss.test.osgi.classloader.support.a, org.jboss.test.osgi.classloader.support.b
      final JavaArchive archiveB = Archives.create("jbosgi323-bundleB", JavaArchive.class);
      archiveB.addClasses(A.class, B.class);
      archiveB.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archiveB.getName());
            builder.addExportPackages("org.jboss.test.osgi.classloader.support.a");
            builder.addExportPackages("org.jboss.test.osgi.classloader.support.b");
            return builder.openStream();
         }
      });

      Bundle bundleA = installBundle(archiveA);
      Bundle bundleB = installBundle(archiveB);

      assertLoadClass(bundleA, A.class.getName(), bundleB);
      assertLoadClassFail(bundleA, B.class.getName());

      assertLoadClass(bundleB, A.class.getName(), bundleB);
      assertLoadClass(bundleB, B.class.getName(), bundleB);

      assertBundleState(Bundle.RESOLVED, bundleA.getState());
      assertBundleState(Bundle.RESOLVED, bundleB.getState());
   }
   
   @Test
   public void testDynamicImportWithPackage() throws Exception
   {
      // Bundle-SymbolicName: jbosgi323-bundleA
      // DynamicImport-Package: org.jboss.test.osgi.classloader.support.a
      final JavaArchive archiveA = Archives.create("jbosgi323-bundleA", JavaArchive.class);
      archiveA.addClass(A.class);
      archiveA.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archiveA.getName());
            builder.addDynamicImportPackages("org.jboss.test.osgi.classloader.support.a");
            return builder.openStream();
         }
      });

      // Bundle-SymbolicName: jbosgi323-bundleB
      // Export-Package: org.jboss.test.osgi.classloader.support.a, org.jboss.test.osgi.classloader.support.b
      final JavaArchive archiveB = Archives.create("jbosgi323-bundleB", JavaArchive.class);
      archiveB.addClasses(A.class, B.class);
      archiveB.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archiveB.getName());
            builder.addExportPackages("org.jboss.test.osgi.classloader.support.a");
            builder.addExportPackages("org.jboss.test.osgi.classloader.support.b");
            return builder.openStream();
         }
      });

      Bundle bundleA = installBundle(archiveA);
      Bundle bundleB = installBundle(archiveB);

      assertLoadClass(bundleA, A.class.getName(), bundleA);
      assertLoadClassFail(bundleA, B.class.getName());

      assertLoadClass(bundleB, A.class.getName(), bundleB);
      assertLoadClass(bundleB, B.class.getName(), bundleB);

      assertBundleState(Bundle.RESOLVED, bundleA.getState());
      assertBundleState(Bundle.RESOLVED, bundleB.getState());
   }
   
   @Test
   public void testDynamicImportWithWildcard() throws Exception
   {
      // Bundle-SymbolicName: jbosgi323-bundleA
      // DynamicImport-Package: *
      final JavaArchive archiveA = Archives.create("jbosgi323-bundleA", JavaArchive.class);
      archiveA.addClass(A.class);
      archiveA.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archiveA.getName());
            builder.addDynamicImportPackages("*");
            return builder.openStream();
         }
      });

      // Bundle-SymbolicName: jbosgi323-bundleB
      // Export-Package: org.jboss.test.osgi.classloader.support.a, org.jboss.test.osgi.classloader.support.b
      final JavaArchive archiveB = Archives.create("jbosgi323-bundleB", JavaArchive.class);
      archiveB.addClasses(A.class, B.class);
      archiveB.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archiveB.getName());
            builder.addExportPackages("org.jboss.test.osgi.classloader.support.a");
            builder.addExportPackages("org.jboss.test.osgi.classloader.support.b");
            return builder.openStream();
         }
      });

      Bundle bundleA = installBundle(archiveA);
      Bundle bundleB = installBundle(archiveB);

      assertLoadClass(bundleA, A.class.getName(), bundleA);
      assertLoadClass(bundleA, B.class.getName(), bundleB);

      assertLoadClass(bundleB, A.class.getName(), bundleB);
      assertLoadClass(bundleB, B.class.getName(), bundleB);

      assertBundleState(Bundle.RESOLVED, bundleA.getState());
      assertBundleState(Bundle.RESOLVED, bundleB.getState());
   }
}
