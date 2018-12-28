package com.zaisoft.happyouapi_v1.model;

import java.util.List;

/**
 * cakePHPのcakehappyouappのArticleクラスに相当。<br>
 * happyou.info上のDBのテーブル。articleのpojo。<br>
 * 主にcakehappyouappのAPIアクセスの際,JSONからの変換のために用いる。<br>
 * (Java)com.zaisoft.cakehappyouapp.model.Article<br>
 * (CakePHP)cakehappyouappのmodel Article<br>
 * (MySQL) articlesテーブル<br>
 * 
 * 
 * @author shogo
 * 
 */
@Deprecated
public class Articles {

	public List<Article> articles;
}
