package com.example.bigdatarss;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainActivity extends AppCompatActivity {

    private ArrayList<BigDataArticle> articleList;
    private URL rssUrl = null;
    private int change_view_flag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Action bar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        // Call Load RSS to initialize ArrayList of Feeds
        loadRSS();
    }

    /**
     * Render List View
     */
    private void loadListView(){
        ListView listView = (ListView) findViewById(R.id.rss_list_view);

        // Set the adapter of the list view
        FeedAdapter feedAdapter = new FeedAdapter(this, R.layout.list_item, articleList);

        // Set the adapter of the Listview
        listView.setAdapter(feedAdapter);
    }

    /**
     * Feed Adapter - Listview
     */
    private class FeedAdapter extends ArrayAdapter<BigDataArticle>{

        private ArrayList<BigDataArticle> items;

        public FeedAdapter(Context context, int textViewResourceId, ArrayList<BigDataArticle> items){
            super(context, textViewResourceId, items);
            this.items = items;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            View v = convertView;
            if(v == null){
                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.list_item, null);
            }
            BigDataArticle article = items.get(position);
            if(article != null){
                TextView title = (TextView)v.findViewById(R.id.title);
                TextView desc  = (TextView)v.findViewById(R.id.description);
                TextView pubdate  = (TextView)v.findViewById(R.id.pubDate);
                if(title != null){
                    title.setText(article.title);
                }
                if(desc != null){
                    desc.setText(article.description.replace("<p>","").replace("&#8211","").replace("&#8217",""));
                }
                if(pubdate != null){
                    pubdate.setText(article.pubDate.replace(" +0000",""));
                }
            }

            // Click listener for a list view specific item
            v.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    Toast.makeText(getContext(), "You clicked in the " + items.get(position).link, Toast.LENGTH_LONG).show();
                    Intent open_url = new Intent(Intent.ACTION_VIEW, Uri.parse( items.get(position).link ));
                    startActivity(open_url);
                }
            });

            return v;
        }
    }


    /**
     * action bar menu inflation
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }


    /**
     * onOptionItemSelected class to validate Render style / Marker locations / "My Location"
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.refresh:
                //Toast.makeText(this, "Refresh Action!", Toast.LENGTH_LONG).show();
                loadRSS();
                return true;

            case R.id.feed1:
                //Toast.makeText(this, "First!", Toast.LENGTH_LONG).show();
                change_view_flag = 1;
                loadRSS();
                return true;

            case R.id.feed2:
                //Toast.makeText(this, "Second Feed!", Toast.LENGTH_LONG).show();
                change_view_flag = 2;
                loadRSS();
                return true;

            case R.id.feed3:
                //Toast.makeText(this, "Third Feed!", Toast.LENGTH_LONG).show();
                change_view_flag = 3;
                loadRSS();
                return true;

            default:
                // If we got here, the user's action was not recognized / Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Load RSS feed - Asynchronous
     */
    public void loadRSS(){
        RssProcessingTask rssp = new RssProcessingTask();
        rssp.execute();
    }

    /**
     * Nested Class - AsyncTask
     */
    class RssProcessingTask extends AsyncTask<Void, Void, Void> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            // Start Progress Dialog during Background processing
            this.dialog.setMessage("Processing...");
            this.dialog.show();

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Log.d("BRUNO", "InBackground()");

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = null;

            try {
                saxParser = saxParserFactory.newSAXParser();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

            try {
                if(change_view_flag == 1) {
                    rssUrl = new URL("https://www.kdnuggets.com/feed");
                }
                else if (change_view_flag == 2){
                    rssUrl = new URL("https://www.analyticsvidhya.com/blog/category/big-data/feed/");
                }
                else{
                    rssUrl = new URL("https://yodalearning.com/feed");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;

            try{
                inputStream = rssUrl.openStream();
            } catch (IOException e){
                e.printStackTrace();
                Log.e("BRUNO", "Exception Inputstream", e);
            }

            // Creating instance of Feed Handler
            feedHandler fh = new feedHandler();

            try {
                Log.d("BRUNO", "saxParser()");
                saxParser.parse(inputStream, fh);
            } catch (IOException e) {
                Log.e("BRUNO", "IOException", e);
            } catch (SAXException e) {
                Log.e("BRUNO", "SAXException", e);
            } catch (Exception e) {
                Log.e("BRUNO", "Exception", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadListView();

            // Dismiss Progress Bar dialog
            this.dialog.dismiss();
        }
    }

    /**
     * SAX Parsing for Big Data RSS feeds
     */
    class feedHandler extends DefaultHandler {

        boolean InTitle, InLink, InPubDate, InDesc, InItem;
        StringBuilder stringBuilder;

        // Start a single object Big Data Article
        BigDataArticle article = new BigDataArticle();

        public feedHandler(){
            articleList = new ArrayList<BigDataArticle>();
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            stringBuilder = new StringBuilder(50);
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            // If starting an element...
            if(qName.equals("title") && InItem){
                InTitle = true;
            } else if(qName.equals("link") && InItem){
                InLink = true;
            } else if(qName.equals("pubDate") && InItem){
                InPubDate = true;
            } else if(qName.equals("description") && InItem){
                InDesc = true;
            } else if(qName.equals("item")){
                InItem = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            // If ending an element...
            if(qName.equals("title") && InItem){
                InTitle = false;
                article.title = stringBuilder.toString();
                stringBuilder.setLength(0);
            }
            else if(qName.equals("link") && InItem){
                InLink = false;
                article.link = stringBuilder.toString();
                stringBuilder.setLength(0);
            }
            else if(qName.equals("pubDate") && InItem){
                InPubDate = false;
                article.pubDate = stringBuilder.toString();
                stringBuilder.setLength(0);
            }
            else if(qName.equals("description") && InItem){ // Description is the last element of an item... so sets Item to false as well
                InDesc = false;
                InItem = false;
                article.description = stringBuilder.toString();
                stringBuilder.setLength(0);

                // Description is the last element, so add article object to the article list
                articleList.add(article);

                // "Refresh" current Big Data Article object (article)
                article = new BigDataArticle();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if(InTitle && InItem){
                stringBuilder.append(ch, start, length);
            }if(InLink && InItem){
                stringBuilder.append(ch, start, length);
            }if(InPubDate && InItem){
                stringBuilder.append(ch, start, length);
            }if(InDesc && InItem){
                stringBuilder.append(ch, start, length);
            }
        }
    }

    //========================================
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
    //========================================
}