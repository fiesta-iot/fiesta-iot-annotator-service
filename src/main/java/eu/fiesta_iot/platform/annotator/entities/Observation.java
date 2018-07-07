/*******************************************************************************
 * Copyright (c) 2018 Jorge Lanza, 
 *                    David Gomez, 
 *                    Luis Sanchez,
 *                    Juan Ramon Santana
 *
 * For the full copyright and license information, please view the LICENSE
 * file that is distributed with this source code.
 *******************************************************************************/
package eu.fiesta_iot.platform.annotator.entities;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.fasterxml.jackson.annotation.JsonProperty;

import eu.fiesta_iot.utils.semantics.vocabulary.Dul;
import eu.fiesta_iot.utils.semantics.vocabulary.Geo;
import eu.fiesta_iot.utils.semantics.vocabulary.IotLite;
import eu.fiesta_iot.utils.semantics.vocabulary.M3Lite;
import eu.fiesta_iot.utils.semantics.vocabulary.Ssn;
import eu.fiesta_iot.utils.semantics.vocabulary.Time;

@XmlRootElement(name = "observation")
public class Observation extends Annotatable {

	@XmlElement(name = "observed_by", required = true)
	@JsonProperty("observed_by")
	private String device;

	@XmlElement(name = "location", required = true)
	@JsonProperty("location")
	private Location location;

	@XmlElement(name = "quantity_kind", required = true)
	@JsonProperty("quantity_kind")
	private String qk;

	@XmlElement(name = "value", required = true)
	@JsonProperty("value")
	private String value;

	@XmlElement(name = "format")
	@JsonProperty("format")
	private String type;

	@XmlElement(name = "unit", required = true)
	@JsonProperty("unit")
	private String uom;

	@XmlElement(name = "timestamp", required = true)
	@XmlJavaTypeAdapter(JaxBOffsetDateTimeAdapter.class)
	@JsonProperty("timestamp")
	private OffsetDateTime timestamp;

	private Observation() {
	}

	protected Observation(String qk, String value, String format, String uom,
	        Location location, OffsetDateTime timestamp, String device) {
		this.qk = qk;
		this.value = value;
		this.type = format;
		this.uom = uom;
		this.location = location;
		this.timestamp = timestamp;
		this.device = device;
	}

	@Override
	public Resource asResource() {
		if (this.device == null || this.qk == null || this.uom == null
		    || this.value == null || this.timestamp == null
		    || this.location == null) {
			throw new IllegalArgumentException("An observation must have "
			                                   + "a value, a bound "
			                                   + "device, quantity kind, "
			                                   + "unit of measurement, "
			                                   + "location and timestamp.");
		}

		Model model = ModelFactory.createDefaultModel();

		Resource qkResource = model.createResource(device + ".quantity." + qk)
		        .addProperty(RDF.type, M3Lite.createClass(qk));
		Resource uomResource = model.createResource(device + ".unit." + uom)
		        .addProperty(RDF.type, M3Lite.createClass(uom));

		Resource locationResource = location.asResource();
		model.add(locationResource.getModel());

		// Its only difference from dateTime is that the time zone expression is
		// required at the end of the value. The letter Z is used to indicate
		// Coordinated Universal Time (UTC). All other time zones are
		// represented by their difference from Coordinated Universal Time in
		// the format +hh:mm, or -hh:mm.
		// 2004-04-12T13:20:00-05:00 1:20 pm on April 12, 2004, US Eastern
		// Standard Time
		// 2004-04-12T13:20:00Z 1:20 pm on April 12, 2004, Coordinated Universal
		// Time (UTC)

		Literal timestampLiteral =
		        model.createTypedLiteral(timestamp
		                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
		                                 XSDDatatype.XSDdateTimeStamp);
		Resource timeResource =
		        model.createResource().addProperty(RDF.type, Time.Instant)
		                .addLiteral(Time.inXSDDateTime, timestampLiteral);

		Literal valueLiteral;
		if (this.type != null) {
			valueLiteral =
			        model.createTypedLiteral(value, new XSDDatatype(type));
		} else {
			valueLiteral = model.createTypedLiteral(value);
		}
		Resource observationValueResource = model.createResource()
		        .addProperty(RDF.type, Ssn.ObservationValue)
		        .addProperty(IotLite.hasUnit, uomResource)
		        .addLiteral(Dul.hasDataValue, valueLiteral);

		Resource observationResultResource =
		        model.createResource().addProperty(RDF.type, Ssn.SensorOutput)
		                .addProperty(Ssn.hasValue, observationValueResource);

		Resource device = model.createResource(this.device);
		Resource observation = model.createResource();
		observation.addProperty(RDF.type, Ssn.Observation)
		        .addProperty(Ssn.observedBy, device)
		        .addProperty(Geo.location, locationResource)
		        .addProperty(Ssn.observationSamplingTime, timeResource)
		        .addProperty(Ssn.observedProperty, qkResource)
		        .addProperty(Ssn.observationResult, observationResultResource);
		device.addProperty(Ssn.madeObservation, observation);

		return observation;
	}

}
