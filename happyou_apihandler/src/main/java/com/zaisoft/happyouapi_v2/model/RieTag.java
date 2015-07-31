package com.zaisoft.happyouapi_v2.model;

/**
 * model class represents article-tag relationships.
 * 
 * @author shogo
 * 
 */
public class RieTag {

	// id of this object
	public long id;

	// id of article
	public long rieid;

	// hashcode of tag. Refer ArticleProp#tagline to actual tag name as string
	public int taghash;

}