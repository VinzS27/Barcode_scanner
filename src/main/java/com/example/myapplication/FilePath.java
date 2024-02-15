package com.example.myapplication;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class FilePath {
    public static String getFilePath(Context context, Uri uri){
        boolean isKitKat= Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT;

        if(isKitKat && DocumentsContract.isDocumentUri(context,uri)){
            if (isExternalStorageDocument(uri)) {

                String docId=DocumentsContract.getDocumentId(uri);
                String[] split=docId.split(":");
                String type=split[0];

                if("primary".equalsIgnoreCase(type)){
                    return Environment.getExternalStorageDirectory()+"/"+split[1];
                }
            }
            else if(isDownloadsDocument(uri)){
                String id=DocumentsContract.getDocumentId(uri);
                Uri contenturi= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(id));

                return getDataColumn(context,contenturi,null,null);
            }

        }
        else if("file".equalsIgnoreCase(uri.getScheme())){
            return uri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionargs){
        Cursor cursor=null;
        final String column="_data";
        final String[] projection={column};
        try {
            cursor=context.getContentResolver().query(uri,projection,selection,selectionargs,null);
            if(cursor!=null && cursor.moveToFirst()){
                int index=cursor.getColumnIndexOrThrow(column);
                return  cursor.getString(index);
            }
        }
        finally {
            if(cursor!=null){
                cursor.close();
            }
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri){
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri){
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}
