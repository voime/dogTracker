package com.voime.koeraradar;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;

public class DogOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
    Context mContext;

    public DogOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        mContext = context;
    }

    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }

    public void removeItem(int i) {
        mOverlays.remove(i);
        populate();
    }

    @Override
    public int size() {
        return mOverlays.size();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = mOverlays.get(index);
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return true;
    }
}
