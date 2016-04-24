package at.becast.youploader.youtube.playlists;

import java.util.Locale;
import java.util.ResourceBundle;

import at.becast.youploader.account.AccountType;
import at.becast.youploader.gui.FrmMain;
import at.becast.youploader.gui.PlaylistItem;
import at.becast.youploader.util.UTF8ResourceBundle;

public class PlaylistUpdater implements Runnable {

	private FrmMain parent;
	private static final ResourceBundle LANG = UTF8ResourceBundle.getBundle("lang", Locale.getDefault());
	public PlaylistUpdater(FrmMain parent){
		this.parent = parent;
	}
	@Override
	public void run() {
		parent.getStatusBar().getProgressBar().setIndeterminate(true);
		parent.getStatusBar().setMessage("Updating Playlists");
		AccountType acc = (AccountType) parent.getCmbAccount().getSelectedItem();
		PlaylistManager pm = new PlaylistManager(acc.getValue());
		pm.save();
		pm.load();
		parent.getPlaylistPanel().clearPlaylists();
		for(Playlist p : pm.getPlaylists().get(acc.getValue())){
			PlaylistItem i = new PlaylistItem(p.id, p.ytId, p.name, p.image);
			parent.getPlaylistPanel().getPlaylistPanel().add(i);
		}
		parent.getStatusBar().getProgressBar().setIndeterminate(false);
		parent.getStatusBar().setMessage(LANG.getString("Status.Ready"));
	}

}
