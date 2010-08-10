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
package org.jboss.test.osgi.container.bundle;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.jar.Attributes;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.osgi.container.bundle.support.a.ObjectA;
import org.jboss.test.osgi.container.bundle.support.a.ObjectA2;
import org.jboss.test.osgi.container.bundle.support.b.ObjectB;
import org.jboss.test.osgi.container.bundle.support.x.ObjectX;
import org.jboss.test.osgi.container.bundle.support.y.ObjectY;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.Version;

/**
 * BundleTest.
 *
 * @author thomas.diesler@jboss.com
 * @author <a href="david@redhat.com">David Bosschaert</a>
 */
public class BundleTestCase extends OSGiFrameworkTest
{
   @Test
   public void testBundleId() throws Exception
   {
      long id1 = -1;
      Archive<?> assembly = assembleArchive("simple-bundle1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         id1 = bundle.getBundleId();
      }
      finally
      {
         bundle.uninstall();
      }
      assertEquals(id1, bundle.getBundleId());

      long id2 = -1;
      bundle = installBundle(assembly);
      try
      {
         id2 = bundle.getBundleId();
      }
      finally
      {
         bundle.uninstall();
      }
      assertEquals(id2, bundle.getBundleId());
      assertTrue("Ids should be different" + id1 + "," + id2, id1 != id2);
   }

   @Test
   public void testSymbolicName() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple-bundle1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         assertEquals("simple1", bundle.getSymbolicName());
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testState() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple-bundle1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         assertEquals(Bundle.INSTALLED, bundle.getState());

         bundle.start();
         assertEquals(Bundle.ACTIVE, bundle.getState());

         bundle.stop();
         assertEquals(Bundle.RESOLVED, bundle.getState());
      }
      finally
      {
         bundle.uninstall();
      }
      assertEquals(Bundle.UNINSTALLED, bundle.getState());
   }

   @Test
   public void testGetBundleContext() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple-bundle1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         BundleContext bundleContext = bundle.getBundleContext();
         assertNull(bundleContext);

         bundle.start();
         bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         bundle.stop();
         bundleContext = bundle.getBundleContext();
         assertNull(bundleContext);
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testLastModified() throws Exception
   {
      // TODO testLastModified
   }

   @Test
   public void testStartStop() throws Exception
   {
      // TODO testStartStop
   }

   @Test
   public void testUpdate() throws Exception
   {
      Archive<?> assembly1 = assembleArchive("bundle1", "/bundles/update/update-bundle1", ObjectA.class);
      Archive<?> assembly2 = assembleArchive("bundle2", "/bundles/update/update-bundle101", ObjectB.class);
      Archive<?> assemblyy = assembleArchive("bundley", "/bundles/update/update-bundley", ObjectY.class);
      Bundle bundle = installBundle(assembly1);
      Bundle bundleY = installBundle(assemblyy);
      try
      {
         BundleContext systemContext = getFramework().getBundleContext();
         int beforeCount = systemContext.getBundles().length;

         bundleY.start();
         assertBundleState(Bundle.ACTIVE, bundleY.getState());

         bundle.start();
         assertBundleState(Bundle.ACTIVE, bundle.getState());
         assertEquals(Version.parseVersion("1.0.0"), bundle.getVersion());
         assertEquals("update-bundle1", bundle.getSymbolicName());
         assertLoadClass(bundle, ObjectA.class.getName());
         assertLoadClassFail(bundle, ObjectB.class.getName());

         Class<?> clsY = bundleY.loadClass(ObjectY.class.getName());

         bundle.update(toVirtualFile(assembly2).openStream());
         assertBundleState(Bundle.ACTIVE, bundle.getState());
         assertEquals(Version.parseVersion("1.0.1"), bundle.getVersion());
         // Nobody depends on the packages, so we can update them straight away
         assertLoadClass(bundle, ObjectB.class.getName());
         assertLoadClassFail(bundle, ObjectA.class.getName());

         assertSame(clsY, bundleY.loadClass(ObjectY.class.getName()));
         assertBundleState(Bundle.ACTIVE, bundleY.getState());

         int afterCount = systemContext.getBundles().length;
         assertEquals("Bundle count", beforeCount, afterCount);
      }
      finally
      {
         bundleY.uninstall();
         bundle.uninstall();

         // Call this method in the finally block of any test that uses Bundle.update()
         packageAdminRefreshAll();
      }
   }

   @Test
   public void testUpdateImportedPackagesRemoved() throws Exception
   {
      Archive<?> assemblyx = assembleArchive("bundlex", "/bundles/update/update-bundlex", ObjectX.class);
      Archive<?> assembly1 = assembleArchive("bundle1", "/bundles/update/update-bundle1", ObjectA.class);
      Archive<?> assembly2 = assembleArchive("bundle2", "/bundles/update/update-bundle101", ObjectB.class);

      Bundle bundleA = installBundle(assembly1);
      Bundle bundleX = installBundle(assemblyx);
      try
      {
         BundleContext systemContext = getFramework().getBundleContext();
         int beforeCount = systemContext.getBundles().length;

         bundleA.start();
         bundleX.start();

         Class<?> cls = bundleX.loadClass(ObjectX.class.getName());
         cls.newInstance();

         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         assertBundleState(Bundle.ACTIVE, bundleX.getState());
         assertEquals(Version.parseVersion("1.0.0"), bundleA.getVersion());
         assertEquals("update-bundle1", bundleA.getSymbolicName());
         assertLoadClass(bundleA, ObjectA.class.getName());
         assertLoadClassFail(bundleA, ObjectB.class.getName());
         assertLoadClass(bundleX, ObjectA.class.getName());

         bundleA.update(toVirtualFile(assembly2).openStream());
         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         assertBundleState(Bundle.ACTIVE, bundleX.getState());
         assertEquals(Version.parseVersion("1.0.1"), bundleA.getVersion());
         // Assembly X depends on a package in the bundle, this should still be available 
         assertLoadClass(bundleX, ObjectA.class.getName());

         getSystemContext().addFrameworkListener(this);
         getPackageAdmin().refreshPackages(new Bundle[] { bundleA });
         assertFrameworkEvent(FrameworkEvent.ERROR, bundleX, BundleException.class);
         assertFrameworkEvent(FrameworkEvent.PACKAGES_REFRESHED, getSystemContext().getBundle(0), null);

         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         // Bundle X is installed because it cannot be resolved any more
         assertBundleState(Bundle.INSTALLED, bundleX.getState());
         assertEquals(Version.parseVersion("1.0.1"), bundleA.getVersion());
         // Nobody depends on the packages, so we can update them straight away
         assertLoadClass(bundleA, ObjectB.class.getName());
         assertLoadClassFail(bundleA, ObjectA.class.getName());

         int afterCount = systemContext.getBundles().length;
         assertEquals("Bundle count", beforeCount, afterCount);
      }
      finally
      {
         getSystemContext().removeFrameworkListener(this);
         bundleX.uninstall();
         bundleA.uninstall();

         // Call this method in the finally block of any test that uses Bundle.update()
         packageAdminRefreshAll();
      }
   }

   @Test
   public void testUpdateImportedPackages() throws Exception
   {
      Archive<?> assemblyx = assembleArchive("bundlex", "/bundles/update/update-bundlex", ObjectX.class);
      Archive<?> assembly1 = assembleArchive("bundle1", new String[] { "/bundles/update/update-bundle1", "/bundles/update/classes1" });
      Archive<?> assembly2 = assembleArchive("bundle2", new String[] { "/bundles/update/update-bundle102", "/bundles/update/classes2" });

      Bundle bundleA = installBundle(assembly1);
      Bundle bundleX = installBundle(assemblyx);
      try
      {
         BundleContext systemContext = getFramework().getBundleContext();
         int beforeCount = systemContext.getBundles().length;

         bundleA.start();
         bundleX.start();

         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         assertBundleState(Bundle.ACTIVE, bundleX.getState());
         assertEquals(Version.parseVersion("1.0.0"), bundleA.getVersion());
         assertEquals("update-bundle1", bundleA.getSymbolicName());
         assertLoadClass(bundleA, ObjectA.class.getName());
         assertLoadClassFail(bundleA, ObjectA2.class.getName());
         assertLoadClass(bundleX, ObjectA.class.getName());
         assertLoadClassFail(bundleX, ObjectA2.class.getName());

         Class<?> cls = bundleX.loadClass(ObjectX.class.getName());
         Object x1 = cls.newInstance();
         assertEquals("ObjectX contains reference: ObjectA", x1.toString());

         bundleA.update(toVirtualFile(assembly2).openStream());
         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         assertBundleState(Bundle.ACTIVE, bundleX.getState());
         assertEquals(Version.parseVersion("1.0.2"), bundleA.getVersion());
         // Bundle A should see the new version of the packages
         assertLoadClass(bundleA, ObjectA2.class.getName());
         assertLoadClassFail(bundleA, ObjectA.class.getName());
         // Bundle X should still see the old packages of bundle A
         assertLoadClass(bundleX, ObjectA.class.getName());
         assertLoadClassFail(bundleX, ObjectA2.class.getName());
         assertSame(cls, bundleX.loadClass(ObjectX.class.getName()));

         getSystemContext().addFrameworkListener(this);
         getPackageAdmin().refreshPackages(new Bundle[] { bundleA });
         assertFrameworkEvent(FrameworkEvent.PACKAGES_REFRESHED, getSystemContext().getBundle(0), null);

         assertBundleState(Bundle.ACTIVE, bundleA.getState());
         assertBundleState(Bundle.ACTIVE, bundleX.getState());
         assertEquals(Version.parseVersion("1.0.2"), bundleA.getVersion());
         assertLoadClass(bundleA, ObjectA2.class.getName());
         assertLoadClassFail(bundleA, ObjectA.class.getName());
         assertLoadClass(bundleX, ObjectA2.class.getName());
         assertLoadClassFail(bundleX, ObjectA.class.getName());

         Class<?> cls2 = bundleX.loadClass(ObjectX.class.getName());
         assertNotSame("Should have loaded a new class", cls, cls2);
         Object x2 = cls2.newInstance();
         assertEquals("ObjectX contains reference: ObjectA2", x2.toString());

         int afterCount = systemContext.getBundles().length;
         assertEquals("Bundle count", beforeCount, afterCount);
      }
      finally
      {
         getSystemContext().removeFrameworkListener(this);
         bundleX.uninstall();
         bundleA.uninstall();

         // Call this method in the finally block of any test that uses Bundle.update()
         packageAdminRefreshAll();
      }
   }

   @Test
   public void testUpdateReadError() throws Exception
   {
      Archive<?> assembly1 = assembleArchive("bundle1", "/bundles/update/update-bundle1", ObjectA.class);

      Bundle bundle = installBundle(assembly1);
      try
      {
         BundleContext systemContext = getFramework().getBundleContext();
         int beforeCount = systemContext.getBundles().length;

         bundle.start();
         assertBundleState(Bundle.ACTIVE, bundle.getState());
         assertEquals(Version.parseVersion("1.0.0"), bundle.getVersion());
         assertEquals("update-bundle1", bundle.getSymbolicName());
         assertLoadClass(bundle, ObjectA.class.getName());
         assertLoadClassFail(bundle, ObjectB.class.getName());

         InputStream ismock = mock(InputStream.class);
         when(ismock.read()).thenThrow(new IOException());
         when(ismock.read((byte[])Mockito.anyObject())).thenThrow(new IOException());
         when(ismock.read((byte[])Mockito.anyObject(), Mockito.anyInt(), Mockito.anyInt())).thenThrow(new IOException());

         try
         {
            bundle.update(ismock);
            fail("Should have thrown a BundleException as the InputStream is unreadable");
         }
         catch (BundleException e)
         {
            // good
         }
         assertBundleState(Bundle.ACTIVE, bundle.getState());
         assertEquals(Version.parseVersion("1.0.0"), bundle.getVersion());
         assertEquals("update-bundle1", bundle.getSymbolicName());
         assertLoadClass(bundle, ObjectA.class.getName());
         assertLoadClassFail(bundle, ObjectB.class.getName());

         int afterCount = systemContext.getBundles().length;
         assertEquals("Bundle count", beforeCount, afterCount);
      }
      finally
      {
         bundle.uninstall();

         // Call this method in the finally block of any test that uses Bundle.update()
         packageAdminRefreshAll();
      }
   }

   @Test
   @Ignore
   public void testUpdateExceptionStart() throws Exception
   {
      // TODO
   }

   @Test
   @Ignore
   public void testUpdateExceptionStop()
   {
      // TODO
   }

   @Test
   @Ignore
   public void testBundleUpdateLocation()
   {
      // TODO
   }

   @Test
   @Ignore
   public void testImportOldPackage()
   {
      // TODO
   }

   @Test
   public void testUninstall() throws Exception
   {
      // TODO testUninstall
   }

   @Test
   public void testSingleton() throws Exception
   {
      Archive<?> assemblyA = assembleArchive("bundle10", "/bundles/singleton/singleton1");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         Archive<?> assemblyB = assembleArchive("bundle20", "/bundles/singleton/singleton2");
         Bundle bundleB = installBundle(assemblyB);
         bundleB.uninstall();
         fail("BundleException expected");
      }
      catch (BundleException t)
      {
         // expected
      }
      finally
      {
         bundleA.uninstall();
      }
   }

   @Test
   public void testNotSingleton() throws Exception
   {
      // Bundle-SymbolicName: singleton;singleton:=true
      Archive<?> assemblyA = assembleArchive("bundle1", "/bundles/singleton/singleton1");
      Bundle bundleA = installBundle(assemblyA);
      try
      {
         // Bundle-SymbolicName: singleton
         // Bundle-Version: 2.0.0
         Archive<?> assemblyB = assembleArchive("not-singleton", "/bundles/singleton/not-singleton");
         Bundle bundleB = installBundle(assemblyB);
         try
         {
            assertEquals(bundleA.getSymbolicName(), bundleB.getSymbolicName());
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

   @Test
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public void testGetHeaders() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple-bundle1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         Dictionary expected = new Hashtable();
         expected.put(Constants.BUNDLE_NAME, "Simple1");
         expected.put(Constants.BUNDLE_SYMBOLICNAME, "simple1");
         expected.put(Constants.BUNDLE_MANIFESTVERSION, "2");
         expected.put(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
         expected.put(Attributes.Name.IMPLEMENTATION_TITLE.toString(), "JBoss OSGi tests");
         expected.put(Attributes.Name.IMPLEMENTATION_VENDOR.toString(), "jboss.org");
         expected.put(Attributes.Name.IMPLEMENTATION_VERSION.toString(), "test");

         Dictionary dictionary = bundle.getHeaders();
         assertEquals(expected, dictionary);
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testLocation() throws Exception
   {
      // TODO testGetLocation
   }

   @Test
   public void testGetRegisteredServices() throws Exception
   {
      // TODO testGetRegisteredServices
   }

   @Test
   public void testServicesInUse() throws Exception
   {
      // TODO testServicesInUse
   }

   @Test
   public void testHasPermission() throws Exception
   {
      // TODO testHasPermission
   }

   @Test
   public void testGetResources() throws Exception
   {
      // TODO testGetResource(s)
   }

   @Test
   public void testLoadClass() throws Exception
   {
      // TODO testLoadClass
   }

   // Call this method at the end of a finally block of any test that uses Bundle.update()
   private void packageAdminRefreshAll() throws BundleException, Exception
   {
      getSystemContext().addFrameworkListener(this);
      getPackageAdmin().refreshPackages(null);
      assertFrameworkEvent(FrameworkEvent.PACKAGES_REFRESHED, getSystemContext().getBundle(0), null);
      getSystemContext().removeFrameworkListener(this);
   }
}
