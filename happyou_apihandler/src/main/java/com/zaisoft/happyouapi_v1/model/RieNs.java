package com.zaisoft.happyouapi_v1.model;

import java.util.Date;

/**
 * Pojo model class of Article-namespace relationships. equivalent to RieNS class in cake PHP<br>
 * 
 * @author shogo
 * 
 */

public class RieNs {
	// id in server
	public long id;
	// article'id
	public long rieid;
	// nsid(=namespacetitlehash)
	public int ns;

	// nstitle
	public String nstitle;

	// news value
	public int val;
	// date of article
	public Date date;
}
