/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.osgi.framework.metadata.internal;

import java.io.Serializable;
import java.util.List;

import org.jboss.osgi.framework.metadata.ActivationPolicyMetaData;

/**
 * Activation policy impl.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class AbstractActivationPolicyMetaData implements ActivationPolicyMetaData, Serializable
{
   private static final long serialVersionUID = 1L;

   private String type = "lazy";
   private List<String> includes;
   private List<String> excludes;

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public List<String> getIncludes()
   {
      return includes;
   }

   public void setIncludes(List<String> includes)
   {
      this.includes = includes;
   }

   public List<String> getExcludes()
   {
      return excludes;
   }

   public void setExcludes(List<String> excludes)
   {
      this.excludes = excludes;
   }
}
