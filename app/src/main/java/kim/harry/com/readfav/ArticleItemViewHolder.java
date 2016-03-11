package kim.harry.com.readfav;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by naver on 16. 1. 10..
 */
public class ArticleItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    ImageView state;
    ImageView thumbnail;
    TextView title;
    TextView description;
    private ClickEventListener listener;

    public ArticleItemViewHolder(View itemView) {
        super(itemView);
        thumbnail = (ImageView) itemView.findViewById(R.id.article_tumbnail);
        title = (TextView) itemView.findViewById(R.id.article_title);
        description = (TextView) itemView.findViewById(R.id.article_decs);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }
    public void setEventListener(ClickEventListener listener){
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v);
    }

    @Override
    public boolean onLongClick(View v) {
        listener.onLongClick(v);
        return true;
    }

    interface ClickEventListener {
        void onClick(View v);
        void onLongClick(View v);
    }
}


