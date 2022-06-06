package cbs.example.traffic_analysis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class Custom_Listview extends BaseAdapter {
    protected LayoutInflater layoutInflater;
    protected ArrayList<String> found_device;
    protected ArrayList<String> device_ip;

    public Custom_Listview(Context context,ArrayList<String> found_device_data,ArrayList<String> device_ip_data){
        layoutInflater = LayoutInflater.from(context);
        found_device = found_device_data;
        device_ip = device_ip_data;

    }
    @Override
    public int getCount() {
        return found_device.size();
    }

    @Override
    public Object getItem(int position) {
        return found_device.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.activity_custom_listview,null);

        TextView first = (TextView) convertView.findViewById(R.id.textView12);
        TextView second = (TextView) convertView.findViewById(R.id.textView13);

        first.setText(found_device.get(position));
        second.setText(device_ip.get(position));

        return convertView;
    }
}