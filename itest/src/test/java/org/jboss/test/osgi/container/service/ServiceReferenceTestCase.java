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
package org.jboss.test.osgi.container.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.osgi.testing.OSGiFrameworkTest;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.test.osgi.container.service.support.a.A;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * ServiceReferenceTest.
 *
 * todo more isAssignableTests
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @author Thomas.Diesler@jboss.com
 * @version $Revision: 1.1 $
 */
@Ignore
public class ServiceReferenceTestCase extends OSGiFrameworkTest
{
   @Test
   public void testGetProperty() throws Exception
   {
      ServiceReference sref = null;
      String[] clazzes = new String[] { BundleContext.class.getName() };
      Object serviceID = null;

      Archive<?> assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testA", "a");
         properties.put("testB", "b");
         properties.put("MiXeD", "Case");
         ServiceRegistration sreg = bundleContext.registerService(clazzes, bundleContext, properties);
         assertNotNull(sreg);

         sref = sreg.getReference();
         assertNotNull(sref);

         serviceID = sref.getProperty(Constants.SERVICE_ID);
         assertNotNull(serviceID);
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID.toLowerCase()));
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID.toUpperCase()));
         assertArrayEquals(clazzes, (String[])sref.getProperty(Constants.OBJECTCLASS));
         assertArrayEquals(clazzes, (String[])sref.getProperty(Constants.OBJECTCLASS.toLowerCase()));
         assertArrayEquals(clazzes, (String[])sref.getProperty(Constants.OBJECTCLASS.toUpperCase()));
         assertEquals("a", sref.getProperty("testA"));
         assertEquals("b", sref.getProperty("testB"));
         assertEquals("Case", sref.getProperty("MiXeD"));
         assertEquals("Case", sref.getProperty("mixed"));
         assertEquals("Case", sref.getProperty("MIXED"));
         assertNull(sref.getProperty(null));
         assertNull(sref.getProperty("doesNotExist"));

         properties.put("testA", "notA");
         assertEquals("a", sref.getProperty("testA"));
         properties.put(Constants.SERVICE_ID, "rubbish");
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID));
         properties.put(Constants.OBJECTCLASS, "rubbish");
         assertEquals(clazzes, sref.getProperty(Constants.OBJECTCLASS));

         sreg.setProperties(properties);
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, sref.getProperty(Constants.OBJECTCLASS));
         assertEquals("notA", sref.getProperty("testA"));
         assertEquals("b", sref.getProperty("testB"));
         assertEquals("Case", sref.getProperty("MiXeD"));
         assertEquals("Case", sref.getProperty("mixed"));
         assertEquals("Case", sref.getProperty("MIXED"));

         sreg.setProperties(null);
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, sref.getProperty(Constants.OBJECTCLASS));
         assertNull(sref.getProperty("testA"));
         assertNull(sref.getProperty("testB"));
         assertNull(sref.getProperty("MiXeD"));
         assertNull(sref.getProperty("mixed"));
         assertNull(sref.getProperty("MIXED"));
         assertNull(sref.getProperty(null));

         sreg.setProperties(properties);
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, sref.getProperty(Constants.OBJECTCLASS));
         assertEquals("notA", sref.getProperty("testA"));
         assertEquals("b", sref.getProperty("testB"));
         assertEquals("Case", sref.getProperty("MiXeD"));
         assertEquals("Case", sref.getProperty("mixed"));
         assertEquals("Case", sref.getProperty("MIXED"));
         assertNull(sref.getProperty(null));

         sreg.unregister();
         assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID));
         assertEquals(clazzes, sref.getProperty(Constants.OBJECTCLASS));
         assertEquals("notA", sref.getProperty("testA"));
         assertEquals("b", sref.getProperty("testB"));
         assertEquals("Case", sref.getProperty("MiXeD"));
         assertEquals("Case", sref.getProperty("mixed"));
         assertEquals("Case", sref.getProperty("MIXED"));
         assertNull(sref.getProperty(null));
      }
      finally
      {
         bundle.uninstall();
      }

      assertEquals(serviceID, sref.getProperty(Constants.SERVICE_ID));
      assertEquals(clazzes, sref.getProperty(Constants.OBJECTCLASS));
      assertEquals("notA", sref.getProperty("testA"));
      assertEquals("b", sref.getProperty("testB"));
      assertEquals("Case", sref.getProperty("MiXeD"));
      assertEquals("Case", sref.getProperty("mixed"));
      assertEquals("Case", sref.getProperty("MIXED"));
      assertNull(sref.getProperty(null));
   }

   @Test
   public void testGetPropertyKeys() throws Exception
   {
      ServiceReference sref = null;

      Archive<?> assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put("testA", "a");
         properties.put("testB", "b");
         properties.put("MiXeD", "Case");
         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, properties);
         assertNotNull(sreg);

         sref = sreg.getReference();
         assertNotNull(sref);

         assertPropertyKeys(sref, "testA", "testB", "MiXeD");

         properties.put("testC", "c");
         assertPropertyKeys(sref, "testA", "testB", "MiXeD");

         sreg.setProperties(properties);
         assertPropertyKeys(sref, "testA", "testB", "testC", "MiXeD");

         sreg.setProperties(null);
         assertPropertyKeys(sref);

         sreg.setProperties(properties);
         assertPropertyKeys(sref, "testA", "testB", "testC", "MiXeD");

         sreg.unregister();
         assertPropertyKeys(sref, "testA", "testB", "testC", "MiXeD");
      }
      finally
      {
         bundle.uninstall();
      }
      assertPropertyKeys(sref, "testA", "testB", "testC", "MiXeD");
   }

   private void assertPropertyKeys(ServiceReference sref, String... expectedKeys)
   {
      Set<String> expected = new HashSet<String>();
      expected.add(Constants.SERVICE_ID);
      expected.add(Constants.OBJECTCLASS);
      for (String key : expectedKeys)
         expected.add(key);

      Set<String> actual = new HashSet<String>();
      for (String key : sref.getPropertyKeys())
         actual.add(key);

      assertEquals(expected, actual);
   }

   @Test
   public void testGetBundle() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(sreg);

         ServiceReference sref = sreg.getReference();
         assertNotNull(sref);

         Bundle other = sref.getBundle();
         assertEquals(bundle, other);

         sreg.unregister();

         other = sref.getBundle();
         assertNull("" + other, other);
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testGetBundleAfterStop() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(sreg);

         ServiceReference sref = sreg.getReference();
         assertNotNull(sref);

         Bundle other = sref.getBundle();
         assertEquals(bundle, other);

         bundle.stop();

         other = sref.getBundle();
         assertNull("" + other, other);
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testUsingBundles() throws Exception
   {
      Archive<?> assembly1 = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle1 = installBundle(assembly1);
      try
      {
         bundle1.start();
         BundleContext bundleContext = bundle1.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(sreg);

         ServiceReference sref = sreg.getReference();
         assertNotNull(sref);

         assertUsingBundles(sref);

         Archive<?> assembly2 = assembleArchive("simple2", "/bundles/simple/simple-bundle2");
         Bundle bundle2 = installBundle(assembly2);
         try
         {
            bundle2.start();
            BundleContext bundleContext2 = bundle2.getBundleContext();
            assertNotNull(bundleContext2);

            bundleContext2.getService(sref);
            assertUsingBundles(sref, bundle2);

            bundleContext2.ungetService(sref);
            assertUsingBundles(sref);

            bundleContext2.getService(sref);
            bundleContext2.getService(sref);
            assertUsingBundles(sref, bundle2);
            bundleContext2.ungetService(sref);
            assertUsingBundles(sref, bundle2);
            bundleContext2.ungetService(sref);
            assertUsingBundles(sref);

            bundleContext.getService(sref);
            bundleContext2.getService(sref);
            assertUsingBundles(sref, bundle1, bundle2);

            sreg.unregister();
            assertUsingBundles(sref);
         }
         finally
         {
            bundle2.uninstall();
         }
      }
      finally
      {
         bundle1.uninstall();
      }
   }

   @Test
   public void testUsingBundlesAfterStop() throws Exception
   {
      Archive<?> assembly1 = assembleArchive("simple1", "/bundles/simple/simple-bundle1");
      Bundle bundle1 = installBundle(assembly1);
      try
      {
         bundle1.start();
         BundleContext bundleContext = bundle1.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(sreg);

         ServiceReference sref = sreg.getReference();
         assertNotNull(sref);

         assertUsingBundles(sref);

         Archive<?> assembly2 = assembleArchive("simple2", "/bundles/simple/simple-bundle2");
         Bundle bundle2 = installBundle(assembly2);
         try
         {
            bundle2.start();
            BundleContext bundleContext2 = bundle2.getBundleContext();
            assertNotNull(bundleContext2);

            bundleContext.getService(sref);
            bundleContext2.getService(sref);
            assertUsingBundles(sref, bundle1, bundle2);

            bundle1.stop();
            assertUsingBundles(sref);
         }
         finally
         {
            bundle2.uninstall();
         }
      }
      finally
      {
         bundle1.uninstall();
      }
   }

   @Test
   public void testIsAssignableToErrors() throws Exception
   {
      Archive<?> assembly = assembleArchive("simple1", "/bundles/simple/simple-bundle1", A.class);
      Bundle bundle = installBundle(assembly);
      try
      {
         bundle.start();
         BundleContext bundleContext = bundle.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(sreg);

         ServiceReference sref = sreg.getReference();
         assertNotNull(sref);

         try
         {
            sref.isAssignableTo(null, A.class.getName());
            fail("Should not be here!");
         }
         catch (IllegalArgumentException t)
         {
            // expected
         }

         try
         {
            sref.isAssignableTo(bundle, null);
            fail("Should not be here!");
         }
         catch (IllegalArgumentException t)
         {
            // expected
         }
      }
      finally
      {
         bundle.uninstall();
      }
   }

   @Test
   public void testNotAssignableTo() throws Exception
   {
      Archive<?> assembly1 = assembleArchive("simple1", "/bundles/simple/simple-bundle1", A.class);
      Bundle bundle1 = installBundle(assembly1);
      try
      {
         bundle1.start();
         BundleContext bundleContext = bundle1.getBundleContext();
         assertNotNull(bundleContext);

         ServiceRegistration sreg = bundleContext.registerService(BundleContext.class.getName(), bundleContext, null);
         assertNotNull(sreg);

         ServiceReference sref = sreg.getReference();
         assertNotNull(sref);

         Archive<?> assembly2 = assembleArchive("simple2", "/bundles/simple/simple-bundle2", A.class);
         Bundle bundle2 = installBundle(assembly2);
         try
         {
            assertFalse(sref.isAssignableTo(bundle2, A.class.getName()));
            assertTrue(sref.isAssignableTo(bundle2, String.class.getName()));

            sreg.unregister();
            assertFalse(sref.isAssignableTo(bundle2, A.class.getName()));
            assertFalse(sref.isAssignableTo(bundle2, String.class.getName())); // review ???
         }
         finally
         {
            bundle2.uninstall();
         }
      }
      finally
      {
         bundle1.uninstall();
      }
   }

   @Test
   public void testIsAssignableTo() throws Exception
   {
      //Bundle-Name: Service2
      //Bundle-SymbolicName: org.jboss.test.osgi.service2
      //Export-Package: org.jboss.test.osgi.service.support.a
      Archive<?> assembly2 = assembleArchive("service2", "/bundles/service/service-bundle2", A.class);
      Bundle bundle2 = installBundle(assembly2);

      try
      {
         bundle2.start();
         BundleContext bundleContext2 = bundle2.getBundleContext();
         assertNotNull(bundleContext2);

         //Bundle-Name: Service1
         //Bundle-SymbolicName: org.jboss.test.osgi.service1
         //Import-Package: org.jboss.test.osgi.service.support.a
         Archive<?> assembly1 = assembleArchive("service1", "/bundles/service/service-bundle1");
         Bundle bundle1 = installBundle(assembly1);

         try
         {

            ServiceRegistration sreg = bundleContext2.registerService(BundleContext.class.getName(), bundleContext2, null);
            assertNotNull(sreg);

            ServiceReference sref = sreg.getReference();
            assertNotNull(sref);

            assertTrue(sref.isAssignableTo(bundle2, A.class.getName()));
            assertTrue(sref.isAssignableTo(bundle2, String.class.getName()));
            assertTrue(sref.isAssignableTo(bundle1, A.class.getName()));
            assertTrue(sref.isAssignableTo(bundle1, String.class.getName()));

            sreg.unregister();
            assertTrue(sref.isAssignableTo(bundle2, A.class.getName()));
            assertTrue(sref.isAssignableTo(bundle2, String.class.getName()));
            assertFalse(sref.isAssignableTo(bundle1, A.class.getName()));
            assertFalse(sref.isAssignableTo(bundle1, String.class.getName())); // review ???
         }
         finally
         {
            bundle1.uninstall();
         }
      }
      finally
      {
         bundle2.uninstall();
      }
   }

   @Test
   public void testCompareTo() throws Exception
   {
      BundleContext context = getFramework().getBundleContext();
      assertNotNull(context);

      ServiceRegistration sreg1 = context.registerService(BundleContext.class.getName(), context, null);
      assertNotNull(sreg1);

      ServiceReference sref1 = sreg1.getReference();
      assertNotNull(sref1);

      ServiceRegistration sreg2 = context.registerService(BundleContext.class.getName(), context, null);
      assertNotNull(sreg2);

      ServiceReference sref2 = sreg2.getReference();
      assertNotNull(sref2);

      Dictionary<String, Object> properties = new Hashtable<String, Object>();
      properties.put(Constants.SERVICE_RANKING, 10);
      ServiceRegistration sreg3 = context.registerService(BundleContext.class.getName(), context, properties);
      assertNotNull(sreg3);

      ServiceReference sref3 = sreg3.getReference();
      assertNotNull(sref3);

      properties = new Hashtable<String, Object>();
      properties.put(Constants.SERVICE_RANKING, -10);
      ServiceRegistration sreg4 = context.registerService(BundleContext.class.getName(), context, properties);
      assertNotNull(sreg4);

      ServiceReference sref4 = sreg4.getReference();
      assertNotNull(sref4);

      assertGreaterRanking(sref1, sref2);
      assertGreaterRanking(sref3, sref1);
      assertGreaterRanking(sref3, sref2);
      assertGreaterRanking(sref1, sref4);
      assertGreaterRanking(sref2, sref4);
      assertGreaterRanking(sref3, sref4);

      try
      {
         sref1.compareTo(null);
         fail("Should not be here!");
      }
      catch (IllegalArgumentException t)
      {
         // expected
      }

      try
      {
         sref1.compareTo(new Object());
         fail("Should not be here!");
      }
      catch (IllegalArgumentException t)
      {
         // expected
      }

      properties = new Hashtable<String, Object>();
      properties.put(Constants.SERVICE_RANKING, "NotANumber");
      ServiceRegistration sreg5 = context.registerService(BundleContext.class.getName(), context, properties);
      assertNotNull(sreg5);

      ServiceReference sref5 = sreg5.getReference();
      assertNotNull(sref5);

      assertGreaterRanking(sref1, sref5); 

      Set<ServiceReference> ordering = new TreeSet<ServiceReference>();
      ordering.add(sref1);
      ordering.add(sref2);
      ordering.add(sref3);
      ordering.add(sref4);
      ordering.add(sref5);
      Iterator<ServiceReference> iterator = ordering.iterator();
      assertEquals(sref4, iterator.next());
      assertEquals(sref5, iterator.next());
      assertEquals(sref2, iterator.next());
      assertEquals(sref1, iterator.next());
      assertEquals(sref3, iterator.next());

      ordering = new TreeSet<ServiceReference>();
      ordering.add(sref5);
      ordering.add(sref4);
      ordering.add(sref3);
      ordering.add(sref2);
      ordering.add(sref1);
      iterator = ordering.iterator();
      assertEquals(sref4, iterator.next());
      assertEquals(sref5, iterator.next());
      assertEquals(sref2, iterator.next());
      assertEquals(sref1, iterator.next());
      assertEquals(sref3, iterator.next());
   }

   @Test
   public void testServiceReferenceOrder() throws Exception
   {
      Runnable runIt = new Runnable()
      {
         public void run()
         {
         }
      };
      
      BundleContext context = getFramework().getBundleContext();
      Hashtable<String, Object> props = new Hashtable<String, Object>();
      props.put(Constants.SERVICE_DESCRIPTION, "min value");
      props.put(Constants.SERVICE_RANKING, Integer.MIN_VALUE);
      ServiceRegistration sreg1 = context.registerService(Runnable.class.getName(), runIt, props);
      ServiceReference sref1 = sreg1.getReference();
      
      props.put(Constants.SERVICE_DESCRIPTION, "max value 1");
      props.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
      ServiceRegistration sreg2 = context.registerService(Runnable.class.getName(), runIt, props);
      ServiceReference sref2 = sreg2.getReference();
      
      props.put(Constants.SERVICE_DESCRIPTION, "max value 2");
      props.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
      ServiceRegistration sreg3 = context.registerService(Runnable.class.getName(), runIt, props);
      ServiceReference sref3 = sreg3.getReference();
      
      props.put(Constants.SERVICE_DESCRIPTION, "max value 3");
      props.put(Constants.SERVICE_RANKING, Integer.MAX_VALUE);
      ServiceRegistration sreg4 = context.registerService(Runnable.class.getName(), runIt, props);
      ServiceReference sref4 = sreg4.getReference();
      
      ServiceReference[] srefs = context.getServiceReferences(Runnable.class.getName(), null);
      assertEquals(4, srefs.length);
      assertEquals(sref1, srefs[0]);
      assertEquals(sref4, srefs[1]);
      assertEquals(sref3, srefs[2]);
      assertEquals(sref2, srefs[3]);
      
      ServiceReference sref = context.getServiceReference(Runnable.class.getName());
      assertEquals(sref2, sref);
      sreg2.unregister();
      
      sref = context.getServiceReference(Runnable.class.getName());
      assertEquals(sref3, sref);
      sreg3.unregister();
      
      sref = context.getServiceReference(Runnable.class.getName());
      assertEquals(sref4, sref);
      sreg4.unregister();

      sref = context.getServiceReference(Runnable.class.getName());
      assertEquals(sref1, sref);
   }

   protected void assertGreaterRanking(ServiceReference sref1, ServiceReference sref2) throws Exception
   {
      assertTrue(sref1 + " > " + sref2, sref1.compareTo(sref2) > 0);
      assertTrue(sref2 + " < " + sref1, sref2.compareTo(sref1) < 0);
   }
}
