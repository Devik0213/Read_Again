package kim.harry.com.readfav;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by naver on 16. 1. 10..
 */
public class ArticleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    ImageView state;
    ImageView thumbnail;
    TextView title;
    TextView description;
    private final Button otherBrowser;
    private ClickEventListener listener;

    public ArticleItemViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView.findViewById(R.id.article_tumbnail);
        thumbnail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongClick(v);
                return true;
            }
        });
        state = (ImageView) itemView.findViewById(R.id.article_state);
        title = (TextView) itemView.findViewById(R.id.article_title);
        description = (TextView) itemView.findViewById(R.id.article_decs);
        otherBrowser = (Button) itemView.findViewById(R.id.article_out);
        otherBrowser.setOnClickListener(this);

    }
    public void setEventListener(ClickEventListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.article_out :
                listener.onClick(v);
                break;
        }
    }

    interface ClickEventListener {
        void onClick(View v);
        void onLongClick(View v);
    }
}


