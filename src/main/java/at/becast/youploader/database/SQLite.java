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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import at.becast.youploader.gui.frmMain;
import at.becast.youploader.youtube.data.Video;

public class SQLite {
	
	public static Connection c;
	
	private SQLite( String database ){
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:"+database);
		} catch ( Exception e ) {
		  System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
	
    public static Connection getInstance(){
        if(c == null)
            new SQLite(frmMain.DB_FILE);
        return c;
    }
    
    public static int addUpload(String account, File file, Video data) throws SQLException, JsonGenerationException, JsonMappingException, IOException{
    	PreparedStatement prest = null;
    	ObjectMapper mapper = new ObjectMapper();
    	String sql	= "INSERT INTO `uploads` (`account`, `file`, `lenght`, `data`, `status`) " +
    			"VALUES (?,?,?,?,?)";
    	prest = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    	prest.setString(1, account);
    	prest.setString(2, file.getAbsolutePath());
    	prest.setLong(3, file.length());
    	prest.setString(4, mapper.writeValueAsString(data));
    	prest.setString(5, "NOT_STARTED");
    	prest.execute();
        ResultSet rs = prest.getGeneratedKeys();
        if (rs.next()){
        	return rs.getInt(1);
        }else{
        	return -1;
        }
    }
}