package com.example.expenseutility.entityadapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.expenseutility.FirstFragment;
import com.example.expenseutility.R;
import com.example.expenseutility.database.DatabaseHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Custom adapter to bind suggestions data to ListView
public class SuggestionAdapter extends BaseAdapter {

    private Context context;
    private List<Suggestion> suggestionsList;
    private static SuggestionAdapter adapter;


    public SuggestionAdapter(Context context, List<Suggestion> suggestionsList) {
        this.context = context;
        this.suggestionsList = suggestionsList;
        adapter = this;
    }




    @Override
    public int getCount() {
        return suggestionsList.size();
    }

    @Override
    public Object getItem(int position) {
        return suggestionsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return suggestionsList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.suggestion_list_item, null);
        }

        TextView idTextView = view.findViewById(R.id.item_id);
        TextView descriptionTextView = view.findViewById(R.id.item_description);
        ImageButton delSuggBtn = view.findViewById(R.id.delSuggBtn);
        CheckBox checkBox = view.findViewById(R.id.chkBox);

        Suggestion suggestion = suggestionsList.get(position);
        idTextView.setText(String.valueOf(suggestion.getId()));
        descriptionTextView.setText(suggestion.getDescription());

        delSuggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper db = new DatabaseHelper(v.getContext());
                int res = db.deleteRowSuggestion(suggestion.getId());
                if(res > 0) {
                    suggestionsList.remove(position);
                    adapter.notifyDataSetChanged();
                    FirstFragment.refreshSuggList();
                }
            }
        });
        return view;
    }

    public void refreshList(SuggestionAdapter adapter, List<Suggestion> suggestionsListExt) {
        suggestionsList.clear();
        suggestionsList.addAll(suggestionsListExt);
        adapter.notifyDataSetChanged();
    }
}
