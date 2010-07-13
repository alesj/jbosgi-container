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
package org.jboss.test.osgi.container.bundle.support.a;

//$Id: ServiceA.java 85293 2009-03-05 13:45:47Z thomas.diesler@jboss.com $

import org.jboss.test.osgi.container.bundle.support.b.ServiceB;
import org.jboss.test.osgi.container.bundle.support.x.X;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * ServiceA has a dependency on ServiceB, both have a dependency on SomePojo
 * 
 * @author thomas.diesler@jboss.com
 * @since 02-Mar-2009
 */
public class ServiceA
{
   ServiceA(BundleContext context)
   {
      ServiceTracker tracker = new ServiceTracker(context, ServiceB.class.getName(), null)
      {
         @Override
         public Object addingService(ServiceReference sref)
         {
            ServiceB serviceB = (ServiceB)super.addingService(sref);
            serviceB.doStuffInB(new X("hello"));
            return serviceB;
         }
      };
      tracker.open();
   }
}
