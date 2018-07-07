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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.fiesta_iot.utils.semantics.vocabulary.Geo;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "location")
@XmlAccessorType(XmlAccessType.NONE)
public class Location extends Annotatable {
	private String id;

	@XmlElement(name = "lat", required=true)
	@JsonProperty("lat")
	double latitude;
	@XmlElement(name = "lon", required=true)
	@JsonProperty("lon")
	double longitude;

	private Location() {

	}

	public Location(float longitude, float latitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@XmlElement(name = "id")
	@JsonInclude(Include.NON_NULL)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public Resource asResource() {
		Model model = ModelFactory.createDefaultModel();

		Resource location = (id == null) ? model.createResource()
		        : model.createResource(id + ".location");

		location.addProperty(RDF.type, Geo.Point)
		        .addLiteral(Geo.latitude, this.latitude)
		        .addLiteral(Geo.longitude, this.longitude);

		return location;
	}
}
