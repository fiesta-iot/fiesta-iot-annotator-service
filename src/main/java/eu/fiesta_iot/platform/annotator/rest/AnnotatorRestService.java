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

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.fiesta_iot.platform.annotator.entities.Annotatable;
import eu.fiesta_iot.platform.annotator.entities.DeviceStandalone;
import eu.fiesta_iot.platform.annotator.entities.DevicesStandalone;
import eu.fiesta_iot.platform.annotator.entities.Observation;
import eu.fiesta_iot.platform.annotator.entities.Observations;
import eu.fiesta_iot.platform.annotator.entities.Testbed;
import eu.fiesta_iot.utils.semantics.serializer.ModelSerializer;
import eu.fiesta_iot.utils.semantics.serializer.Serializer;
import eu.fiesta_iot.utils.semantics.serializer.SerializerFactory;
import eu.fiesta_iot.utils.semantics.serializer.exceptions.CannotSerializeException;
import eu.fiesta_iot.utils.semantics.vocabulary.FiestaIoT;

@Path("")
@RequestScoped
public class AnnotatorRestService {

	Logger log = LoggerFactory.getLogger(getClass());

	@POST
	@Path("testbed")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response annotate(Testbed testbed, @Context Request req) {
		return annotate((Annotatable) testbed, req);
	}

	@POST
	@Path("device")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response annotate(DeviceStandalone device, @Context Request req) {
		return annotate((Annotatable) device, req);
	}

	@POST
	@Path("devices")
	@Consumes(MediaType.APPLICATION_XML)
	public Response annotateDevices(DevicesStandalone devices,
	                                @Context Request req) {
		return annotate(devices.getDevices(), req);
	}

	@POST
	@Path("devices")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response annotateDevices(List<DeviceStandalone> devices,
	                                @Context Request req) {
		return annotate(devices, req);
	}

	@POST
	@Path("observation")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response annotateObservation(Observation observation,
	                                    @Context Request req) {
		return annotate((Annotatable) observation, req);
	}

	@POST
	@Path("observations")
	@Consumes(MediaType.APPLICATION_XML)
	public Response annotateObservations(Observations observations,
	                                     @Context Request req) {
		return annotate(observations.getObservations(), req);
	}

	@POST
	// @Path("/{a:observations_list|devices_list}")
	@Path("observations")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response annotateObservations(List<Observation> observations,
	                                     @Context Request req) {
		return annotate(observations, req);
	}

	private Response annotate(Annotatable annotatable, Request req) {
		return annotate(annotatable.toModel(), req);
	}

	private Response annotate(List<? extends Annotatable> annotatables,
	                          Request req) {
		// Annotate each observation and bound to model
		Model model = ModelFactory.createDefaultModel();

		for (Annotatable a : annotatables) {
			model.add(a.toModel());
		}

		// Annotate Model
		return annotate(model, req);
	}

	private Response annotate(Model model, Request req) {
		// Add prefixes to model
		// It's done in order to avoid the problem with PrefixInspector
		// at the validator.
		// From Jena documentation:
		// The PrefixInspector also reports a problem if any prefix looks like
		// an Jena automatically-generated prefix, j.<i>Number</i>. (Jena
		// generates these prefixes when writing RDF/XML if the XML
		// syntactically requires a prefix but the model hasn't defined one.)
		model.setNsPrefixes(FiestaIoT.PREFIX_MAP);

		try {
			Serializer<?> renderer = SerializerFactory.getSerializer(model);
			Variant variant =
			        req.selectVariant(renderer.listAvailableVariants());
			if (variant == null) {
				throw new CannotSerializeException();
			}
			String response = renderer.writeAs(variant.getMediaType());
			return Response.ok(response).build();
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException(ex.getMessage());
		} catch (CannotSerializeException ex) {
			return Response
			        .notAcceptable(ModelSerializer.getAvailableVariants())
			        .build();
		}
	}



	
	
	
	
//	@GET
//	//@Path("/{a:observations_list|devices_list}")
//	@Path("test_list")
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	//public List<B> annotate() {
//	public Response annotate() {
//		B b1 = new B();
//		b1.device = "jorge";
//		B b2 = new B();
//		b2.device = "jorge";
//		
//		Blist list = new Blist();
//		list.list = Arrays.asList(b1, b2);
//	
//		return Response.ok(list).build();
//	}

	
	
	// @GET
	// @Path("observations")
	// @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public Response get() {
	// Location location = new Location((float) 1.2, (float) 1.5);
	// OffsetDateTime timestamp = OffsetDateTime.now();
	//
	// Observation o1 = new Observation("qk1", "value1", "float", "uom",
	// location,
	// timestamp, "device");
	//
	// Observation o2 = new Observation("qk2", "value2", "float", "uom",
	// location,
	// timestamp, "device");
	//
	// Observations o = new Observations();
	// o.observations = new Observation[]{o1, o2};
	//
	// return Response.ok(o, MediaType.APPLICATION_JSON_TYPE).build();
	// }


	// @GET
	// @Path("testbed")
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public Testbed annotateTest() {
	// Testbed t = new DefaultAnnotator().testTestbedSimple();
	// return t;
	// }

	// @GET
	// @Path("observation")
	// @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	// public Observation annotateTestObservation() {
	// Observation t = new DefaultAnnotator().testObservation();
	// return t;
	// }
}