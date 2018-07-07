/*******************************************************************************
 * Copyright (c) 2018 Jorge Lanza, 
 *                    David Gomez, 
 *                    Luis Sanchez,
 *                    Juan Ramon Santana
 *
 * For the full copyright and license information, please view the LICENSE
 * file that is distributed with this source code.
 *******************************************************************************/
package eu.fiesta_iot.platform.annotator.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiesta_iot.platform.annotator.rest.exceptions.BadRequestExceptionHandler;
import eu.fiesta_iot.platform.annotator.rest.exceptions.NotFoundExceptionHandler;
import eu.fiesta_iot.platform.annotator.rest.exceptions.RestWebApplicationExceptionMapper;
import eu.fiesta_iot.platform.annotator.rest.exceptions.UncaughtThrowableExceptionMapper;

public class JaxRsActivator extends Application {
	private static final Logger log =
	        LoggerFactory.getLogger(JaxRsActivator.class);
	
	Set<Object> singletons = new HashSet<Object>();
	Set<Class<?>> classes = new HashSet<Class<?>>();

	public JaxRsActivator() {
		log.info("***** Activating Annotator as a Service *****");

		classes.add(AnnotatorRestService.class);
		
		classes.add(NotFoundExceptionHandler.class);
		classes.add(BadRequestExceptionHandler.class);
		
		singletons.add(new RestWebApplicationExceptionMapper());
		singletons.add(new UncaughtThrowableExceptionMapper());
	}

	@Override
	public Set<Class<?>> getClasses() {
		// classes.add(MyResource.class);
		return classes;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
