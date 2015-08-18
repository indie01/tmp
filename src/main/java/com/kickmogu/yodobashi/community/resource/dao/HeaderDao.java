package com.kickmogu.yodobashi.community.resource.dao;




public interface HeaderDao {

	public String getUniversalHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl );

	public String getCommonHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl);

	public String getImportUrl( );

	public String getPath( boolean isSsl );
	
	public void removeUniversalHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl ) throws SecurityException, NoSuchMethodException;

	public void removeCommonHeader( boolean isSsl, boolean isAdult, String returnUrl, String itemSearchUrl) throws SecurityException, NoSuchMethodException;
	
	public void removePath( boolean isSsl ) throws SecurityException, NoSuchMethodException;
	
	public void removeImportUrl() throws SecurityException, NoSuchMethodException;
	
}
