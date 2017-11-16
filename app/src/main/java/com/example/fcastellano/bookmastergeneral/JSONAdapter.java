package com.example.fcastellano.bookmastergeneral;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import com.squareup.picasso.Picasso;

public class JSONAdapter extends BaseAdapter {
    private static final String IMAGE_URL_BASE = "http://covers.openlibrary.org/b/id/";

    Context mContext;
    LayoutInflater mInflater;
    JSONArray mJsonArray;

    public JSONAdapter (Context context, LayoutInflater inflater){
        mContext = context;
        mInflater = inflater;
        mJsonArray = new JSONArray();
    }

    @Override
    public int getCount() {
        return mJsonArray.length();
    }

    @Override
    public JSONObject getItem(int position) {
        return mJsonArray.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null){
            view = mInflater.inflate(R.layout.row_book, null);

            holder = new ViewHolder();
            holder.thumbnailImageView = (ImageView) view.findViewById(R.id.img_thumbnail);
            holder.titleTextView = (TextView) view.findViewById(R.id.text_title);
            holder.authorTextView = (TextView) view.findViewById(R.id.text_author);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // Thumbnail
        JSONObject jsonObject = (JSONObject) getItem(position);
        if (jsonObject.has("cover_i")){
            String imageID = jsonObject.optString("cover_i");
            String imageURL = IMAGE_URL_BASE + imageID + "-S.jpg";
            Picasso.with(mContext)
                    .load(imageURL)
                    .placeholder(R.drawable.ic_books)
                    .into(holder.thumbnailImageView);
        } else {
            holder.thumbnailImageView.setImageResource(R.drawable.ic_books);
        }

        // Title & Author
        String bookTitle = "";
        String authorName = "";

        if (jsonObject.has("title")){
            bookTitle = jsonObject.optString("title");
        }

        if (jsonObject.has("author_name")){
            // Yes, it seems to me that there is no "author" key,
            // instead there is almost always an "author_name" key,
            // an array, usually with one value only... let's use the
            // first entry for now.
            authorName = jsonObject.optJSONArray("author_name")
                        .optString(0, "");
        }

        holder.titleTextView.setText(bookTitle);
        holder.authorTextView.setText(authorName);

        return view;
    }

    public void updateData(JSONArray jsonArray){
        mJsonArray = jsonArray;
        notifyDataSetChanged();
    }

    // package the three objects in a single wrapper
    private static class ViewHolder {
        public ImageView thumbnailImageView;
        public TextView titleTextView;
        public TextView authorTextView;
    }
}
