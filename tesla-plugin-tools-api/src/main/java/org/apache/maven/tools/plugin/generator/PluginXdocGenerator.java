package org.apache.maven.tools.plugin.generator;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.DefaultPluginToolsRequest;
import org.apache.maven.tools.plugin.ExtendedMojoDescriptor;
import org.apache.maven.tools.plugin.PluginToolsRequest;
import org.apache.maven.tools.plugin.util.PluginUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

/**
 * @todo add example usage tag that can be shown in the doco
 * @version $Id: PluginXdocGenerator.java 938263 2010-04-26 22:48:25Z bentmann $
 */
public class PluginXdocGenerator
    implements Generator
{
    /** locale */
    private final Locale locale;

    /** project */
    private final MavenProject project;

    /**
     * Default constructor using <code>Locale.ENGLISH</code> as locale.
     * Used only in test cases.
     */
    public PluginXdocGenerator()
    {
        this.project = null;
        this.locale = Locale.ENGLISH;
    }

    /**
     * Constructor using <code>Locale.ENGLISH</code> as locale.
     *
     * @param project not null Maven project.
     */
    public PluginXdocGenerator( MavenProject project )
    {
        this.project = project;
        this.locale = Locale.ENGLISH;
    }

    /**
     * @param project not null.
     * @param locale not null wanted locale.
     */
    public PluginXdocGenerator( MavenProject project, Locale locale )
    {
        this.project = project;
        if ( locale == null )
        {
            this.locale = Locale.ENGLISH;
        }
        else
        {
            this.locale = locale;
        }
    }

    /** {@inheritDoc} */
    public void execute( File destinationDirectory, PluginDescriptor pluginDescriptor )
        throws IOException
    {
        execute( destinationDirectory, new DefaultPluginToolsRequest( project, pluginDescriptor ) );
    }

    /** {@inheritDoc} */
    public void execute( File destinationDirectory, PluginToolsRequest request )
        throws IOException
    {
        if ( request.getPluginDescriptor().getMojos() != null )
        {
            for ( Iterator it = request.getPluginDescriptor().getMojos().iterator(); it.hasNext(); )
            {
                MojoDescriptor descriptor = (MojoDescriptor) it.next();

                processMojoDescriptor( descriptor, destinationDirectory );
            }
        }
    }

    /**
     * @param mojoDescriptor not null
     * @param destinationDirectory not null
     * @throws IOException if any
     */
    protected void processMojoDescriptor( MojoDescriptor mojoDescriptor, File destinationDirectory )
        throws IOException
    {
        File outputFile = new File( destinationDirectory, getMojoFilename( mojoDescriptor, "xml" ) );
        String encoding = "UTF-8";
        Writer writer = null;
        try
        {
            writer = new OutputStreamWriter( new FileOutputStream( outputFile ), encoding );

            XMLWriter w = new PrettyPrintXMLWriter( new PrintWriter( writer ), encoding, null );
            writeBody( mojoDescriptor, w );

            writer.flush();
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    /**
     * @param mojo not null
     * @param ext not null
     * @return the output file name
     */
    private String getMojoFilename( MojoDescriptor mojo, String ext )
    {
        return mojo.getGoal() + "-mojo." + ext;
    }

    /**
     * @param mojoDescriptor not null
     * @param w not null
     */
    private void writeBody( MojoDescriptor mojoDescriptor, XMLWriter w )
    {
        w.startElement( "document" );
        w.addAttribute( "xmlns", "http://maven.apache.org/XDOC/2.0" );
        w.addAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        w.addAttribute( "xsi:schemaLocation", "http://maven.apache.org/XDOC/2.0 http://maven.apache.org/xsd/xdoc-2.0.xsd" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        w.startElement( "properties" );

        w.startElement( "title" );
        w.writeText( mojoDescriptor.getFullGoalName() );
        w.endElement(); // title

        w.endElement(); // properties

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        w.startElement( "body" );

        w.startElement( "section" );

        w.addAttribute( "name", mojoDescriptor.getFullGoalName() );

        //writeReportNotice( mojoDescriptor, w );

        w.startElement( "p" );
        w.writeMarkup( getString( "pluginxdoc.mojodescriptor.fullname" ) );
        w.endElement(); //p
        w.startElement( "p" );
        w.writeMarkup( mojoDescriptor.getPluginDescriptor().getGroupId() + ":"
            + mojoDescriptor.getPluginDescriptor().getArtifactId() + ":"
            + mojoDescriptor.getPluginDescriptor().getVersion() + ":" + mojoDescriptor.getGoal() );
        w.endElement(); //p

        if ( StringUtils.isNotEmpty( mojoDescriptor.getDeprecated() ) )
        {
            w.startElement( "p" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.deprecated" ) );
            w.endElement(); // p
            w.startElement( "div" );
            w.writeMarkup( PluginUtils.makeHtmlValid( mojoDescriptor.getDeprecated() ) );
            w.endElement(); // div
        }

        w.startElement( "p" );
        w.writeMarkup( getString( "pluginxdoc.description" ) );
        w.endElement(); //p
        w.startElement( "div" );
        if ( StringUtils.isNotEmpty( mojoDescriptor.getDescription() ) )
        {
            w.writeMarkup( PluginUtils.makeHtmlValid( mojoDescriptor.getDescription() ) );
        }
        else
        {
            w.writeText( getString( "pluginxdoc.nodescription" ) );
        }
        w.endElement(); // div

        writeGoalAttributes( mojoDescriptor, w );

        writeGoalParameterTable( mojoDescriptor, w );

        w.endElement(); // section

        w.endElement(); // body

        w.endElement(); // document
    }

    /**
     * @param mojoDescriptor not null
     * @param w not null
     */
    private void writeGoalAttributes( MojoDescriptor mojoDescriptor, XMLWriter w )
    {
        w.startElement( "p" );
        w.writeMarkup( getString( "pluginxdoc.mojodescriptor.attributes" ) );
        w.endElement(); //p

        boolean addedUl = false;
        String value;
        if ( mojoDescriptor.isProjectRequired() )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.projectRequired" ) );
            w.endElement(); //li
        }

        if ( mojoDescriptor.isAggregator() )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.aggregator" ) );
            w.endElement(); //li
        }

        if ( mojoDescriptor.isDirectInvocationOnly() )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.directInvocationOnly" ) );
            w.endElement(); //li
        }

        value = mojoDescriptor.isDependencyResolutionRequired();
        if ( StringUtils.isNotEmpty( value ) )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.dependencyResolutionRequired", value ) );
            w.endElement(); //li
        }

        if ( mojoDescriptor instanceof ExtendedMojoDescriptor )
        {
            ExtendedMojoDescriptor extendedMojoDescriptor = (ExtendedMojoDescriptor) mojoDescriptor;

            value = extendedMojoDescriptor.getDependencyCollectionRequired();
            if ( StringUtils.isNotEmpty( value ) )
            {
                if ( !addedUl )
                {
                    w.startElement( "ul" );
                    addedUl = true;
                }
                w.startElement( "li" );
                w.writeMarkup( format( "pluginxdoc.mojodescriptor.dependencyCollectionRequired", value ) );
                w.endElement(); //li
            }

            if ( extendedMojoDescriptor.isThreadSafe() )
            {
                if ( !addedUl )
                {
                    w.startElement( "ul" );
                    addedUl = true;
                }
                w.startElement( "li" );
                w.writeMarkup( getString( "pluginxdoc.mojodescriptor.threadSafe" ) );
                w.endElement(); //li
            }

        }

        value = mojoDescriptor.getSince();
        if ( StringUtils.isNotEmpty( value ) )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.since", value ) );
            w.endElement(); //li
        }

        value = mojoDescriptor.getPhase();
        if ( StringUtils.isNotEmpty( value ) )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.phase", value ) );
            w.endElement(); //li
        }

        value = mojoDescriptor.getExecutePhase();
        if ( StringUtils.isNotEmpty( value ) )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.executePhase", value ) );
            w.endElement(); //li
        }

        value = mojoDescriptor.getExecuteGoal();
        if ( StringUtils.isNotEmpty( value ) )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.executeGoal", value ) );
            w.endElement(); //li
        }

        value = mojoDescriptor.getExecuteLifecycle();
        if ( StringUtils.isNotEmpty( value ) )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.executeLifecycle", value ) );
            w.endElement(); //li
        }

        if ( mojoDescriptor.isOnlineRequired() )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.onlineRequired" ) );
            w.endElement(); //li
        }

        if ( !mojoDescriptor.isInheritedByDefault() )
        {
            if ( !addedUl )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            w.startElement( "li" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.inheritedByDefault" ) );
            w.endElement(); //li
        }

        if ( addedUl )
        {
            w.endElement(); //ul
        }
    }

    /**
     * @param mojoDescriptor not null
     * @param w not null
     */
    private void writeGoalParameterTable( MojoDescriptor mojoDescriptor, XMLWriter w )
    {
        List parameterList = mojoDescriptor.getParameters();

        //remove components and read-only parameters
        List list = filterParameters( parameterList );

        if ( list != null && list.size() > 0 )
        {
            writeParameterSummary( mojoDescriptor, list, w );

            writeParameterDetails( mojoDescriptor, list, w );
        }
        else
        {
            w.startElement( "subsection" );
            w.addAttribute( "name", getString( "pluginxdoc.mojodescriptor.parameters" ) );

            w.startElement( "p" );
            w.writeMarkup( getString( "pluginxdoc.mojodescriptor.noParameter" ) );
            w.endElement(); //p

            w.endElement();
        }
    }

    /**
     * @param parameterList not null
     * @return the parameters list without components.
     */
    private List filterParameters( List parameterList )
    {
        List filtered = new ArrayList();

        if ( parameterList != null )
        {
            for ( Iterator parameters = parameterList.iterator(); parameters.hasNext(); )
            {
                Parameter parameter = (Parameter) parameters.next();

                if ( parameter.isEditable() )
                {
                    String expression = parameter.getExpression();

                    if ( expression == null || !expression.startsWith( "${component." ) )
                    {
                        filtered.add( parameter );
                    }
                }
            }
        }

        return filtered;
    }

    /**
     * @param mojoDescriptor not null
     * @param parameterList not null
     * @param w not null
     */
    private void writeParameterDetails( MojoDescriptor mojoDescriptor, List parameterList, XMLWriter w )
    {
        w.startElement( "subsection" );
        w.addAttribute( "name", getString( "pluginxdoc.mojodescriptor.parameter.details" ) );

        for ( Iterator parameters = parameterList.iterator(); parameters.hasNext(); )
        {
            Parameter parameter = (Parameter) parameters.next();

            w.startElement( "p" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.parameter.name_internal", parameter.getName() ) );
            w.endElement(); //p

            if ( StringUtils.isNotEmpty( parameter.getDeprecated() ) )
            {
                w.startElement( "div" );
                w.writeMarkup( format( "pluginxdoc.mojodescriptor.parameter.deprecated",
                                       PluginUtils.makeHtmlValid( parameter.getDeprecated() ) ) );
                w.endElement(); // div
            }

            w.startElement( "div" );
            if ( StringUtils.isNotEmpty( parameter.getDescription() ) )
            {
                w.writeMarkup( PluginUtils.makeHtmlValid( parameter.getDescription() ) );
            }
            else
            {
                w.writeMarkup( getString( "pluginxdoc.nodescription" ) );
            }
            w.endElement(); // div

            boolean addedUl = false;
            if ( !addedUl && StringUtils.isNotEmpty( parameter.getType() ) )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.type" ), parameter.getType(), w );

            if ( StringUtils.isNotEmpty( parameter.getSince() ) )
            {
                if ( !addedUl )
                {
                    w.startElement( "ul" );
                    addedUl = true;
                }
                writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.since" ), parameter.getSince(), w );
            }
            else
            {
                if ( StringUtils.isNotEmpty( mojoDescriptor.getSince() ) )
                {
                    if ( !addedUl )
                    {
                        w.startElement( "ul" );
                        addedUl = true;
                    }
                    writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.since" ),
                                 mojoDescriptor.getSince(), w );
                }
            }

            if ( parameter.isRequired() )
            {
                if ( !addedUl )
                {
                    w.startElement( "ul" );
                    addedUl = true;
                }
                writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.required" ),
                             getString( "pluginxdoc.yes" ), w );
            }
            else
            {
                if ( !addedUl )
                {
                    w.startElement( "ul" );
                    addedUl = true;
                }
                writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.required" ),
                             getString( "pluginxdoc.no" ), w );
            }

            if ( !addedUl && StringUtils.isNotEmpty( parameter.getExpression() ) )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.expression" ), parameter.getExpression(), w );

            if ( !addedUl && StringUtils.isNotEmpty( parameter.getDefaultValue() ) )
            {
                w.startElement( "ul" );
                addedUl = true;
            }
            writeDetail( getString( "pluginxdoc.mojodescriptor.parameter.default" ),
                         escapeXml( parameter.getDefaultValue() ), w );

            if ( addedUl )
            {
                w.endElement(); //ul
            }

            if ( parameters.hasNext() )
            {
                w.writeMarkup( "<hr/>" );
            }
        }

        w.endElement();
    }

    /**
     * @param param not null
     * @param value could be null
     * @param w not null
     */
    private void writeDetail( String param, String value, XMLWriter w )
    {
        if ( StringUtils.isNotEmpty( value ) )
        {
            w.startElement( "li" );
            w.writeMarkup( format( "pluginxdoc.detail", new String[]{ param, value } ) );
            w.endElement(); //li
        }
    }

    /**
     * @param mojoDescriptor not null
     * @param parameterList not null
     * @param w not null
     */
    private void writeParameterSummary( MojoDescriptor mojoDescriptor, List parameterList, XMLWriter w )
    {
        List requiredParams = getParametersByRequired( true, parameterList );
        if ( requiredParams.size() > 0 )
        {
            writeParameterList( mojoDescriptor, getString( "pluginxdoc.mojodescriptor.requiredParameters" ),
                                requiredParams, w );
        }

        List optionalParams = getParametersByRequired( false, parameterList );
        if ( optionalParams.size() > 0 )
        {
            writeParameterList( mojoDescriptor, getString( "pluginxdoc.mojodescriptor.optionalParameters" ),
                                optionalParams, w );
        }
    }

    /**
     * @param mojoDescriptor not null
     * @param title not null
     * @param parameterList not null
     * @param w not null
     */
    private void writeParameterList( MojoDescriptor mojoDescriptor, String title, List parameterList, XMLWriter w )
    {
        w.startElement( "subsection" );
        w.addAttribute( "name", title );

        w.startElement( "table" );
        w.addAttribute( "border", "0" );
        w.addAttribute( "align", "left" );

        w.startElement( "tr" );
        w.startElement( "th" );
        w.writeText( getString( "pluginxdoc.mojodescriptor.parameter.name" ) );
        w.endElement(); //th
        w.startElement( "th" );
        w.writeText( getString( "pluginxdoc.mojodescriptor.parameter.type" ) );
        w.endElement(); //th
        w.startElement( "th" );
        w.writeText( getString( "pluginxdoc.mojodescriptor.parameter.since" ) );
        w.endElement(); //th
        w.startElement( "th" );
        w.writeText( getString( "pluginxdoc.mojodescriptor.parameter.description" ) );
        w.endElement(); //th
        w.endElement(); //tr

        for ( Iterator parameters = parameterList.iterator(); parameters.hasNext(); )
        {
            Parameter parameter = (Parameter) parameters.next();

            w.startElement( "tr" );
            w.startElement( "td" );
            w.writeMarkup( format( "pluginxdoc.mojodescriptor.parameter.name_link", parameter.getName() ) );
            w.endElement(); //td
            w.startElement( "td" );
            int index = parameter.getType().lastIndexOf( "." );
            w.writeMarkup( "<code>" + parameter.getType().substring( index + 1 ) + "</code>" );
            w.endElement(); //td
            w.startElement( "td" );
            if ( StringUtils.isNotEmpty( parameter.getSince() ) )
            {
                w.writeMarkup( "<code>" + parameter.getSince() + "</code>" );
            }
            else
            {
                if ( StringUtils.isNotEmpty( mojoDescriptor.getSince() ) )
                {
                    w.writeMarkup( "<code>" + mojoDescriptor.getSince() + "</code>" );
                }
                else
                {
                    w.writeMarkup( "<code>-</code>" );
                }
            }
            w.endElement(); //td
            w.startElement( "td" );
            String description;
            if ( StringUtils.isNotEmpty( parameter.getDeprecated() ) )
            {
                description =
                    format( "pluginxdoc.mojodescriptor.parameter.deprecated",
                            PluginUtils.makeHtmlValid( parameter.getDeprecated() ) );
            }
            else if ( StringUtils.isNotEmpty( parameter.getDescription() ) )
            {
                description = PluginUtils.makeHtmlValid( parameter.getDescription() );
            }
            else
            {
                description = getString( "pluginxdoc.nodescription" );
            }
            w.writeMarkup( description + "<br/>" );

            if ( StringUtils.isNotEmpty( parameter.getDefaultValue() ) )
            {
                w.writeMarkup( format( "pluginxdoc.mojodescriptor.parameter.defaultValue",
                                       escapeXml( parameter.getDefaultValue() ) ) );
            }
            w.endElement(); //td
            w.endElement(); //tr
        }

        w.endElement(); //table
        w.endElement(); //section
    }

    /**
     * @param required <code>true</code> for required parameters, <code>false</code> otherwise.
     * @param parameterList not null
     * @return list of parameters depending the value of <code>required</code>
     */
    private List getParametersByRequired( boolean required, List parameterList )
    {
        List list = new ArrayList();

        for ( Iterator parameters = parameterList.iterator(); parameters.hasNext(); )
        {
            Parameter parameter = (Parameter) parameters.next();

            if ( parameter.isRequired() == required )
            {
                list.add( parameter );
            }
        }

        return list;
    }

    /**
     * Gets the resource bundle for the <code>locale</code> instance variable.
     *
     * @return The resource bundle for the <code>locale</code> instance variable.
     */
    private ResourceBundle getBundle()
    {
        return ResourceBundle.getBundle( "pluginxdoc", locale, getClass().getClassLoader() );
    }

    /**
     * @param key not null
     * @return Localized, text identified by <code>key</code>.
     * @see #getBundle()
     */
    private String getString( String key )
    {
        return getBundle().getString( key );
    }

    /**
     * Convenience method.
     *
     * @param key not null
     * @param arg1 not null
     * @return Localized, formatted text identified by <code>key</code>.
     * @see #format(String, Object[])
     */
    private String format( String key, Object arg1 )
    {
        return format( key, new Object[] { arg1 } );
    }

    /**
     * Looks up the value for <code>key</code> in the <code>ResourceBundle</code>,
     * then formats that value for the specified <code>Locale</code> using <code>args</code>.
     *
     * @param key not null
     * @param args not null
     * @return Localized, formatted text identified by <code>key</code>.
     */
    private String format( String key, Object[] args )
    {
        String pattern = getString( key );
        // we don't need quoting so spare us the confusion in the resource bundle to double them up in some keys
        pattern = StringUtils.replace( pattern, "'", "''" );

        MessageFormat messageFormat = new MessageFormat( "" );
        messageFormat.setLocale( locale );
        messageFormat.applyPattern( pattern );

        return messageFormat.format( args );
    }

    /**
     * @param text the string to escape
     * @return A string escaped with XML entities
     */
    private String escapeXml( String text )
    {
        if ( text != null )
        {
            text = text.replaceAll( "&", "&amp;" );
            text = text.replaceAll( "<", "&lt;" );
            text = text.replaceAll( ">", "&gt;" );
            text = text.replaceAll( "\"", "&quot;" );
            text = text.replaceAll( "\'", "&apos;" );
        }
        return text;
    }

}
