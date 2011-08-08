package org.apache.maven.plugin.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.tools.plugin.generator.Generator;
import org.apache.maven.tools.plugin.generator.PluginDescriptorGenerator;

import java.io.File;

/**
 * Generate a plugin descriptor.
 * <br/>
 * <b>Note:</b> Phase is after the "compilation" of any scripts.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id: DescriptorGeneratorMojo.java 940010 2010-05-01 13:35:33Z bentmann $
 * @since 2.0
 * @goal descriptor
 * @phase process-classes
 * @requiresDependencyResolution runtime
 */
public class DescriptorGeneratorMojo
    extends AbstractGeneratorMojo
{
    /**
     * The directory where the generated <code>plugin.xml</code> file will be put.
     *
     * @parameter default-value="${project.build.outputDirectory}/META-INF/maven"
     */
    protected File outputDirectory;

    /**
     * A flag to disable generation of the <code>plugin.xml</code> in favor of a hand authored plugin descriptor.
     * 
     * @parameter default-value="false"
     * @since 2.6
     */
    private boolean skipDescriptor;

    /** {@inheritDoc} */
    protected File getOutputDirectory()
    {
        return outputDirectory;
    }

    /** {@inheritDoc} */
    protected Generator createGenerator()
    {
        return new PluginDescriptorGenerator();
    }

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        if ( skipDescriptor )
        {
            return;
        }

        super.execute();
    }

}
