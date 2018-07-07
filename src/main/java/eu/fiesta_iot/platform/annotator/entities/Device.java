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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import eu.fiesta_iot.utils.semantics.vocabulary.Geo;
import eu.fiesta_iot.utils.semantics.vocabulary.IotLite;
import eu.fiesta_iot.utils.semantics.vocabulary.Ssn;

//@XmlRootElement(name = "device")
//@XmlAccessorType(XmlAccessType.NONE)
public class Device extends SensingDevice {

	private Location location;
	
	private boolean mobile;

	// @XmlList
	@XmlElementWrapper(name = "sensing_devices")
	@XmlElement(name = "sensing_device")
	@JsonProperty("sensing_devices")
	@JsonInclude(Include.NON_NULL)
	private List<SensingDevice> sensingDevices = new ArrayList<SensingDevice>(0);

	protected Device() {
	}

	public Device(String id, Location location,
	        List<SensingDevice> subDevices) {
		setId(id);
		setLocation(location);
		this.sensingDevices = subDevices;
	}

	public Device(String id, Location location, String quantityKind,
	        String unitOfMeasurement) {
		super(id, quantityKind, unitOfMeasurement);
		setLocation(location);
	}

	@XmlElement(name = "location", required = true, nillable=false)
	@JsonProperty("location")
	@JsonInclude(Include.ALWAYS)
	public Location getLocation() {
		return location;
	}

	// TODO: Make a copy of the location object
	private void setLocation(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Device location cannot be null");
		}
		this.location = location;
		this.location.setId(id);
	}

	@XmlElement(name = "mobile", required = false, nillable=false, defaultValue = "false")
	@JsonProperty("mobile")
	@JsonInclude(Include.ALWAYS)
	public boolean isMobile() {
		return mobile;
	}

	// TODO: Make a copy of the location object
	private void setMobile(boolean mobile) {
		this.mobile = mobile;
	}
	
	@Override
	public Resource asResource() {
		if (id == null) {
			throw new IllegalArgumentException("Device identifier cannot be null");
		}
		
		if (quantityKind != null && unitOfMeasurement != null
		    && sensingDevices.size() != 0) {
			throw new IllegalArgumentException("A device cannot have both sensing devices and quantity kind and uom. Use either sensing devices or quantity kind and uom.");
		}

		Resource device = null;
		if (quantityKind != null && unitOfMeasurement != null) {
			device = asResourceWithoutSensingDevices();
		} else if (sensingDevices.size() != 0) {
			device = asResourceWithSensingDevices();
		} else {
			throw new IllegalArgumentException("Either sensing devices and quantity kind and uom must be included in device description.");
		}

		// Add platform / location
		return addPlatform(device);
	}

	private Resource asResourceWithoutSensingDevices() {
		return super.asResource();
	}

	private Resource asResourceWithSensingDevices() {
		Model model = ModelFactory.createDefaultModel();

		Resource device =
		        model.createResource(id).addProperty(RDF.type, Ssn.Device);

		for (SensingDevice sd : sensingDevices) {
			Resource r = sd.asResource();
			device.addProperty(Ssn.hasSubSystem, r);
			r.addProperty(IotLite.isSubSystemOf, device);
			model.add(r.getModel());
		}

		return device;
	}

	private Resource addPlatform(Resource device) {
		if (location == null) {
			throw new IllegalArgumentException("Device location cannot be null");
		}
		
		Model model = device.getModel();

		Resource platform = model.createResource(id + ".platform")
		        .addProperty(RDF.type, Ssn.Platform)
				.addLiteral(IotLite.isMobile, isMobile());

		Resource location = this.location.asResource();
		platform.addProperty(Geo.location, location);
		model.add(location.getModel());

		device.addProperty(Ssn.onPlatform, platform);
		platform.addProperty(Ssn.attachedSystem, device);

		return device;
	}
}
