package com.citywebtechnologies.smsconnect;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.citywebtechnologies.smsconnect.model.ConnectSMS;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ConnectSMSListAdapter extends BaseAdapter {
	Context context;
	List<ConnectSMS> connectSMSList;




	static class ViewHolder {
		@Bind(R.id.address)
		TextView address;
		@Bind(R.id.message)
		TextView message;
		@Bind(R.id.sentStatus)
		TextView sentStatus;
		@Bind(R.id.dateReceived)
		TextView dateReceived;
		public ViewHolder(View view) {
			ButterKnife.bind(this, view);
		}
	}

	ConnectSMSListAdapter(Context context, List<ConnectSMS> smsList) {
		this.context = context;
		this.connectSMSList = smsList;
	}

	void updateAdapter(List<ConnectSMS> data){
		connectSMSList.clear();
		connectSMSList.addAll(data);
		notifyDataSetChanged();
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		ConnectSMS sms = connectSMSList.get(position);
		/*if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_msg, null);
		}*/
		ViewHolder holder;
		if (convertView != null) {
			holder = (ViewHolder) convertView.getTag();
		} else {
			convertView = inflater.inflate(R.layout.listitem_msg, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}

		holder.message.setText(sms.getMessage());

		holder.address.setText(sms.getAddress());
		if (sms.getSentStatus() == 1)
			holder.sentStatus.setText("Sent & server updated");
		else if (sms.getSentStatus() == 2)
			holder.sentStatus.setText("Sent, Waiting server update");
		else if (sms.getSentStatus() == 3)
			holder.sentStatus.setText("Failed, Retrying");
		else if (sms.getSentStatus() == 4)
			holder.sentStatus.setText("No service, Retrying");
		else if (sms.getSentStatus() == 5)
			holder.sentStatus.setText("No radio, Retrying");
		else if (sms.getSentStatus() == 6)
			holder.sentStatus.setText("Permission Denied, Retrying");
		else if (sms.getSentStatus() == -1 || sms.getSentStatus() == 11)
			holder.sentStatus.setText("Failed, system error");
		else if (sms.getSentStatus() == 100 || sms.getSentStatus() == 99)
			holder.sentStatus.setText("Queued");
		else
			holder.sentStatus.setText("Pending");


		if (sms.getDateSent() > 0) {
			SimpleDateFormat sdf = new SimpleDateFormat("d MMM yy HH:mm",
					Locale.getDefault());

			holder.dateReceived.setText(sdf.format(new Date(sms.getDateSent())));
		}else
		{
			holder.dateReceived.setText("...");
		}
		return convertView;
	}

	@Override
	public int getCount() {
		return connectSMSList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
}
