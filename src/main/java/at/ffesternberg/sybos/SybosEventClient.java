package at.ffesternberg.sybos;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.ffesternberg.sybos.entity.Event;

public class SybosEventClient extends SybosClient<Event> {
	private boolean loadPast=false;
	private boolean loadFuture=true;
	
	public SybosEventClient(String baseUrl, String token) {
		super(baseUrl, token);
	}

	@Override
	protected String getServerResource() {
		return "xmlVeranstaltung.php";
	}

	@Override
	protected Event processEntity(Element el) throws ParseException{
		Event ev = new Event();
		ev.setId(Integer.parseInt(getTextContent(el, "id")));
		String von = getTextContent(el, "von")+" "+getTextContent(el, "vont");
		String bis = getTextContent(el, "bis")+" "+getTextContent(el, "bist");
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		ev.setFrom(sdf.parse(von));
		ev.setTo(sdf.parse(bis));
		ev.setReferat(getTextContent(el, "referat"));
		ev.setBezeichnung1(getTextContent(el, "bezeichnung1"));
		ev.setBezeichnung2(getTextContent(el, "bezeichnung2"));
		ev.setOrt(getTextContent(el, "ort"));
		ev.setInhalt(getTextContent(el, "inhalt"));
		ev.setVoraussetzung(getTextContent(el, "voraussetzung"));
		ev.setKosten(getTextContent(el, "kosten"));
		ev.setAbteilung(getTextContent(el, "abteilung"));
		ev.setTitel(getTextContent(el, "veroeffentltitel"));
		ev.setBeschreibung(getTextContent(el, "veroeffentltxt"));
		return ev;
	}
	private String getTextContent(Element el, String tag) throws ParseException{
		NodeList items = el.getElementsByTagName(tag);
		if(items.getLength()>=1){
			return items.item(0).getTextContent();
		}else{
			throw new ParseException("Element "+tag+" not found!", -1);
		}
	}
	
	@Override
	public List<Event> loadEntites() throws IOException, ParseException {
		HashSet<Event> evSet=new HashSet<Event>();
		if(isLoadFuture()){
			getArgs().put("z", "future");
			evSet.addAll(super.loadEntites());
		}
		if(isLoadPast()){
			getArgs().put("z", "past");
			evSet.addAll(super.loadEntites());
		}
		List<Event> evList=new LinkedList<Event>();
		evList.addAll(evSet);
		Collections.sort(evList);
		return evList;
	}
	
	public boolean isLoadPast() {
		return loadPast;
	}

	public void setLoadPast(boolean loadPast) {
		this.loadPast = loadPast;
	}

	public boolean isLoadFuture() {
		return loadFuture;
	}

	public void setLoadFuture(boolean loadFuture) {
		this.loadFuture = loadFuture;
	}
	
}
