package kim.harry.com.readfav.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by naver on 16. 3. 10..
 */
public class ParcelArticle implements Parcelable {
    private String url;
    private String thumbnail;
    private String title;
    private String content;

    public ParcelArticle(Article article){
        this.url = article.getUrl();
        this.thumbnail = article.getThumbnail();
        this.title = article.getTitle();
        this.content = article.getContent();
    }

    protected ParcelArticle(Parcel in) {
        url = in.readString();
        thumbnail = in.readString();
        title = in.readString();
        content = in.readString();
    }

    public static final Creator<ParcelArticle> CREATOR = new Creator<ParcelArticle>() {
        @Override
        public ParcelArticle createFromParcel(Parcel in) {
            return new ParcelArticle(in);
        }

        @Override
        public ParcelArticle[] newArray(int size) {
            return new ParcelArticle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(thumbnail);
        dest.writeString(title);
        dest.writeString(content);
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUrl() {
        return url;
    }
}
