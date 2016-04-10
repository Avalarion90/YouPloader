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
package at.becast.youploader.youtube.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.becast.youploader.account.Account;
import at.becast.youploader.gui.FrmMain;
import at.becast.youploader.util.JSONSplitter;
import at.becast.youploader.youtube.data.CookieJar;
import at.becast.youploader.youtube.data.GameDataItem;

public class GameData {
	private Account acc;
	private static final Logger LOG = LoggerFactory.getLogger(GameData.class);
	private CookieJar persistentCookieStore = new CookieJar();
	List<GameDataItem> gd = new ArrayList<GameDataItem>();
	public GameData(int acc_id) {
		try {
			this.acc = Account.read(acc_id);
		} catch (IOException e) {
			LOG.error("Unable to read Account ", e);
		}
	    
	    CookieManager cmrCookieMan = new CookieManager(persistentCookieStore, null);
		persistentCookieStore.setSerializeableCookies(this.acc.getCookie());
		CookieHandler.setDefault(cmrCookieMan);
	}

	public List<GameDataItem> getGames(String search) throws IOException {
		String url = String.format("https://www.youtube.com/game_suggestions_ajax?action_get_game_suggestions=1&token=%s&max_matches=10&use_similar=0",URLEncoder.encode(search,"UTF-8"));
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		con.setRequestProperty("User-Agent", FrmMain.APP_NAME+" "+FrmMain.VERSION);
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		String data = response.substring(1, response.length()-1).toString();
		String[] items = JSONSplitter.split(data);
		for(String i : items){
			String[] d = JSONSplitter.split(i.substring(1,i.length()-1));
			gd.add(new GameDataItem(d));
		}
		return gd;
	}		
}
