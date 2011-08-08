package org.apache.maven.tools.plugin.extractor;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.descriptor.InvalidPluginDescriptorException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.PluginToolsRequest;

import java.util.List;

/**
 * @author jdcasey
 * @version $Id: MojoDescriptorExtractor.java 746366 2009-02-20 21:13:39Z jdcasey $
 */
public interface MojoDescriptorExtractor
{
    /** Plexus role for lookup */
    String ROLE = MojoDescriptorExtractor.class.getName();


    /**
     * Execute the mojo extraction.
     *
     * @return a list of mojo descriptors.
     * @throws ExtractionException if any
     * @throws InvalidPluginDescriptorException if any
     * 
     * @deprecated Use {@link MojoDescriptorExtractor#execute(PluginToolsRequest)} instead. 
     *     Provided for backward compatibility with maven-plugin-plugin &lt; 2.5.
     */
    List execute( MavenProject project, PluginDescriptor pluginDescriptor )
        throws ExtractionException, InvalidPluginDescriptorException;
    
    /**
     * Execute the mojo extraction.
     *
     * @param request The {@link PluginToolsRequest} containing information for the extraction process.
     * @return a list of mojo descriptors.
     * @throws ExtractionException if any
     * @throws InvalidPluginDescriptorException if any
     * @since 2.5
     */
    List execute( PluginToolsRequest request )
        throws ExtractionException, InvalidPluginDescriptorException;
}