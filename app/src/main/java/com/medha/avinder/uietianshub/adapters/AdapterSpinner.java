package com.medha.avinder.uietianshub.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.medha.avinder.uietianshub.R;

import java.util.ArrayList;

public class AdapterSpinner extends BaseAdapter {
    private Context context;
    private String[] stringArray;
    private ArrayList<String> list;
    private LayoutInflater layoutInflater;
    private int type;

    public AdapterSpinner(Context context, String[] stringArray, int type, ArrayList<String> list) {
        this.context = context;
        this.stringArray = stringArray;
        this.type = type;
        this.list = list;

        layoutInflater = (LayoutInflater.from(context));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (type == 0) {
            view = layoutInflater.inflate(R.layout.item_signup_spinner, null);
            TextView names = view.findViewById(R.id.textView);
            ImageView dropdown = view.findViewById(R.id.imageView);
            names.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat_semi_bold));
            names.setText(stringArray[i]);
            if (names.getText().equals("Select Semester") || names.getText().equals("Select Branch")) {
                names.setTextColor(Color.rgb(18, 18, 18));
                dropdown.setVisibility(View.VISIBLE);
            } else {
                names.setTextColor(Color.rgb(0, 0, 0));
                dropdown.setVisibility(View.INVISIBLE);
            }
        } else if (type == 1){
            view = layoutInflater.inflate(R.layout.item_add_paper_spinner, null);
            TextView tvSubject = view.findViewById(R.id.tvSubject);
            tvSubject.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat_semi_bold));
            tvSubject.setText(list.get(i));
        } else {
            view = layoutInflater.inflate(R.layout.item_add_paper_spinner, null);
            int padding = context.getResources().getDimensionPixelOffset(R.dimen.margin_8_dp);
            view.findViewById(R.id.linearLayout).setPadding(padding + 10, padding, padding, padding);
            TextView tvSubject = view.findViewById(R.id.tvSubject);
            tvSubject.setTypeface(ResourcesCompat.getFont(context, R.font.montserrat));
            tvSubject.setText(stringArray[i]);
            tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tvSubject.setGravity(Gravity.START);
        }
        return view;
    }

    @Override
    public int getCount() {
        if (type == 1) {
            return list.size();
        } else {
            return stringArray.length;
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
}