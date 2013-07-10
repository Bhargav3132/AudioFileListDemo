package com.bhargav.audiofilelistwithplayer;


public class AudioDataModel{

	private String fileName = "";
	private String fileSize = "";
	private String filePath = "";
	private String fileId = "";
	private String fileDuration = "";
	private boolean selected = false;
	
	public String getFileDuration() {
		return fileDuration;
	}

	public void setFileDuration(String fileDuration) {
		this.fileDuration = fileDuration;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}