package com.bhargav.audiofilelistwithplayer;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class AudioListAdapter extends BaseAdapter {

	/**
	 * Context
	 */
	private Context mContext;

	/**
	 * LayoutInflater declaration
	 */
	private LayoutInflater inflater = null;

	/**
	 * Holder class declaration
	 */
	private Holder holder = null;

	/**
	 * Variable declaration
	 */
	private boolean[] isPlaying;
	private int previousPlaying = -1, mPosition = -1;
	private boolean isFirstTime;
	private ImageView ivPlayingView = null;

	/**
	 * ArrayList for containing Audio files data
	 */
	private ArrayList<AudioDataModel> audioFileList;

	/**
	 * ArrayList to store selected files
	 */
	private ArrayList<String> selectedFileList;

	/**
	 * Constructor for calling this Adapter
	 * 
	 * @param context
	 * @param audioFileList
	 */
	public AudioListAdapter(Context context, ArrayList<AudioDataModel> audioFileList) {
		this.mContext = context;
		this.audioFileList = audioFileList;
		selectedFileList = new ArrayList<String>();
		isFirstTime = true;
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		isPlaying = new boolean[audioFileList.size()];
		for (int i = 0; i < isPlaying.length; i++) {
			isPlaying[i] = false;
		}
	}

	@Override
	public int getCount() {
		return audioFileList.size();
	}

	@Override
	public Object getItem(int position) {
		return audioFileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<String> getSelectedFileList() {
		return selectedFileList;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		if (convertView == null) {

			// Initialize views
			holder = new Holder();
			convertView = inflater.inflate(R.layout.raw_audio_list_item, null);
			holder.tvFileName = (TextView) convertView.findViewById(R.id.row_audio_list_item_tv_file_name);
			holder.tvFileSize = (TextView) convertView.findViewById(R.id.row_audio_list_item_tv_file_size);
			holder.ivFileIcon = (ImageView) convertView.findViewById(R.id.row_audio_list_item_iv_img);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.row_audio_list_item_cbox_select);

			convertView.setTag(holder);
			holder.checkBox.setTag(position);

		} else {

			holder = (Holder) convertView.getTag();
			((Holder) convertView.getTag()).checkBox.setTag(position);

		}

		// get the map object from list
		AudioDataModel audioFileModel = audioFileList.get(position);

		// set data to view
		holder.tvFileName.setText(audioFileModel.getFileName());
		holder.tvFileSize.setText(audioFileModel.getFileSize());

		if (!isPlaying[position]) {
			holder.ivFileIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_play));
		} else {
			holder.ivFileIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_pause));
		}
		
		if(Boolean.valueOf(audioFileModel.isSelected())){
			holder.checkBox.setChecked(true);
		}else {
			holder.checkBox.setChecked(false);
		}

		holder.ivFileIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mPosition = position;
				ivPlayingView = ((ImageView) v);
				if (position != previousPlaying) {
					if (!isPlaying[position]) {
						isPlaying[position] = true;
						((ImageView) v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_pause));
						((AudioFileListActivity)mContext).playAudioFile(audioFileList.get(position).getFilePath(), true);
					}

					if (!isFirstTime) {
						isPlaying[previousPlaying] = false;
					} else {
						isFirstTime = false;
					}

				} else {
					Log.v("else pause", "else pause");
					if (isPlaying[position]) {
						isPlaying[position] = false;
						((ImageView) v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_play));
						((AudioFileListActivity)mContext).playAudioFile("", false);

					} else {
						isPlaying[position] = true;
						((ImageView) v).setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_pause));
						((AudioFileListActivity)mContext).playAudioFile(audioFileList.get(position).getFilePath(), true);
					}

				}
				previousPlaying = position;
				notifyDataSetChanged();
			}
		});

		holder.checkBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				int chkPos = (Integer) v.getTag();
				Log.e("Position", "" + chkPos);
				audioFileList.get(chkPos).setSelected((((CheckBox) v).isChecked()));
				if (selectedFileList.contains(String.valueOf(chkPos))) {
					selectedFileList.remove(String.valueOf(chkPos));
				} else {
					selectedFileList.add(String.valueOf(chkPos));
				}
				
			}
		});

		convertView.setId(Integer.valueOf(audioFileModel.getFileId()));

		return convertView;
	}

	public void changeFileAfterCompletion() {
		isPlaying[mPosition] = false;
		ivPlayingView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_play));
		notifyDataSetChanged();
	}

	/**
	 * Holder class
	 */
	private class Holder {
		TextView tvFileName, tvFileSize;
		ImageView ivFileIcon;
		CheckBox checkBox;
	}

}
