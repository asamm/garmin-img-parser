package org.free.garminimg.utils;
/*
 * Created by menion on 05/04/2018.
 * This code is part of Locus project from Asamm Software, s. r. o.
 * Copyright (C) 2018
 */

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class Utils {

    public static byte[] copyOfRange(byte[] original, int from, int to) {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0,
                Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Reads binary data from file.
     * @param file file to load from
     * @return binary data
     */
    public static synchronized byte[] loadBytes(File file) {
        // check file
        if (!exists(file)) {
            return null;
        }

        // prepare stream and load data
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            return toByteArray(is);
        } catch (Exception e) {
            Timber.e(e, "loadBytes(" + file.getAbsolutePath() + ")");
        } finally {
            closeQuietly(is);
        }
        return null;
    }
    /**
     * Test if certain file exists.
     * @param file file to test
     * @return {@code true} if exists
     */
    public static boolean exists(File file) {
        try {
            // create and test path
            return file != null && file.exists();
        } catch (Exception e) {
            Timber.e(e, "exists(" + file + ")");
            return false;
        }
    }

    /**
     * Read data from existing input stream into byte array.
     * @param is input stream
     * @return loaded bytes
     * @throws IOException IO operation exception
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        try{
            // finally copy
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            copy(is, baos);
            byte[] result = baos.toByteArray();
            return baos.toByteArray();
        } finally{
            closeQuietly(is);
        }
    }

    /**
     * Copy all data from first stream to second. Streams are not closed after use.
     * @param in input stream with data
     * @param out output stream
     * @return number of bytes copied
     * @throws IOException IO operation exception
     */
    public static long copy(InputStream in, OutputStream out) throws IOException {
        int n;
        long count = 0;
        byte[] buffer = new byte[1024 * 32];
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
            count += n;
        }

        // flush output stream if finished correctly
        out.flush();

        // return number of wrote bytes
        return count;
    }

    /**
     * Closes the closeable stream while ignoring any errors.
     * @param closeable closeable item to be closed.
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            Timber.e(e, "closeQuietly(" + closeable + ")");
        }
    }

    /**
     * Parse integer value from text.
     * @param data text to parse
     * @param defValue default value used in case, text is not an integer
     * @return parsed value
     */
    public static int parseInt(String data, int defValue) {
        try {
            return Integer.parseInt(data.trim());
        } catch (Exception e) {
            return defValue;
        }
    }

    // COLORS

    /**
     * Create transparent color.
     * @param c base color
     * @param alpha component [0..255] of the color
     * @return generated color with transparency
     */
    public static int getColorTransparent(int c, int alpha) {
        return Color.argb(alpha, red(c), green(c), blue(c));
    }

    /**
     * Return the red component of a color int. This is the same as saying
     * (color >> 16) & 0xFF
     */
    public static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Return the green component of a color int. This is the same as saying
     * (color >> 8) & 0xFF
     */
    public static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Return the blue component of a color int. This is the same as saying
     * color & 0xFF
     */
    public static int blue(int color) {
        return color & 0xFF;
    }

    // GRAPHICS

    public static Bitmap resize(Bitmap draw, int newWidth) {
        if (draw == null) {
            return null;
        }

        return resize(draw, newWidth,
                newWidth * draw.getHeight() / draw.getWidth());
    }

    public static Bitmap resize(Bitmap draw, int newWidth, int newHeight) {
        if (draw == null || newWidth <= 0 || draw.getWidth() == newWidth) {
            return draw;
        }

        return Bitmap.createScaledBitmap(draw, newWidth, newHeight, true);
    }
}
