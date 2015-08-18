package com.zaisoft.happyouapi_v2.model;

import java.util.List;

/**
 * This class is pojo model class and equivalent to the cakehappyouapp.model.Article in cakePHP.<br>
 * 
 * You can instantiate this object from query result of json. <br>
 * (Java)com.zaisoft.happyouapi_v2.model<br>
 * (CakePHP)cakehappyouapp2model Article<br>
 * (RDBMS) articles table in server<br>
 * 
 * @author shogo
 * 
 */
public class Article {

	/**
	 * properties of Article. It have to be cappitalized to support Jsonic.
	 */
	public ArticleProps Article;

	/**
	 * represents article-tag relationships
	 */
	public List<RieTag> tag;

	/**
	 * represents article-newssite relationships
	 */
	public List<RieNs> ns;

	private transient String stringexpression;

	public transient boolean deletable;
	
	public String toString() {

		if (stringexpression != null) {
			return stringexpression;
		}
		StringBuilder b = new StringBuilder();
		b.append(Article.frozenDate);
		b.append(" ");
		b.append(Article.orgname);
		b.append("/");
		b.append(Article.title);
		b.append(" [");
		b.append(Article.tagline);
		b.append("] ");
		b.append(Article.linkedAbsUrlList);

		this.stringexpression = b.toString();

		return this.stringexpression;

	}

}
