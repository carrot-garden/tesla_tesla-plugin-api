package org.sonatype.maven.plugin;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention( RUNTIME )
@Target( { TYPE } )
public @interface GeneratedResourceRoot
{

    String[] directory();

    String[] filtering() default {};

    String[] targetPath() default {};

    String[] includes() default {};

    String[] excludes() default {};

    FileKind kind() default FileKind.MAIN;

}
