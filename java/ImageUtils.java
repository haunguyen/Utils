package vn.mclab.nursing.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import vn.mclab.nursing.base.App;
import vn.mclab.nursing.base.ISavePicture;

/**
 * Created by developer_hau on 6/5/17.
 */

public class ImageUtils {
    private ImageUtils() {

    }

    static Bitmap getScaledBitmap(Context context, Uri imageUri, float maxWidth, float maxHeight, Bitmap.Config bitmapConfig) {
        String filePath = FileUtil.getRealPathFromURI(context, imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
        if (bmp == null) {

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(filePath);
                BitmapFactory.decodeStream(inputStream, null, options);
                /*if (inputStream != null) {
                    inputStream.close();
                }*/
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
            } catch (IOException exception) {
                exception.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualWidth < 0 || actualHeight < 0) {
            Bitmap bitmap2 = BitmapFactory.decodeFile(filePath);
            actualWidth = bitmap2.getWidth();
            actualHeight = bitmap2.getHeight();
        }

        float imgRatio = (float) actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

        //width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

        //setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

        //inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

        //this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            //load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
            if (bmp == null) {

                InputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(filePath);
                    BitmapFactory.decodeStream(inputStream, null, options);
                    /*if (inputStream != null) {
                        inputStream.close();
                    }*/

                } catch (FileNotFoundException exception) {
                    exception.printStackTrace();
                } catch (IOException exception) {
                    exception.printStackTrace();
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        actualHeight = actualHeight<=0?1:actualHeight;
        actualWidth = actualWidth<=0?1:actualWidth;
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, bitmapConfig);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, 0, 0);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        //check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(),
                    matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scaledBitmap;
    }

    public static File compressImage(Context context, Uri imageUri, float maxWidth, float maxHeight,
                                     Bitmap.CompressFormat compressFormat, Bitmap.Config bitmapConfig, int maxSize, String parentPath, String prefix, String fileName) {
        FileOutputStream out = null;
        String filename = generateFilePath(context, parentPath, imageUri, compressFormat.name().toLowerCase(), prefix, fileName);
        try {
            out = new FileOutputStream(filename);

            //write the compressed bitmap at the destination specified by filename.
            int jpgQuality = 100;
            byte[] resultBitmap;
            Bitmap reducedBitmap = ImageUtils.getScaledBitmap(context, imageUri, maxWidth, maxHeight, bitmapConfig);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            reducedBitmap.compress(compressFormat, jpgQuality, outStream);
            resultBitmap = outStream.toByteArray();
            // Adjust Quality until file size is less than maxSize.
            while (resultBitmap.length > (maxSize * 1024)) {
                jpgQuality = jpgQuality - 5;
                if (jpgQuality < 0) {
                    break;
                }
                outStream.reset();
                reducedBitmap.compress(compressFormat, jpgQuality, outStream);
                resultBitmap = outStream.toByteArray();
                LogUtils.e("jpgQuality", "jpgQuality" + jpgQuality + "/////" + resultBitmap.length);

            }
            outStream.writeTo(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }

        return new File(filename);
    }

    public static String setProfileToLocalApp(Context context, File _file, Bitmap bitmapImage, int id, String oldLink) {

        if (oldLink != null) {
            if (oldLink.trim().length() > 0) {
                File file = new File(oldLink);
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        if (bitmapImage == null) {
            return "";
        }

        File myFile = compressImageLocalApp(id, context, Uri.fromFile(_file), 600, 600,
                Bitmap.CompressFormat.JPEG, Bitmap.Config.ARGB_8888, 100);

        LogUtils.e("TuanNM_new_link", myFile.getAbsolutePath());
        return myFile.getAbsolutePath();
    }

    public static File compressImageLocalApp(int id, Context context, Uri imageUri, float maxWidth, float maxHeight,
                                             Bitmap.CompressFormat compressFormat, Bitmap.Config bitmapConfig, int maxSize) {
        FileOutputStream out = null;
        ContextWrapper cw = new ContextWrapper(App.getAppContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("babyrepo", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, id + "_" + Long.toHexString(System.currentTimeMillis()) + ".jpg");
        String filename = mypath.getAbsolutePath();
        try {
            out = new FileOutputStream(filename);
            //write the compressed bitmap at the destination specified by filename.
            int jpgQuality = 100;
            byte[] resultBitmap;
            Bitmap reducedBitmap = ImageUtils.getScaledBitmap(context, imageUri, maxWidth, maxHeight, bitmapConfig);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            reducedBitmap.compress(compressFormat, jpgQuality, outStream);
            resultBitmap = outStream.toByteArray();
            // Adjust Quality until file size is less than maxSize.
            while (resultBitmap.length > (maxSize * 1024)) {
                jpgQuality = jpgQuality - 5;
                if (jpgQuality < 0) {
                    break;
                }
                outStream.reset();
                reducedBitmap.compress(compressFormat, jpgQuality, outStream);
                resultBitmap = outStream.toByteArray();
                LogUtils.e("jpgQuality", "jpgQuality" + jpgQuality + "/////" + resultBitmap.length);
            }
            outStream.writeTo(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }

        return new File(filename);
    }

    public static String setProfileToLocalAppBaby(final Context context, final String filePath, final int id, final String oldLink, final String tag, final boolean isPhotoToday, final ISavePicture iSavePicture) {
        if (filePath == null || (filePath != null && filePath.length() <= 0)) {
            if (iSavePicture != null) {
                iSavePicture.onSavePictureDone("");
            }
            return "";
        }
        final File _file = new File(filePath);
        /*if (isPhotoToday) {
            exifInterface = Utils.getExifInterface(context, Uri.fromFile(new File(filePath)));
        }*/
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... strings) {

                File myFile = compressImageLocalAppBaby(id, tag, context, Uri.fromFile(_file), 2000, 2000,
                        Bitmap.CompressFormat.JPEG, Bitmap.Config.ARGB_8888, 500, isPhotoToday);

                if (oldLink != null) {
                    if (oldLink.trim().length() > 0) {
                        File file = new File(oldLink);
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
                return myFile.getAbsolutePath();
            }

            @Override
            protected void onPostExecute(String s) {
                if (iSavePicture != null) {
                    if (s == null) {
                        s = "";
                    }
                    iSavePicture.onSavePictureDone(s);
                }
                super.onPostExecute(s);
            }
        }.execute("");

        return "";
    }

    public static File compressImageLocalAppBaby(int id, String tag, Context context, Uri imageUri, float maxWidth, float maxHeight,
                                                 Bitmap.CompressFormat compressFormat, Bitmap.Config bitmapConfig, int maxSize, boolean isPhotoToday) {
        FileOutputStream out = null;
        ContextWrapper cw = new ContextWrapper(App.getAppContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("babyrepo", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, tag + "_" + id + "_" + Long.toHexString(System.currentTimeMillis()) + ".jpg");
        String filename = mypath.getAbsolutePath();
        try {
            out = new FileOutputStream(filename);
            //write the compressed bitmap at the destination specified by filename.
            int jpgQuality = 100;
            byte[] resultBitmap;
            Bitmap reducedBitmap = ImageUtils.getScaledBitmap(context, imageUri, maxWidth, maxHeight, bitmapConfig);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            reducedBitmap.compress(compressFormat, jpgQuality, outStream);
            resultBitmap = outStream.toByteArray();
            // Adjust Quality until file size is less than maxSize.
            while (resultBitmap.length > (maxSize * 1024)) {
                jpgQuality = jpgQuality - 5;
                if (jpgQuality < 0) {
                    break;
                }
                outStream.reset();
                reducedBitmap.compress(compressFormat, jpgQuality, outStream);
                resultBitmap = outStream.toByteArray();
                LogUtils.e("jpgQuality", "jpgQuality" + jpgQuality + "/////" + resultBitmap.length);
            }
            outStream.writeTo(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }
        File f = new File(filename);
        if (isPhotoToday) {
            setExif(context, f, imageUri);
        }
        return f;
    }

    private static void setExif(Context context, File f, Uri imageUri) {
        android.support.media.ExifInterface exifInterface = null;
        exifInterface = Utils.getExifInterface(context, imageUri);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (exifInterface != null) {
            if (exifInterface.getDateTime() != -1) {
                try {
                    android.support.media.ExifInterface exif = new android.support.media.ExifInterface(f.getAbsolutePath());
                    exif.setAttribute(ExifInterface.TAG_DATETIME, sdf.format(new Date(exifInterface.getDateTime())));
                    exif.saveAttributes();
                } catch (IOException e) {
                }
            } else if ((new File(imageUri.getPath())).lastModified() > 0) {
                try {
                    android.support.media.ExifInterface exif = new android.support.media.ExifInterface(f.getAbsolutePath());
                    exif.setAttribute(ExifInterface.TAG_DATETIME, sdf.format((new File(imageUri.getPath())).lastModified()));
                    exif.saveAttributes();
                } catch (IOException e) {
                }

            } else {
                try {
                    android.support.media.ExifInterface exif = new android.support.media.ExifInterface(f.getAbsolutePath());
                    exif.setAttribute(ExifInterface.TAG_DATETIME, sdf.format(new Date()));
                    exif.saveAttributes();
                } catch (IOException e) {
                }
            }
        } else {
            try {
                android.support.media.ExifInterface exif = new android.support.media.ExifInterface(f.getAbsolutePath());
                exif.setAttribute(ExifInterface.TAG_DATETIME, sdf.format(new Date()));
                exif.saveAttributes();
            } catch (IOException e) {
            }
        }
    }

    private static String generateFilePath(Context context, String parentPath, Uri uri,
                                           String extension, String prefix, String fileName) {
        File file = new File(context.getCacheDir(), "folder");
//        File file = new File(parentPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        /** if prefix is null, set prefix "" */
        prefix = TextUtils.isEmpty(prefix) ? "" : prefix;
        /** reset fileName by prefix and custom file name */
//        fileName = TextUtils.isEmpty(fileName) ? prefix + FileUtil.splitFileName(FileUtil.getFileName(context, uri))[0] : fileName;
        fileName = prefix + "chat";
        return file.getAbsolutePath() + File.separator + fileName + "." + extension;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

}
