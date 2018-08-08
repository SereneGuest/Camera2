package com.smewise.camera2.utils;

import android.content.Context;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.smewise.camera2.Config;
import com.smewise.camera2.data.CamListPreference;
import com.smewise.camera2.data.CamMenuPreference;
import com.smewise.camera2.data.PreferenceGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by wenzhe on 11/27/17.
 */

public class XmlInflater {
    private static final String TAG = Config.getTag(XmlInflater.class);
    private static final Class<?>[] CTOR_SIGNATURE =
            new Class[] {Context.class, AttributeSet.class};
    private static final ArrayMap<String, Constructor<?>> sConstructorMap = new ArrayMap<>();
    private Context mContext;

    public XmlInflater(Context context) {
        mContext = context;
    }

    private CamListPreference getInstance(String tagName, Object[] args) {
        Constructor<?> constructor = sConstructorMap.get(tagName);
        if (constructor == null) {
            try {
                Class<?> clazz = mContext.getClassLoader().loadClass(tagName);
                constructor = clazz.getConstructor(CTOR_SIGNATURE);
                sConstructorMap.put(tagName, constructor);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        try {
            assert constructor != null;
            return (CamListPreference) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "init preference error");
        return null;
    }

    public PreferenceGroup inflate(int resId) {
        XmlPullParser parser = mContext.getResources().getXml(resId);
        AttributeSet attrs = Xml.asAttributeSet(parser);
        Object[] args = new Object[]{mContext, attrs};
        PreferenceGroup preferenceGroup = new PreferenceGroup();
        try {
            for (int type = parser.next();
                    type != XmlPullParser.END_DOCUMENT; type = parser.next()) {
                if (type != XmlPullParser.START_TAG) continue;
                int depth = parser.getDepth();
                if (depth > 1) {
                    CamListPreference pref = getInstance(parser.getName(), args);
                    preferenceGroup.add(pref);
                }
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return preferenceGroup;
    }
}
