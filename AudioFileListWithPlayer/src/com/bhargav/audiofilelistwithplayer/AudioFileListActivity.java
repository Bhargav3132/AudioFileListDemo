package com.bhargav.audiofilelistwithplayer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.util.Log;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class AudioFileListActivity extends Activity implements OnErrorListener, OnCompletionListener, Runnable {

	/**
	 * ListView declaration
	 */
	private ListView lvAudioFileList;

	/**
	 * Adapter declaration for Audio files
	 */
	private AudioListAdapter audioListAdapter;
	ArrayList<AudioDataModel> audioFileList;

	/**
	 * Constants
	 */
	private final String BROADCAST_CURRENT_POSITION = "PLAY";

	/**
	 * TextView declaration
	 */
	private TextView tvTimeReached, tvTimeRemaining;

	/**
	 * SeekBar declaration
	 */
	private SeekBar sbPlayProgress;

	/**
	 * MediaPlayer object
	 */
	private MediaPlayer mp = new MediaPlayer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_file_list);

		// View initialization
		lvAudioFileList = (ListView) findViewById(R.id.activity_audio_file_list_lv);
		sbPlayProgress = (SeekBar) findViewById(R.id.activity_audio_file_list_pb);
		tvTimeReached = (TextView) findViewById(R.id.activity_audio_file_list_tv_progress);
		tvTimeRemaining = (TextView) findViewById(R.id.activity_audio_file_list_tv_remaining);

		/**
		 * Check for sdcard status
		 */
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

			audioFileList = new ArrayList<AudioDataModel>();
			setAdapterForAudios();

		} else {
			Toast.makeText(AudioFileListActivity.this, "No sd card found.", Toast.LENGTH_LONG).show();
		}

		sbPlayProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				if (fromUser && mp.isPlaying()) {
					mp.seekTo(progress);
				}

			}
		});

	}

	/**
	 * Set adapter for audio file in list and play
	 */
	private void setAdapterForAudios() {

		// fetch data for audio files available in sd card
//		Cursor mCursor = managedQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID,
//				MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.SIZE }, AudioColumns.IS_MUSIC + "!=0", null, "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");
		
		
		CursorLoader cursorLoader=new CursorLoader(AudioFileListActivity.this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.SIZE }, AudioColumns.IS_MUSIC + "!=0", null, null);
		Cursor mCursor=cursorLoader.loadInBackground();

		// set data in model
		AudioDataModel audioFileModel;

		while (mCursor.moveToNext()) {


			audioFileModel = new AudioDataModel();
			audioFileModel.setFileName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
			audioFileModel.setFileDuration(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
			audioFileModel.setFileId(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));
			audioFileModel.setFilePath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
			audioFileModel.setFileSize(getFileSize(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.SIZE))));
			audioFileModel.setSelected(false);

			audioFileList.add(audioFileModel);

		}

		// if audio data found than visible the list view
		if (audioFileList.size() > 0) {

			audioListAdapter = new AudioListAdapter(AudioFileListActivity.this, audioFileList);
			lvAudioFileList.setAdapter(audioListAdapter);

		} else {
			Toast.makeText(AudioFileListActivity.this, "No audio files found.", Toast.LENGTH_LONG).show();
		}

		// Register receiver
		IntentFilter intentFilter = new IntentFilter(BROADCAST_CURRENT_POSITION);
		registerReceiver(mReceiver, intentFilter);
	}

	/**
	 * Convert file size for attachment from bytes to kb or mb
	 * 
	 * @param fileSize
	 *            = file size in string
	 * @return file size in kb or mb
	 */
	public static String getFileSize(String fileSize) {
		int DIVIDER = 1024;
		String result = null;
		DecimalFormat format;
		float size = Float.parseFloat(fileSize);
		if ((size / DIVIDER) > DIVIDER) {
			format = new DecimalFormat(".0");
			result = format.format(((size / DIVIDER)) / DIVIDER) + " MB";
			Log.e("File size in MB", "" + ((size / DIVIDER)) / DIVIDER);
		} else {
			format = new DecimalFormat(".00");
			result = format.format((size / DIVIDER)) + " KB";
			Log.e("File size in KB", "" + (size / DIVIDER));
		}
		return result;
	}

	/**
	 * BroadCastReceiver for showing progress of audio file
	 * 
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getExtras() != null) {
				int currentProgress = intent.getIntExtra("play_position", 0);
				tvTimeReached.setText(mediaTime((long) currentProgress));
				sbPlayProgress.setProgress(currentProgress);
			}

		}
	};

	/**
	 * Method for converting milliseconds to minute
	 * 
	 * @param milliSecs
	 * @return
	 */
	public static String mediaTime(long milliSecs) {
		StringBuffer sb = new StringBuffer();
		long m = milliSecs / (60 * 1000);
		sb.append(m < 10 ? "0" + m : m);
		sb.append(":");
		long s = (milliSecs % (60 * 1000)) / 1000;
		sb.append(s < 10 ? "0" + s : s);
		return sb.toString();
	}

	/**
	 * Play Audio file from list
	 * 
	 * @param filePath
	 *            = path of file
	 * @param playFile
	 *            = flag for pause and play
	 * 
	 */
	public void playAudioFile(String filePath, boolean playFile) {

		mp.setOnErrorListener(this);
		mp.setOnCompletionListener(this);
		try {
			Log.e("File Path", filePath);
			if (mp.isPlaying()) {
				mp.stop();
				mp.reset();
			}
			if (filePath != null && playFile) {
				mp.reset();
				mp.setDataSource(AudioFileListActivity.this, Uri.parse(filePath));
				mp.prepare();
				mp.start();

				// set total time to view
				tvTimeRemaining.setText(mediaTime((long) mp.getDuration()));

				// set max progress to seek bar
				sbPlayProgress.setMax(mp.getDuration());

				// start thread for updating current progress in seek bar
				new Thread(this).start();

			} else if (filePath.equals("") && !playFile) {

				// stop playing audio file
				tvTimeRemaining.setText("00:00");
				tvTimeReached.setText("00:00");
				if (mp.isPlaying()) {
					mp.stop();
					new Thread(this).stop();
				}

			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		if (mp.isPlaying()) {
			mp.stop();
		}
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.media.MediaPlayer.OnErrorListener#onError(android.media.MediaPlayer,
	 *      int, int)
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {

		audioListAdapter.changeFileAfterCompletion();
		Toast.makeText(AudioFileListActivity.this, "File not supported.", Toast.LENGTH_LONG).show();
		return false;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see android.media.MediaPlayer.OnCompletionListener#onCompletion(android.media.MediaPlayer)
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {

		Log.e("Playing Complete", "Playing Complete");
		audioListAdapter.changeFileAfterCompletion();
		// new Thread(this).stop();
		tvTimeRemaining.setText("00:00");
		tvTimeReached.setText("00:00");

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		int currentPosition = 0;
		int total = mp.getDuration();

		while (mp.isPlaying() && currentPosition < total) {
			currentPosition = mp.getCurrentPosition();

			// broadcast the current progress
			callBroadcast(currentPosition);
		}

	}

	/**
	 * Call to BroadCast the audio files current position
	 * 
	 * @param currentPosition
	 */
	private void callBroadcast(int currentPosition) {

		Intent intent = new Intent();
		intent.putExtra("play_position", currentPosition);
		intent.setAction(BROADCAST_CURRENT_POSITION);
		sendBroadcast(intent);

	}

}
