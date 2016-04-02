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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.becast.youploader.account.AccountManager;
import at.becast.youploader.database.SQLite;
import at.becast.youploader.gui.UploadItem;
import at.becast.youploader.youtube.GuiUploadEvent;
import at.becast.youploader.youtube.Uploader;
import at.becast.youploader.youtube.data.Upload;
import at.becast.youploader.youtube.data.Video;
import at.becast.youploader.youtube.data.VideoMetadata;
import at.becast.youploader.youtube.exceptions.UploadException;

public class UploadWorker extends Thread {
	
	public Video videodata;
	public VideoMetadata metadata;
	public int id, acc_id;
	public int retrys = 0;
	private int speed_limit;
	public File file;
	public UploadItem frame;
	public Upload upload;
	private UploadEvent event;
	private Uploader uploader;
	public String url, enddir;
	private AccountManager AccMgr;
	private static final Logger LOG = LoggerFactory.getLogger(UploadWorker.class);
	
	public UploadWorker(int id, UploadItem frame, int acc_id, File file, Video videodata, int speed_limit, String enddir, VideoMetadata metadata){
		this.id = id;
		this.speed_limit = speed_limit;
		this.frame = frame;
		this.upload = null;
		this.acc_id = acc_id;
		this.file = file;
		this.videodata = videodata;
		this.metadata = metadata;
		this.enddir = enddir;
		this.AccMgr = AccountManager.getInstance();
		this.uploader = new Uploader(this.AccMgr.getAuth(acc_id));
		this.event = new GuiUploadEvent(frame);
	}
	
	public UploadWorker(int id, UploadItem frame, int acc_id, File file, Video videodata, int speed_limit, String enddir, VideoMetadata metadata, String url, String yt_id){
		this.id = id;
		this.speed_limit = speed_limit;
		this.frame = frame;
		this.acc_id = acc_id;
		this.file = file;
		this.upload = new Upload(url,file,yt_id,videodata,metadata);
		this.videodata = videodata;
		this.metadata = metadata;
		this.enddir = enddir;
		this.AccMgr = AccountManager.getInstance();
		this.uploader = new Uploader(this.AccMgr.getAuth(acc_id));
		this.event = new GuiUploadEvent(frame);
	}
	
	public void prepare(){
		try {
			this.upload = this.uploader.prepareUpload(this.file, this.videodata);
		} catch (IOException | UploadException e) {
			LOG.error("Could not prepare upload ",e);
		}
		this.upload.setMetadata(this.metadata);
		SQLite.prepareUpload(this.id,this.upload.url,this.upload.id);
		this.frame.getlblUrl().setText("https://www.youtube.com/watch?v="+this.upload.id);
	}
	
	public void setMetadata(){
		
	}
	
	public void setThumbnail(){
		try {
			this.uploader.uploadThumbnail(new File(this.metadata.getThumbnail()), upload);
		} catch (IOException | UploadException e) {
			LOG.error("Could not set Thumbnail ",e);
		}
	}
	
	@Override
	public void run(){
		setName( "Uploader-" + getId() );
		try {
			if(this.upload == null){
				this.prepare();
				SQLite.startUpload(this.id,0);
				this.uploader.upload(this.upload, this.event, this.speed_limit);
			}else{
				try{
					this.uploader.resumeUpload(this.upload, this.event, this.speed_limit);
				}catch( IOException e){
					this.event.onError(true);
				}
			}
			
		} catch (IOException e) {
			LOG.error("Could not run upload",e);
		}
	}
	
	public void setSpeed(int Speed){
		this.uploader.setSpeedlimit(Speed);
	}
	
	public void resetUploader(){
		this.uploader = new Uploader(this.AccMgr.getAuth(acc_id));
	}
	
	
	public void abort(){
		this.uploader.abort();
	}
	public void uploadThumbnail(){
		this.setThumbnail();
	}
	
	public int getRetrys() {
		return retrys;
	}

	public boolean delete() {
		try {
			return this.uploader.delete(this.upload);
		} catch (IOException e) {
			return false;
		}
		
	}
	
	public void setRetrys(int retrys) {
		this.retrys = retrys+1;
	}
	
	public void suspend(Boolean suspend){
		synchronized (this.uploader) {
			if(!suspend){
				this.uploader.notify();
			}
		}
	}
}
