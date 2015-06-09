package com.zaisoft.happyouapi_v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.zaisoft.happyouapi_v1.model.Articles;

@RunWith(Parameterized.class)
public class ApiHandlerTest {

	String email;
	String password;

	public ApiHandlerTest(String email, String pass) {
		this.email = email;
		this.password = pass;
	}

	@Parameters
	public static List<Object[]> testData() {
		Object[][] data = new String[][] { { "youremail", "yourpassword" } };

		return Arrays.asList(data);
	}

	@Test
	public void testListArticles() {

		ApiHandler h = new ApiHandler(email, password);
		try {
			h.login();
			Articles articles = h.listArticlesByPost(Arrays.asList(new String[] { "pubdatelast=1000" }));
			articles.articles.forEach(ar ->
			{
				System.out.println(ar.Article.title);
			});

			assertEquals(true, articles.articles.size() > 0);

		} catch (Exception ex) {
			ex.printStackTrace();
			fail("failed by some exception");
		}
	}
}
