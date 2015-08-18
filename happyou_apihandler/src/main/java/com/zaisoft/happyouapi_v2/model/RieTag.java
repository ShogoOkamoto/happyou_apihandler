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

	@Override
	public boolean equals(Object o) {

		RieTag t2 = (RieTag) o;
		if (taghash == t2.taghash) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return taghash;
	}

}