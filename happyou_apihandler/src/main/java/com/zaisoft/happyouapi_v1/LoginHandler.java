package com.zaisoft.happyouapi_v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

/**
 * Handle login in happyou API
 * 
 * @author shogo
 *
 */
public class LoginHandler {

	/**
	 * username to login
	 */
	public String username;
	/**
	 * password to login
	 */
	public String password;

	/**
	 * retrieved cookie after login
	 */
	private List<String> retrievedCookie;

	public LoginHandler(String user, String pass) {

		this.username = user;
		this.password = pass;
	}

	/**
	 * login by submitting form!
	 * 
	 * @param parsedContents
	 * @param retrieved
	 *            cookies
	 * @throws Exception
	 */
	public void login() throws Exception {

		if (this.getRetrievedCookie() != null) {
			return;// already logged in;
		}

		//
		// First, retrieve login form contents and build query.
		//
		Map<String, Object> parsedContents = this.parseLoginForm();

		// get parserd contents...
		String actionUrl = (String) parsedContents.get("action");
		String query = (String) parsedContents.get("query");
		List<String> cookie = (List<String>) parsedContents.get("cookie");
		setRetrievedCookie(cookie); // save cookie

		//
		// Second, post form contents with cookie with some redirects
		//
		URL targeturl = new URL(new URL(ApiHandler.PATH2LOGIN), actionUrl);

		HttpsURLConnection connection = null;
		for (int i = 0; i < 4; i++) { // it will repeat redirects for a couple of times.

			// send query.
			connection = (HttpsURLConnection) targeturl.openConnection();
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(false);// To embed cookie in header, you cannot redirect automatically.
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			connection.setRequestProperty("Accept-Language", "ja,en-US;q=0.8,en;q=0.6");
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", query.length() + "");
			connection.setRequestProperty("Cache-Control", "max-age=0");
			connection.setRequestProperty("Origin", "https://happyou.info");
			connection.setRequestProperty("Referer", "https://happyou.info/webappv1/authake/user/login");

			if (cookie.size() > 0) {
				for (String c : cookie) {
					c = c.split(";", 2)[0];
					connection.addRequestProperty("Cookie", c);
				}
			}

			if (i == 0) { // send query in first loop
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
				out.write(query);
				out.flush();
				out.close();
			}

			//
			// Third, read response after submitting a form
			//
			int responseCode = connection.getResponseCode();
			String responseMes = connection.getResponseMessage();
			Map<String, List<String>> headers = connection.getHeaderFields();
			// read headers
			for (String key : headers.keySet()) {
				List<String> lines = headers.get(key);
				if (key != null) {

					if (key.equalsIgnoreCase("Location")) {
						targeturl = new URL(headers.get("Location").get(0));
					}
				}
			}
			if (responseCode == 200) {
				if (targeturl.toExternalForm().equals(ApiHandler.PATH2API_TOPPAGE)) {
					return;
				} else {
					// one more loop
				}
			} else if (responseCode == 302) {
				// one more loop
			} else {
				break;
			}

			Thread.sleep(100);
		}

		throw new IOException("Login failed");

	}

	/**
	 * download page simply, and retrieve cookie
	 * 
	 * @param urlstr
	 * @param cookies
	 * @return
	 * @throws IOException
	 */
	private String downloadPage(String urlstr, List<String> cookies) throws IOException {

		URL url = new URL(urlstr);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

		if (cookies != null) {
			Map<String, List<String>> header = connection.getHeaderFields();
			for (String key : header.keySet()) {
				if (key != null && key.compareToIgnoreCase("set-cookie") == 0) {
					cookies.addAll(header.get(key));
					break;
				}
			}
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuffer sb = new StringBuffer();
		String str = null;
		while ((str = in.readLine()) != null) {
			sb.append(str);
		}
		in.close();

		return sb.toString();
	}

	/**
	 * parse loginform and create POST query
	 * 
	 * @param url2loginform
	 * @param parsedContents
	 * @throws Exception
	 */
	private Map<String, Object> parseLoginForm() throws Exception {

		List<String> cookies = new ArrayList<>();

		//
		// retrieve form web page...
		String formpageContents = downloadPage(ApiHandler.PATH2LOGIN, cookies);

		// parse HTML and find login form
		Source s = new Source(formpageContents);
		Element loginFormElement = findLoginForm(s.getChildElements());

		if (loginFormElement == null) {
			throw new Exception("Failed to find loginform in webpagecontents");
		}

		// build query from form element
		StringBuffer sb = new StringBuffer();

		// attribute... of form element...
		String action = loginFormElement.getAttributes().getValue("action");

		// inspect <input type...> within form element
		for (Element e : loginFormElement.getChildElements()) {
			buildFormQuery(sb, e);
		}

		String q = sb.toString();

		Map<String, Object> parsedContents = new HashMap<>();
		parsedContents.put("query", q);
		parsedContents.put("action", action);
		parsedContents.put("cookie", cookies);

		return parsedContents;
	}

	private void buildFormQuery(StringBuffer sb, Element loginFormElement) throws Exception {

		// children..
		for (Element child : loginFormElement.getChildElements()) {
			buildFormQuery(sb, child);
		}

		// this element itself.
		if (!loginFormElement.getStartTag().getName().equalsIgnoreCase("input")) {
			return;
		}
		// <INPUT TYPE=xxx name=xxx value=xxx id=xxx>
		Attributes atts = loginFormElement.getAttributes();

		String targetName = null;
		String targetValue = "";

		for (int i = 0, l = atts.getCount(); i < l; i++) {
			Attribute att = atts.get(i);
			String key = att.getKey();
			if (key.equalsIgnoreCase("name")) {
				targetName = att.getValue();
			} else if (key.equals("value")) {
				targetValue = att.getValue();
			}
		}
		// targetName = targetValue
		if (targetName != null) {

			// fillout form!
			if (targetName.equalsIgnoreCase("data[User][login]")) {
				targetValue = this.username;
			} else if (targetName.equalsIgnoreCase("data[User][password]")) {
				targetValue = this.password;
			}

			if (sb.length() > 0) {
				sb.append("&");
			}

			sb.append(URLEncoder.encode(trim(targetName), "utf-8"));
			sb.append("=");
			sb.append(URLEncoder.encode(trim(targetValue), "utf-8"));

		}
	}

	/**
	 * remove some character from head and tail. "hoge"->hoge
	 * 
	 * @param s
	 * @return
	 */
	private String trim(String s) {

		if (s == null) {
			return null;
		}
		if (s.length() < 2) {
			return s;
		}
		if (s.startsWith("\"") && s.endsWith("\"")) {
			s = s.substring(1, s.length() - 1);
		}
		if (s.startsWith("'") && s.endsWith("'")) {
			s = s.substring(1, s.length() - 1);
		}
		return s;
	}

	/**
	 * find form element from parsed web page contents;
	 * 
	 * @param elements
	 * @return
	 */
	private Element findLoginForm(List<Element> elements) {

		for (Element e : elements) {

			// is this form element?
			if (e.getStartTag().getName().equalsIgnoreCase("FORM")) {
				// This is form. But is this really login form?
				String formId = e.getAttributeValue("id");
				if (formId != null && formId.equals("UserLoginForm")) {
					// yes. it is.
					return e;
				}
			}

			// scan recursive...
			if (e.getChildElements() != null) {
				Element formElment = findLoginForm(e.getChildElements());
				if (formElment != null) {
					return formElment;
				}
			}
		}
		return null;
	}

	/**
	 * @return the retrievedCookie
	 */
	public List<String> getRetrievedCookie() {
		return retrievedCookie;
	}

	/**
	 * @param retrievedCookie
	 *            the retrievedCookie to set
	 */
	public void setRetrievedCookie(List<String> retrievedCookie) {
		this.retrievedCookie = retrievedCookie;
	}

}
