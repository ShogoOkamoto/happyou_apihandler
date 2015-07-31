package com.zaisoft.happyouapi_v2.model;

import java.util.Date;

/*
 * Model class of Article properites.
 * equivalent to the cakehappyouapp.model.ArticleProp in cakePHP.<br>
 * 
 * @author shogo
 * 
 */
public class ArticleProps {

	/**
	 * id of arritle
	 */
	public long id;
	/**
	 * title 
	 */
	public String title;
	/**
	 * subtitle
	 */
	public String subtitle;
	/**
	 * description
	 */
	public String description;
	/**
	 * published date to server
	 */
	public Date publishedDate;
	/**
	 * found date by crawler
	 */
	public Date foundDate;
	/**
	 * retrieved date of article 
	 */
	public Date frozenDate;
	/**
	 * the place where this article is/was placed
	 */
	public String archivedUrl;
	
	/**
	 * url list of this article has
	 */
	public String linkedAbsUrlList;
	
	/**
	 * tags of this aritlce 
	 */
	public String tagline;
	
	/**
	 * organization names of this website
	 */
	public String orgname;
}
