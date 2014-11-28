package co.tapdatapp.tapandroid.service;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

import java.text.DecimalFormat;
import java.util.List;

import co.tapdatapp.tapandroid.R;


/**
 * Created by arash on 10/13/14.
 */
public class YapaAdapter extends ArrayAdapter {
    private Context context;
    private boolean useList = true;

    public YapaAdapter(Context context, List items){
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
    }
    private class ViewHolder{
        TextView tvDol;
        TextView tvTaps;
        ImageView ivYap;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        ViewHolder holder = null;
        TapTxn item = (TapTxn) getItem(position);
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if(useList){
                viewToUse = mInflater.inflate(R.layout.yapalayout, null); }
            else {
                viewToUse = mInflater.inflate(R.layout.yapalayout, null); }
            holder = new ViewHolder();
            holder.tvDol = (TextView)viewToUse.findViewById(R.id.tvDollars);
            holder.ivYap = (ImageView) viewToUse.findViewById(R.id.ivTxnYap);
            holder.tvTaps = (TextView) viewToUse.findViewById(R.id.tvTaps);

            viewToUse.setTag(holder);
        } else
        {
            viewToUse = convertView; holder = (ViewHolder) viewToUse.getTag();
        }
        holder.tvDol.setText("$" +new DecimalFormat("#.##").format(item.getTXNamountUSD()));
//            + String.format("());
            new TapCloud.DownloadImageTask(holder.ivYap)
                .execute(item.getPayloadImageThumb());
            return viewToUse;
    }
    public static String fmt(double d)
    {
        if(d == (long) d)
            return String.format("%d",(long)d);
        else
            return String.format("%s",d);
    }
    }


