
package com.flight.search.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.FlightOfferSearch;
import com.amadeus.resources.FlightOfferSearch.FareDetailsBySegment;
import com.amadeus.resources.FlightOfferSearch.Itinerary;
import com.amadeus.resources.FlightOfferSearch.SearchSegment;
import com.amadeus.resources.FlightOfferSearch.TravelerPricing;
import com.amadeus.resources.FlightPrice;
import com.flight.search.amadeusconnection.AmadeusConnect;
import com.flight.search.model.Passenger;
import com.flight.search.model.Segment;
import com.flight.search.model.SelectedFlightResponseModel;


@Component	
public class SelectedFlightService {

	
	@Autowired
	AmadeusConnect ama;
	
	public SelectedFlightResponseModel getTravelers(FlightOfferSearch search) throws ResponseException {

		FlightPrice amadeusResponse = ama.confirm(search);
		SelectedFlightResponseModel response = new SelectedFlightResponseModel();
		TravelerPricing[] travelerPricings = amadeusResponse.getFlightOffers()[0].getTravelerPricings();
		Itinerary[] itineraries = amadeusResponse.getFlightOffers()[0].getItineraries();
		getPriceDetails(response, travelerPricings);

		List<Segment> outBoundSegments = getBaggageDetails(travelerPricings, itineraries[0].getSegments());

		List<Segment> inBoundSegments = new ArrayList<Segment>();
		if (itineraries.length > 1) {
			inBoundSegments = getBaggageDetails(travelerPricings, itineraries[1].getSegments());
		}
		response.setInBoundSegments(inBoundSegments);
		response.setOutBoundSegments(outBoundSegments);

		return response;
	}

	// To get pricing details of all passengers
	private void getPriceDetails(SelectedFlightResponseModel response, TravelerPricing[] travelerPricings) {

		for (TravelerPricing pricing : travelerPricings) {
			Passenger passenger = new Passenger();
			passenger.setId(pricing.getTravelerId());
			passenger.setBasePrice(pricing.getPrice().getBase());
			passenger.setTotal(pricing.getPrice().getTotal());
			passenger.setTravelerType(pricing.getTravelerType());

			response.getTravelers().add(passenger);
		}
	}

	// To get baggage details by segment
	private  List<Segment> getBaggageDetails(TravelerPricing[] travelerPricings, SearchSegment[] segments) {

		List<Segment> responseSegments = new ArrayList<Segment>();

		for (SearchSegment segment : segments) {
			Segment item = new Segment();

			item.getDeparture().setIataCode(segment.getDeparture().getIataCode());
			item.getDeparture().setTerminal(segment.getDeparture().getTerminal());
			item.getDeparture().setAt(segment.getDeparture().getAt());

			item.getArrival().setIataCode(segment.getArrival().getIataCode());
			item.getArrival().setTerminal(segment.getArrival().getTerminal());
			item.getArrival().setAt(segment.getArrival().getAt());

			item.setCarrierCode(segment.getCarrierCode());
			item.setDuration(segment.getDuration());
			item.setNumber(segment.getNumber());

			TravelerPricing outboundPrice = travelerPricings[0];
			for (FareDetailsBySegment detail : outboundPrice.getFareDetailsBySegment()) {
				if (detail.getSegmentId().equals(segment.getId())) {
					item.setWeight(detail.getIncludedCheckedBags().getWeight());
					item.setWeightUnit(detail.getIncludedCheckedBags().getWeightUnit());
				}
			}

			responseSegments.add(item);
		}

		return responseSegments;

	}
}
