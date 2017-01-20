package at.ffesternberg.sybos;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import at.ffesternberg.sybos.entity.SybosEntity;

public abstract class SybosClient<T extends SybosEntity> {
	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";
	private static final Logger log = Logger.getLogger(SybosClient.class);
	
	private String baseUrl;
	private String token;
	private int count = 60;
	private String order = ORDER_ASC;
	private Map<String, String> args = new HashMap<String, String>();

	public SybosClient(String baseUrl, String token) {
		if (!baseUrl.endsWith("/")) {
			baseUrl += "/";
		}
		this.baseUrl = baseUrl;
		this.token = token;
	}

	public List<T> loadEntites() throws IOException, ParseException {
		Document d = loadXML();
		List<T> list = new LinkedList<T>();
		Element root = d.getDocumentElement();
		NodeList items = root.getElementsByTagName("item");
		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			list.add(processEntity(item));
		}
		return list;
	}

	protected Document loadXML() throws IOException {
		URL url = null;
		try {
			url = new URL(baseUrl + getServerResource() + "?token=" + token
					+ getArguments());
			log.debug("request: "+url.toExternalForm());
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document d = db.parse(url.openStream());
			return d;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			log.error("Malformed XML File: "+url);
			throw new IOException("Malformed XML file! "+url , e);
		}
	}

	private String getArguments() {
		StringBuilder sb = new StringBuilder();
		sb.append("&a=").append(getCount());
		sb.append("&o=").append(getOrder());
		if (args != null) {
			for (Entry<String, String> argument : args.entrySet()) {
				sb.append("&").append(argument.getKey()).append("=").append(
						argument.getValue());
			}
		}
		return sb.toString();
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		if (count > 0)
			this.count = count;
		else
			count = 1;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		if (order == ORDER_ASC || order == ORDER_DESC)
			this.order = order;
		else
			this.order = ORDER_ASC;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}

	protected abstract String getServerResource();

	protected abstract T processEntity(Element el) throws ParseException;
}
