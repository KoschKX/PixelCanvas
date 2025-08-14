package com.screechingchimp.pixelcanvas;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String>{

    private final Activity context;
    private ArrayList<String> ttl;

    public int selected=0;
    public int stopperHeight=80;

    public int highlightColor=0;

    private static final String TAG = "PadNotes";

    public CustomListAdapter(Activity context,
                              ArrayList<String>ttl) {
        super(context, R.layout.list_single, ttl);
        this.context = context;
        this.ttl = ttl;

        highlightColor=context.getResources().getColor(R.color.colorAccent);
    }


    public void markListItem(int pos){
        selected=pos;
        notifyDataSetInvalidated();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

          LayoutInflater inflater = context.getLayoutInflater();
          view = inflater.inflate(R.layout.filelist_single, null);

          TextView txtTitle = (TextView) view.findViewById(R.id.txt);
          txtTitle.setText(ttl.get(position));

          View tbl = view.findViewById(R.id.tbl);
          View bdr = view.findViewById(R.id.bdr);

          if (selected == position) {
              txtTitle.setTextColor(highlightColor);
          }

          bdr.setVisibility(View.VISIBLE);

        return view;
    }

    public void refresh(){
        notifyDataSetInvalidated();
    }

}
