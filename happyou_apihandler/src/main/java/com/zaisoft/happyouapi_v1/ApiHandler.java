package com.zaisoft.happyouapi_v1;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.arnx.jsonic.JSON;

import com.zaisoft.happyouapi_v1.model.Articles;
import com.zaisoft.happyouapi_v1.model.RieNs;

/**
 * login happyou api with password, send query, get results.
 * 
 * @author shogo
 *
 */
@Deprecated
public class ApiHandler {

	/**
	 * path to login dialog
	 */
	public static final String PATH2LOGIN = "https://happyou.info/webappv1/authake/user/login";

	/**
	 * toppage of api
	 */
	public static final String PATH2API_TOPPAGE = "https://happyou.info/webappv1/";

	/**
	 * path to json response
	 */
	public static final String PATH2API_JSON = "https://happyou.info/webappv1/api/index.json";

	/**
	 * path to rss response
	 */
	public static final String PATH2API_RSS = "https://happyou.info/webappv1/api/index.rss";

	public LoginHandler loginHandler;

	/**
	 * 
	 * @param user
	 *            username to login
	 * @param pass
	 *            password to login
	 */
	public ApiHandler(String user, String pass) {
		this.loginHandler = new LoginHandler(user, pass);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			String username = args[0];
			String password = args[1];

			ApiHandler s = new ApiHandler(username, password);

			// First, you have to login with SSL
			s.login();

			// After login, you can access protected area, api and so on.
			List<String> query = new ArrayList<>();
			query.add("pubdatelast=100");
			query.add("count=10");

			// get articles by 'get'
			Articles articles = s.listArticlesByGet(query);

			// Or get articles by 'post' whatever...
			// Articles articles = s.listArticlesByPost(query);

			articles.articles.forEach(ar -> {
				System.out.println(ar.Article.frozenDate + " " + ar.Article.title);
				System.out.println("\ttag:" + ar.Article.tagline);
				if (ar.ns != null) {
					for (RieNs riens : ar.ns) {
						System.out.println("\tns:" + riens.nstitle);
					}
				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.exit(0);
	}

	public void login() throws Exception {
		this.loginHandler.login();
	}

	/**
	 * Send query to retrive articles
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Articles listArticlesByPost(List<String> query) throws Exception {

		if (loginHandler.getRetrievedCookie() == null) {
			throw new NullPointerException("You have to login() first.");
		}

		String jsonResult = this.sendPostQuery(query);

		// convert jsonresult to articles;
		Articles articles = (Articles) JSON.decode(jsonResult, Articles.class);

		return articles;
	}

	/**
	 * Send query to retrive articles
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Articles listArticlesByGet(List<String> query) throws Exception {

		if (loginHandler.getRetrievedCookie() == null) {
			throw new NullPointerException("You have to login() first.");
		}

		String url = PATH2API_JSON + "?" + createQuery2str(query, false);

		// download
		String jsonResult = sendGetQuery(url);

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
	public String sendGetQuery(String urlstr) throws Exception {

		URL url = new URL(urlstr);

		for (int i = 0; i < 4; i++) {

			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			if (loginHandler.getRetrievedCookie() != null) {
				// make them to one line...
				StringBuffer sb = new StringBuffer();
				for (String c : loginHandler.getRetrievedCookie()) {
					if (sb.length() > 0) {
						sb.append("; ");
					}
					sb.append(c);
				}
				String cookieLine = sb.toString();

				// remove after";"
				cookieLine = cookieLine.split(";", 2)[0];

				connection.addRequestProperty("Cookie", cookieLine);
			}

			connection.connect();

			int responseCode = connection.getResponseCode();

			if (responseCode == 200) {
				// read body...
				StringBuffer buffer = new StringBuffer();
				String sep = System.getProperty("line.separator");
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				reader.lines().forEach(l -> buffer.append(l + sep));
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

	/**
	 * send query after login, and get json result.
	 * 
	 * @param url
	 * @param cookie
	 * @return
	 * @throws Exception
	 */
	public String sendPostQuery(List<String> query) throws Exception {

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

			if (loginHandler.getRetrievedCookie() != null) {
				// make them to one line...
				StringBuffer sb = new StringBuffer();
				for (String c : loginHandler.getRetrievedCookie()) {
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
			if (responseCode == 200) {
				// read body...
				StringBuffer buffer = new StringBuffer();
				String sep = System.getProperty("line.separator");
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				reader.lines().forEach(l -> buffer.append(l + sep));
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

	/**
	 * create query from parameter list
	 * 
	 * @param data
	 * @param encodeManually
	 * @return
	 */
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

}
