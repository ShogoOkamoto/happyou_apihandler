package com.zaisoft.happyouapi_v1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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

import net.arnx.jsonic.JSON;
import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import com.zaisoft.happyouapi_v1.model.Articles;

/**
 * login happyou api with password, send query, get results.
 * 
 * @author shogo
 *
 */
public class ApiHandler {

	public static final String PATH2LOGIN = "https://zaisoft.sakura.ne.jp/happyou/webappv1/authake/user/login";
	public static final String PATH2API_TOPPAGE = "https://zaisoft.sakura.ne.jp/happyou/webappv1/index.php";
	public static final String PATH2API_JSON = "https://zaisoft.sakura.ne.jp/happyou/webappv1/api/index.json";
	public static final String PATH2API_RSS = "https://zaisoft.sakura.ne.jp/happyou/webappv1/api/index.rss";

	/*
	 * url contains form to login
	 */
	private String username;
	private String password;

	/**
	 * retrieved cookie after login
	 */
	private List<String> retrievedCookie;

	/**
	 * 
	 * @param user
	 *            username to login
	 * @param pass
	 *            password to login
	 */
	public ApiHandler(String user, String pass) {

		this.username = user;
		this.password = pass;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			String username = args[0];
			String password = args[1];

			ApiHandler s = new ApiHandler(username, password);

			// login with SSL
			s.login();

			// After login, you can access protected area, api and so on.
			List<String> query = new ArrayList<>();
			query.add("pubdatelast=256");
			Articles articles = s.listArticles(query);

		
			articles.articles.forEach(ar -> {
				System.out.println(ar.Article.title);
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Send query to retrive articles
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Articles listArticles(List<String> query) throws Exception {

		if (this.getRetrievedCookie() == null) {
			throw new NullPointerException("You have to login() first.");
		}

		String jsonResult = this.sendQuery(query);

		// convert jsonresult to articles;
		Articles articles = (Articles) JSON.decode(jsonResult, Articles.class);

		return articles;
	}

	/**
	 * send query after login, and get json result.
	 * 
	 * @param url
	 * @param cookie
	 * @return
	 * @throws Exception
	 */
	public String sendQuery(List<String> query) throws Exception {

		URL url = new URL(PATH2API_JSON);

		for (int i = 0; i < 4; i++) {

			// build query.
			byte[] queryBytes = createQuery2str(query, false).getBytes("utf-8");

			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestProperty("Connection", "keep-alive");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(queryBytes.length));

			if (this.getRetrievedCookie() != null) {
				// make them to one line...
				StringBuffer sb = new StringBuffer();
				for (String c : this.getRetrievedCookie()) {
					if (sb.length() > 0) {
						sb.append("; ");
					}
					sb.append(c);
				}
				String cookieLine = sb.toString();

				cookieLine = cookieLine.split(";", 2)[0];

				connection.addRequestProperty("Cookie", cookieLine);
			}

			// post query..
			DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
			dos.write(createQuery2str(query, false).getBytes("utf-8"));
			dos.flush();
			dos.close();

			int responseCode = connection.getResponseCode();
			String responseMes = connection.getResponseMessage();

			if (responseCode == 200) {
				// read body...
				StringBuffer buffer = new StringBuffer();
				String sep = System.getProperty("line.separator");
				String line = null;
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					buffer.append(line);
					buffer.append(sep);
				}

				reader.close();

				String result = buffer.toString();
				return result;
			} else if (responseCode == 302) {
				// redirect to somewhere
				String redirectto = connection.getHeaderField("Location");
				if (redirectto != null) {
					url = new URL(redirectto);
				} else {
					break;
				}

			} else {
				String line = null;
				StringBuffer buffer = new StringBuffer();
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				reader.lines().forEach(l -> buffer.append(l));
				System.out.println(buffer.toString());
				return null;
			}

			Thread.sleep(1000);

		}

		return null;
	}

	private String createQuery2str(List<String> data, boolean encodeManually) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i) != null) {
				String s = data.get(i).toString();

				if (!encodeManually) {
					int sp = s.indexOf("=");
					if (sp != -1) {
						String key = s.substring(0, sp);
						String value = s.substring(sp + 1);
						try {
							sb.append(URLEncoder.encode(key, "utf-8"));
							sb.append("=");
							sb.append(URLEncoder.encode(value, "utf-8"));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else {
					sb.append(s);
				}

				if (i < data.size() - 1) {
					sb.append("&");
				}
			}
		}

		String str = sb.toString();
		return str;
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
		URL targeturl = new URL(new URL(PATH2LOGIN), actionUrl);

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
			connection.setRequestProperty("Origin", "https://zaisoft.sakura.ne.jp");
			connection.setRequestProperty("Referer", "https://zaisoft.sakura.ne.jp/happyou/webappv1/authake/user/login");

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
				if (targeturl.toExternalForm().equals(PATH2API_TOPPAGE)) {
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
		String formpageContents = downloadPage(PATH2LOGIN, cookies);

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
	 * "hoge"->hoge
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
	 * parse cookie lines<br>
	 * key=value; key=value; key=value; *
	 * 
	 * @param cookies
	 * @return
	 */
	private Map<String, String> parseCookies(List<String> cookies) {

		Map<String, String> keyvalueMap = new HashMap();

		if (cookies == null) {
			return keyvalueMap;
		}

		for (String line : cookies) {
			String[] keyvalues = line.split("; *"); // key=value; key=value; key=value;
			if (keyvalues != null) {
				for (String keyvaluestr : keyvalues) {
					String[] keyvalue = keyvaluestr.split("="); // key=value
					if (keyvalue.length == 2) {
						String key = keyvalue[0];
						String val = keyvalue[1];
					}
				}
			}
		}
		return keyvalueMap;
	}

	private List<String> getRetrievedCookie() {
		return retrievedCookie;
	}

	private void setRetrievedCookie(List<String> retrievedCookie) {
		this.retrievedCookie = retrievedCookie;
	}

}
