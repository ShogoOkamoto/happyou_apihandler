package com.zaisoft.happyouapi_v2.model;

import java.util.List;

import com.zaisoft.happyouapi_v2.model.Article;

/**
 * This class is pojo model class. and root of json document
 * 
 * You can instantiate this object from query result of json. <br>
 * (Java)com.zaisoft.happyouapi_v2.model<br>
 * (CakePHP)cakehappyouapp2model Article<br>
 * (RDBMS) articles table in server<br>
 * 
 * @author shogo
 * 
 */

public class Articles {

	public List<Article> articles;
	
	public String errorid;
	public String errormes;
	
	
}
