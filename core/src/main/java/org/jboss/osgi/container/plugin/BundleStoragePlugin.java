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
package org.jboss.osgi.container.plugin;

// $Id$

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.osgi.framework.Bundle;

/**
 * An abstraction of a bundle persistent storage system.
 * 
 * @author thomas.diesler@jboss.com
 * @since 18-Aug-2009
 */
public interface BundleStoragePlugin extends Plugin 
{
   File getStorageDir(Bundle bundle);
   
   File getBundleStreamDir();
   
   File getDataFile(Bundle bundle, String filename);
   
   File storeBundleStream(String location, InputStream input, int revisionCount) throws IOException;

   void cleanStorage(String propValue);
}