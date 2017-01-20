package at.ffesternberg.homepage.sybos2ical;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import at.ffesternberg.sybos.SybosEventClient;
import at.ffesternberg.sybos.entity.Event;
import at.itshorty.util.AppProperties;
import at.itshorty.util.PlaceholderStringFormater;

public class Sybos2Ical {
	private static SybosEventClient client;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AppProperties.initialize("sybos2ical");
		String baseUrl = AppProperties.getInstance().getProperty(
				"sybos.baseurl");
		String token = AppProperties.getInstance().getProperty("sybos.token");
		if (baseUrl == null || token == null || baseUrl.length() == 0
				|| token.length() == 0) {
			throw new RuntimeException(
					"Illegal configuration exception! Check your sybos2ical.properties!");
		}
		boolean loadFuture=Boolean.parseBoolean(AppProperties.getInstance().getProperty("sybos.loadfuture", "true"));
		boolean loadPast=Boolean.parseBoolean(AppProperties.getInstance().getProperty("sybos.loadpast", "false"));
		client = new SybosEventClient(baseUrl, token);
		client.setLoadPast(loadPast);
		client.setLoadFuture(loadFuture);
		try {
			Calendar calendar = new Calendar();
			calendar
					.getProperties()
					.add(
							new ProdId(
									"-//itshorty.at for ff-esternberg.at//sybos2ical 0.1//DE"));
			calendar.getProperties().add(Version.VERSION_2_0);
			calendar.getProperties().add(CalScale.GREGORIAN);
			PlaceholderStringFormater psf = new PlaceholderStringFormater();
			String template = AppProperties.getInstance().getProperty(
					"ical.format");
			for (Event ev : client.loadEntites()) {
				HashMap<String, String> values = new HashMap<String, String>();
				values.put("beschreibung", ev.getBeschreibung());
				values.put("inhalt", ev.getInhalt());
				values.put("referat", ev.getReferat());
				values.put("bezeichnung1", ev.getBezeichnung1());
				values.put("kosten", ev.getKosten());
				values.put("voraussetzung", ev.getVoraussetzung());
				values.put("abteilung", ev.getAbteilung());
				values.put("ort", ev.getOrt());

				VEvent event = new VEvent(new DateTime(ev.getFrom()),
						new DateTime(ev.getTo()), ev.getTitel());
				Uid uid = new Uid();
				uid.setValue(ev.getId() + "@sybos2ical");
				event.getProperties().add(uid);
				event.getProperties().add(new Location(ev.getOrt()));
				event.getProperties().add( new Description(psf.replace(template, values)));
				calendar.getComponents().add(event);
			}
			CalendarOutputter outputter = new CalendarOutputter();
			try {
				outputter.output(calendar, System.out);
			} catch (ValidationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
