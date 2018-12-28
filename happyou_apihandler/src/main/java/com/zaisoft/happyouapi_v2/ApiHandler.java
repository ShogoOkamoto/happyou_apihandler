package com.zaisoft.happyouapi_v2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.arnx.jsonic.JSON;

import com.zaisoft.happyouapi_v2.model.Articles;
import com.zaisoft.happyouapi_v2.model.RieNs;

/**
 * login happyou api with password, send query, get results.
 * 
 * @author shogo
 *
 */
public class ApiHandler {

	/**
	 * path to json response
	 */
	public static final String PATH2API_JSON = "https://happyou.info/webapp2/api2/index.json";

	/**
	 * path to rss response
	 */
	public static final String PATH2API_RSS = "https://happyou.info/webapp2/api2/index.rss";

	public String app_id;

	/**
	 * 
	 * @param app_id
	 *            application id
	 */
	public ApiHandler(String app_id) {
		this.app_id = app_id;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			String app_id = args[0];

			ApiHandler s = new ApiHandler(app_id);

			List<String> query = new ArrayList<>();
			query.add("pubdatelast=100");
			query.add("count=10");

			// get articles by 'get'
			Articles articles = s.listArticlesByGet(query);

			articles.articles.forEach(ar -> {
				System.out.println(ar.Article.frozenDate + " " + ar.Article.title);
				System.out.println("\ttag:" + ar.Article.tagline);
				if (ar.ns != null) {
					for (RieNs riens : ar.ns) {
						System.out.println("\tns:" + riens.nstitle);
					}
				}
			});

			System.out.println("errid:" + articles.errorid);
			System.out.println("errmes:" + articles.errormes);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		System.exit(0);
	}

	/**
	 * Send query to retrive articles
	 * 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Articles listArticlesByPost(List<String> query) throws Exception {

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
		return listArticlesByGet(createQuery2str(query, false));
	}
	/**
	 * Send query to retrive articles
	 *
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public Articles listArticlesByGet(String query) throws Exception {

		String url = PATH2API_JSON + "?" + query;

		// download
		String jsonResult = sendGetQuery(url);

		// convert jsonresult to articles;
		Object json = JSON.decode(jsonResult, Articles.class);

		Articles articles = (Articles) json;

		return articles;
	}
	/**
	 * send query and get json result.
	 * 
	 * @param urlstr
	 * @return
	 * @throws Exception
	 */
	public String sendGetQuery(String urlstr) throws Exception {

		URL url = new URL(urlstr);

		for (int i = 0; i < 4; i++) {

			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

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
				return null;
			}

			Thread.sleep(1000);

		}

		return null;
	}

	/**
	 * send query and get json result.
	 * 
	 * @param query
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

		sb.append("app_id=" + this.app_id + "&");

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
