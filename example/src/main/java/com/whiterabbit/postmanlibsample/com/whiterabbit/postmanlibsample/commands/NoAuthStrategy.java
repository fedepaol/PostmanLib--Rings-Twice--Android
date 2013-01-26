package com.whiterabbit.postmanlibsample.com.whiterabbit.postmanlibsample.commands;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import com.whiterabbit.postman.commands.RestServerStrategy;
import com.whiterabbit.postman.exceptions.ResultParseException;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: fedepaol
 * Date: 12/18/12
 * Time: 12:41 AM
 */
public class NoAuthStrategy implements RestServerStrategy {
    private static final String url = "http://www.cimonesci.it/cams/polle.jpg";

    public NoAuthStrategy(){

    }

    @Override
    public String getOAuthSigner() {
        return null;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Verb getVerb() {
        return Verb.GET;
    }

    @Override
    public void processHttpResult(Response result, Context context) throws ResultParseException {
        Bitmap b = BitmapFactory.decodeStream(result.getStream());
        File path = context.getExternalFilesDir(null);
        File target = new File(path, "picture.png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(target);
            b.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    @Override
    public void addParamsToRequest(OAuthRequest request) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }


    public static final Creator<NoAuthStrategy> CREATOR
            = new Creator<NoAuthStrategy>() {
        public NoAuthStrategy createFromParcel(Parcel in) {
            return new NoAuthStrategy(in);
        }

        public NoAuthStrategy[] newArray(int size) {
            return new NoAuthStrategy[size];
        }
    };


    public NoAuthStrategy(Parcel in){
    }

}
