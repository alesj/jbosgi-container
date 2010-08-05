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
package org.jboss.test.osgi.container.xservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.osgi.testing.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.test.osgi.container.xservice.bundleA.BundleActivatorA;
import org.jboss.test.osgi.container.xservice.bundleA.BundleServiceA;
import org.jboss.test.osgi.container.xservice.bundleB.BundleActivatorB;
import org.jboss.test.osgi.container.xservice.bundleB.BundleServiceB;
import org.jboss.test.osgi.container.xservice.moduleA.ModuleActivatorA;
import org.jboss.test.osgi.container.xservice.moduleA.ModuleServiceA;
import org.jboss.test.osgi.container.xservice.moduleB.ModuleActivatorB;
import org.jboss.test.osgi.container.xservice.moduleB.ModuleServiceB;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

/**
 * Test that an MSC module can have a dependency on an OSGi bundle and vice versa. 
 *
 * @author Thomas.Diesler@jboss.com
 * @since 12-Jul-2010
 */
public class ModuleServiceTestCase extends OSGiFrameworkTest
{
   @Test
   public void testModuleService() throws Exception
   {
      Bundle moduleAS = installBundle(getModuleAS());
      try
      {
         assertNotNull("Bundle not null", moduleAS);
         assertEquals("moduleAS", moduleAS.getSymbolicName());
         assertEquals(Version.parseVersion("1.0"), moduleAS.getVersion());

         moduleAS.start();
         assertBundleState(Bundle.ACTIVE, moduleAS.getState());

         BundleContext context = moduleAS.getBundleContext();
         assertNotNull("Context not null", context);

         ServiceReference sref = context.getServiceReference(ModuleServiceA.class.getName());
         assertNotNull("Service ref not null", sref);

         Object service = context.getService(sref);
         assertNotNull("Service not null", service);

         String was = invokeService(service, "hello");
         assertEquals("hello:moduleAS", was);
         
         moduleAS.stop();
         assertBundleState(Bundle.RESOLVED, moduleAS.getState());
      }
      finally
      {
         moduleAS.uninstall();
      }
   }

   @Test
   public void testModuleDependsOnBundle() throws Exception
   {
      Bundle moduleBS = installBundle(getModuleBS());
      try
      {
         // Install the dependent bundle
         Bundle bundleB = installBundle(getBundleB());
         try
         {
            bundleB.start();
            assertBundleState(Bundle.ACTIVE, bundleB.getState());
            
            moduleBS.start();
            assertBundleState(Bundle.ACTIVE, moduleBS.getState());

            BundleContext context = moduleBS.getBundleContext();
            ServiceReference sref = context.getServiceReference(ModuleServiceB.class.getName());
            String was = invokeService(context.getService(sref), "hello");
            assertEquals("hello:moduleBS:xservice.bundleB:1.0.0", was);
         }
         finally
         {
            bundleB.uninstall();
         }
         
         moduleBS.stop();
         assertBundleState(Bundle.RESOLVED, moduleBS.getState());
      }
      finally
      {
         moduleBS.uninstall();
      }
   }
   
   @Test
   public void testBundleDependsOnModule() throws Exception
   {
      Bundle bundleA = installBundle(getBundleA());
      try
      {
         // Install the dependent module
         Bundle moduleAS = installBundle(getModuleAS());
         try
         {
            moduleAS.start();
            assertBundleState(Bundle.ACTIVE, moduleAS.getState());
            
            bundleA.start();
            assertBundleState(Bundle.ACTIVE, bundleA.getState());

            BundleContext context = bundleA.getBundleContext();
            ServiceReference sref = context.getServiceReference(BundleServiceA.class.getName());
            String was = invokeService(context.getService(sref), "hello");
            assertEquals("hello:bundleA:1.0.0", was);
         }
         finally
         {
            moduleAS.uninstall();
         }
         
         bundleA.stop();
         assertBundleState(Bundle.RESOLVED, bundleA.getState());
      }
      finally
      {
         bundleA.uninstall();
      }
   }
   
   private String invokeService(Object service, String exp) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
   {
      Method method = service.getClass().getMethod("echo", new Class<?>[] { String.class });
      String was = (String)method.invoke(service, exp);
      return was;
   }

   private JavaArchive getBundleA()
   {
      // Bundle-SymbolicName: bundleA
      // Bundle-Activator: org.jboss.test.osgi.container.xservice.bundleA.BundleActivatorA
      // Require-Bundle: moduleAS;bundle-version:=1.0.0
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "bundleA");
      archive.addClasses(BundleActivatorA.class, BundleServiceA.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleVersion("1.0.0");
            builder.addBundleActivator(BundleActivatorA.class);
            builder.addRequireBundle("moduleAS;bundle-version:=1.0.0"); //;visibility:=reexport
            return builder.openStream();
         }
      });
      return archive;
   }
   
   private JavaArchive getBundleB()
   {
      // Bundle-Version: 1.0.0
      // Bundle-SymbolicName: xservice.bundleB
      // Bundle-Activator: org.jboss.test.osgi.container.xservice.bundleB.BundleActivatorB
      // Export-Package: org.jboss.test.osgi.container.xservice.bundleB
      final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "xservice.bundleB");
      archive.addClasses(BundleActivatorB.class, BundleServiceB.class);
      archive.setManifest(new Asset()
      {
         public InputStream openStream()
         {
            OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
            builder.addBundleManifestVersion(2);
            builder.addBundleSymbolicName(archive.getName());
            builder.addBundleVersion("1.0.0");
            builder.addBundleActivator(BundleActivatorB.class);
            builder.addExportPackages(BundleServiceB.class);
            return builder.openStream();
         }
      });
      return archive;
   }
   
   private JavaArchive getModuleAS()
   {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "moduleAS");
      archive.addManifestResource(getResourceFile("xservice/moduleAS/META-INF/module.xml"));
      archive.addClasses(ModuleActivatorA.class, ModuleServiceA.class);
      return archive;
   }

   private JavaArchive getModuleBS()
   {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "moduleBS");
      archive.addManifestResource(getResourceFile("xservice/moduleBS/META-INF/module.xml"));
      archive.addClasses(ModuleActivatorB.class, ModuleServiceB.class);
      return archive;
   }
}
