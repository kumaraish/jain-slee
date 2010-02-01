package org.mobicents.slee.resource.sip11;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class Utils {

	private static Set<String> DIALOG_CREATING_METHODS;
	
	/**
	 * 
	 * @return
	 */
	public static Set<String> getDialogCreatingMethods() {
		if (DIALOG_CREATING_METHODS == null) {
			final Set<String> set = new HashSet<String>();
			set.add(Request.INVITE);
			set.add(Request.REFER);
			set.add(Request.SUBSCRIBE);
			DIALOG_CREATING_METHODS = Collections.unmodifiableSet(set);
		}
		return DIALOG_CREATING_METHODS;
	}

	private static Set<String> HEADERS_TO_OMMIT_ON_REQUEST_COPY;
	
	/**
	 * 
	 * @return
	 */
	public static Set<String> getHeadersToOmmitOnRequestCopy() {
		if (HEADERS_TO_OMMIT_ON_REQUEST_COPY == null) {
			final Set<String> set = new HashSet<String>();
			set.add(RouteHeader.NAME);
			set.add(RecordRouteHeader.NAME);
			set.add(ViaHeader.NAME);
			set.add(CallIdHeader.NAME);
			set.add(CSeqHeader.NAME);
			set.add(FromHeader.NAME);
			set.add(ToHeader.NAME);
			set.add(ContentLengthHeader.NAME);
			HEADERS_TO_OMMIT_ON_REQUEST_COPY = Collections.unmodifiableSet(set);
		}
		return HEADERS_TO_OMMIT_ON_REQUEST_COPY;
	}
	
	private static Set<String> HEADERS_TO_OMMIT_ON_RESPONSE_COPY;

	/**
	 * 
	 * @return
	 */
	public static Set<String> getHeadersToOmmitOnResponseCopy() {
		if (HEADERS_TO_OMMIT_ON_RESPONSE_COPY == null) {
			final Set<String> set = new HashSet<String>();
			set.add(RouteHeader.NAME);
			set.add(RecordRouteHeader.NAME);
			set.add(ViaHeader.NAME);
			set.add(CallIdHeader.NAME);
			set.add(CSeqHeader.NAME);
			set.add(ContactHeader.NAME);
			set.add(FromHeader.NAME);
			set.add(ToHeader.NAME);
			set.add(ContentLengthHeader.NAME);
			HEADERS_TO_OMMIT_ON_RESPONSE_COPY = Collections.unmodifiableSet(set);
		}
		return HEADERS_TO_OMMIT_ON_RESPONSE_COPY;
	}
	
	/**
	 * Generates route list the same way dialog does.
	 * @param response
	 * @return
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	public static List<RouteHeader> getRouteList(Response response, HeaderFactory headerFactory) throws ParseException {
		// we have record route set, as we are client, this is reversed
		final ArrayList<RouteHeader> routeList = new ArrayList<RouteHeader>();
		final ListIterator rrLit = response.getHeaders(RecordRouteHeader.NAME);
		while (rrLit.hasNext()) {
			final RecordRouteHeader rrh = (RecordRouteHeader) rrLit.next();
			final RouteHeader rh = headerFactory.createRouteHeader(rrh.getAddress());
			final Iterator pIt = rrh.getParameterNames();
			while (pIt.hasNext()) {
				final String pName = (String) pIt.next();
				rh.setParameter(pName, rrh.getParameter(pName));
			}
			routeList.add(0, rh);
		}
		return routeList;
	}
	
	/**
	 * Forges Request-URI using contact and To name par to address URI, this is
	 * required on dialog fork, this is how target is determined
	 * 
	 * @param response
	 * @return
	 * @throws ParseException 
	 */
	public static URI getRequestUri(Response response, AddressFactory addressFactory) throws ParseException {
		ContactHeader contact = ((ContactHeader) response.getHeader(ContactHeader.NAME));
		if (contact != null) {
			//FIXME: SipUri instanceof check ?
			if(contact.getAddress().getURI().isSipURI())
			{
				SipURI contactURI = (SipURI) contact.getAddress().getURI();
				SipURI requestURI = addressFactory.createSipURI(contactURI.getUser(), contactURI.getHost());
				requestURI.setPort(contactURI.getPort());
				return requestURI;			
			}else
			{
				//it may be tel,fax,generic
				//RFC3261 10.2.1
				URI contactURI = (URI) contact.getAddress().getURI().clone();
				return contactURI;
			}
		} 
		return null;
	}
}
