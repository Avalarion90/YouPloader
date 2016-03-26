/* 
 * YouPloader Copyright (c) 2016 genuineparts (itsme@genuineparts.org)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package at.becast.youploader.database;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import at.becast.youploader.gui.FrmMain;
import at.becast.youploader.templates.Template;
import at.becast.youploader.youtube.data.Video;
import at.becast.youploader.youtube.io.UploadManager;
import at.becast.youploader.youtube.io.UploadManager.Status;

public class SQLite {
	
	private static Connection c;
	private static final Logger LOG = LoggerFactory.getLogger(SQLite.class);
	private SQLite( String database ){
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+database);
		} catch ( Exception e ) {
			LOG.error("SQLite", e);
		}
	}
	
    public static Connection getInstance(){
        if(c == null)
            new SQLite(FrmMain.DB_FILE);
        return c;
    }
    

    public static Boolean setup(){
        if(c == null)
            new SQLite(FrmMain.DB_FILE);
        
        PreparedStatement prest = null;
        try {
			prest = c.prepareStatement("CREATE TABLE `settings` (`name` VARCHAR, `value` VARCHAR)");
			prest.executeUpdate();
			prest = c.prepareStatement("INSERT INTO `settings` VALUES('client_id','581650568827-44vbqcoujflbo87hbirjdi6jcj3hlnbu.apps.googleusercontent.com')");
			prest.executeUpdate();
			prest = c.prepareStatement("INSERT INTO `settings` VALUES('clientSecret','l2M4y-lu9uCkSgBdCKp1YAxX')");
			prest.executeUpdate();
			prest = c.prepareStatement("INSERT INTO `settings` VALUES('tos_agreed','0')");
			prest.executeUpdate();
			prest = c.prepareStatement("CREATE TABLE `accounts` (`id` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , `name` VARCHAR NOT NULL , `refresh_token` VARCHAR, `cookie` VARCHAR, `active` INTEGER DEFAULT 0)");
			prest.executeUpdate();
			prest = c.prepareStatement("CREATE TABLE `templates` (`id` INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , `name` VARCHAR, `data` VARCHAR)");
			prest.executeUpdate();
			prest = c.prepareStatement("CREATE TABLE `uploads` (`id` INTEGER PRIMARY KEY  NOT NULL ,`file` VARCHAR,`account` INTEGER DEFAULT (null),`yt_id` VARCHAR, `enddir` VARCHAR ,`url` VARCHAR,`uploaded` INTEGER DEFAULT (null) ,`lenght` INTEGER DEFAULT (null) ,`data` VARCHAR,`status` VARCHAR)");
			prest.executeUpdate();
			setVersion(3);
		} catch (SQLException e) {
			LOG.error("Error creating Database",e);
			return false;
		}
                
        return true;
    }
    
    public static int getVersion() throws SQLException{
    	PreparedStatement prest = null;
    	String sql	= "PRAGMA `user_version`";
    	prest = c.prepareStatement(sql);
    	ResultSet rs = prest.executeQuery();
    	if (rs.next()){
        	int version = rs.getInt(1);
        	rs.close();
        	return version;
    	}else{
    		return 0;
    	}
    }
    
    public static void setVersion(int version) throws SQLException{
    	PreparedStatement prest = null;
    	String sql	= "PRAGMA `user_version`="+version;
    	prest = c.prepareStatement(sql);
    	prest.executeUpdate();
    }
    
    public static int addUpload(int account, File file, Video data, String enddir) throws SQLException, IOException{
    	PreparedStatement prest = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String sql	= "INSERT INTO `uploads` (`account`, `file`, `lenght`, `data`,`enddir`, `status`) " +
    			"VALUES (?,?,?,?,?,?)";
    	prest = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    	prest.setInt(1, account);
    	prest.setString(2, file.getAbsolutePath());
    	prest.setLong(3, file.length());
    	prest.setString(4, mapper.writeValueAsString(data));
    	prest.setString(5, enddir);
    	prest.setString(6, UploadManager.Status.NOT_STARTED.toString());
    	prest.execute();
        ResultSet rs = prest.getGeneratedKeys();
        prest.close();
        if (rs.next()){
        	int id = rs.getInt(1);
        	rs.close();
        	return id;
        }else{
        	return -1;
        }
    }
    
    public static Boolean prepareUpload(int id, String Url, String yt_id){
    	PreparedStatement prest = null;
    	String sql	= "UPDATE `uploads` SET `status`=?,`url`=?,`yt_id`=? WHERE `id`=?";
    	try {
			prest = c.prepareStatement(sql);
	    	prest.setString(1, UploadManager.Status.PREPARED.toString());
	    	prest.setString(2, Url);
	    	prest.setString(3, yt_id);
	    	prest.setInt(4, id);
	    	boolean res = prest.execute();
	    	prest.close();
	    	return res;     
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public static Boolean startUpload(int id, long progress){
    	PreparedStatement prest = null;
    	String sql	= "UPDATE `uploads` SET `status`=?,`uploaded`=? WHERE `id`=?";
    	try {
			prest = c.prepareStatement(sql);
	    	prest.setString(1, UploadManager.Status.UPLOADING.toString());
	    	prest.setLong(2, progress);
	    	prest.setInt(3, id);
	    	boolean res = prest.execute();
	    	prest.close();
	    	return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    }
    
    public static Boolean updateUploadProgress(int id, long progress){
    	PreparedStatement prest = null;
    	String sql	= "UPDATE `uploads` SET `uploaded`=? WHERE `id`=?";
    	try {
			prest = c.prepareStatement(sql);
	    	prest.setLong(1, progress);
	    	prest.setInt(2, id);
	    	boolean res = prest.execute();
	    	prest.close();
	    	return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
    }

	public static Boolean setUploadFinished(int upload_id, Status Status) {
		PreparedStatement prest = null;
    	String sql	= "UPDATE `uploads` SET `status`=?,`url`=?,`uploaded`=`lenght` WHERE `id`=?";
    	try {
			prest = c.prepareStatement(sql);
	    	prest.setString(1, Status.toString());
	    	prest.setString(2, "");
	    	prest.setInt(3, upload_id);
	    	boolean res = prest.execute();
	    	prest.close();
	    	return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}		
	}

	public static Boolean deleteUpload(int upload_id) {
		PreparedStatement prest = null;
    	String sql	= "DELETE FROM `uploads` WHERE `id`=?";
    	try {
			prest = c.prepareStatement(sql);
	    	prest.setInt(1, upload_id);
	    	boolean res = prest.execute();
	    	prest.close();
	    	return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}				
	}
	
	public static Boolean updateUpload(int account, File file, Video data, String enddir, int id) throws SQLException, IOException{
    	PreparedStatement prest = null;
    	String sql	= "UPDATE `uploads` SET `account`=?, `file`=?, `lenght`=?, `enddir`=? WHERE `id`=?";
    	prest = c.prepareStatement(sql);
    	prest.setInt(1, account);
    	prest.setString(2, file.getAbsolutePath());
    	prest.setLong(3, file.length());
    	prest.setString(4, enddir);
    	prest.setInt(5, id);
    	prest.execute();
    	boolean res = prest.execute();
    	prest.close();
    	return res && updateUploadData(data, id);
    }
	
    public static Boolean updateUploadData(Video data, int id) throws SQLException, IOException{
    	PreparedStatement prest = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String sql	= "UPDATE `uploads` SET `data`=? WHERE `id`=?";
    	prest = c.prepareStatement(sql);
    	prest.setString(1, mapper.writeValueAsString(data));
    	prest.setInt(2, id);
    	prest.execute();
    	boolean res = prest.execute();
        prest.close();
        return res;
    }
    
    public static int saveTemplate(Template template) throws SQLException, IOException{
    	PreparedStatement prest = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String sql	= "INSERT INTO `templates` (`name`, `data`) " +
    			"VALUES (?,?)";
    	prest = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    	prest.setString(1, template.getName());
    	prest.setString(2, mapper.writeValueAsString(template));
    	prest.execute();
        ResultSet rs = prest.getGeneratedKeys();
        prest.close();
        if (rs.next()){
        	int id = rs.getInt(1);
        	rs.close();
        	return id;
        }else{
        	return -1;
        }
    }

	public static Boolean updateTemplate(int id, Template template) throws SQLException, IOException {
		PreparedStatement prest = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String sql	= "UPDATE `templates` SET `data`=? WHERE `id`=?";
    	prest = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    	prest.setString(1, mapper.writeValueAsString(template));
    	prest.setInt(2, id);
    	prest.execute();
    	boolean res = prest.execute();
        prest.close();
        return res;
	}

	public static Boolean deleteTemplate(int id) {
		PreparedStatement prest = null;
    	String sql	= "DELETE FROM `templates` WHERE `id`=?";
    	try {
			prest = c.prepareStatement(sql);
	    	prest.setInt(1, id);
	    	boolean res = prest.execute();
	    	prest.close();
	    	return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}			
	}

    
}