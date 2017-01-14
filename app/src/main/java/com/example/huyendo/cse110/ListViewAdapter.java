package com.example.sondo.cse110;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Son Do on 12/2/2015.
 */
public class ListViewAdapter extends ArrayAdapter<listViewItem> {
    Context mContext;
    int mResource;
    ArrayList<listViewItem> mData;
    public ListViewAdapter(Context context, int resource, ArrayList<listViewItem> data) {
        super(context, resource, data);
        mContext = context;
        mResource = resource;
        mData = data;
    }
    @Override
    public listViewItem getItem(int position){
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View row_layout = convertView;
        ListViewAdapterHolder holder = null;

        if(row_layout == null){
            //Inflate the layout for a single row
            LayoutInflater inflater = LayoutInflater.from(mContext);
            row_layout = inflater.inflate(mResource,parent,false);

            holder = new ListViewAdapterHolder();
            //Get a reference to different view element we wish to update
            holder.itemName = (TextView) row_layout.findViewById(R.id.itemName_listview);
            holder.userItemName = (TextView) row_layout.findViewById(R.id.UserItemName);
            holder.date = (TextView) row_layout.findViewById(R.id.DateCreated);
            holder.imageIcon = (ImageView) row_layout.findViewById(R.id.itemImage);

            row_layout.setTag(holder);
        }
        else {
            holder = (ListViewAdapterHolder) row_layout.getTag();
        }

        //get the data from the arraylist
        listViewItem item = mData.get(position);

        //setting the view to reflect what we need to display
        if(item.ItemName.length()>=10){
            holder.itemName.setText(item.ItemName.substring(0,10)+"...");
        }
        else
            holder.itemName.setText(item.ItemName);
        holder.itemName.setTypeface(Typeface.createFromAsset(mContext.getAssets(), "fonts/Vanilla.ttf"));
        holder.itemName.setTextColor(Color.YELLOW);
        holder.userItemName.setText("By " + item.UserCreated);
        holder.userItemName.setTextColor(Color.RED);
        holder.date.setText(item.date);
        holder.date.setTextColor(Color.DKGRAY);
        if(!item.ImageName.isEmpty()){
            int imageId = mContext.getResources().getIdentifier(item.ImageName,"drawable",mContext.getPackageName());
            holder.imageIcon.setImageResource(imageId);
        }
        return row_layout;
    }
    private static class ListViewAdapterHolder{
        TextView itemName;
        TextView userItemName;
        TextView date;
        ImageView imageIcon;
    }
}

