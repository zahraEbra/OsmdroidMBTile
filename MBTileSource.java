package com.zahraEbra.osmdroid;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.util.MapTileIndex;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class MBTileSource extends BitmapTileSourceBase {

    // Database related fields
    public final static String TABLE_TILES = "tiles";
    public final static String COL_TILES_ZOOM_LEVEL = "zoom_level";
    public final static String COL_TILES_TILE_COLUMN = "tile_column";
    public final static String COL_TILES_TILE_ROW = "tile_row";
    public final static String COL_TILES_TILE_DATA = "tile_data";

    protected SQLiteDatabase database;
    protected File archive;

    // Reasonable defaults ..
    public static final int minZoom = 8;
    public static final int maxZoom = 15;
    public static final int tileSizePixels = 256;

    // Required for the superclass
//    public static final String resourceId = ResourceProxy.string.offline_mode;

    /**
     * The reason this constructor is protected is because all parameters,
     * except file should be determined from the archive file. Therefore a
     * factory method is necessary.
     *
     * @param minZoom
     * @param maxZoom
     * @param tileSizePixels
     * @param file
     */
    protected MBTileSource(int minZoom,
                           int maxZoom,
                           int tileSizePixels,
                           File file,
                           SQLiteDatabase db) {
        super("MBTiles", minZoom, maxZoom, tileSizePixels, ".png");

        archive = file;
        database = db;
    }

    /**
     * Creates a new MBTileSource from file.
     * <p>
     * Parameters minZoom, maxZoom en tileSizePixels are obtained from the
     * database. If they cannot be obtained from the DB, the default values as
     * defined by this class are used.
     *
     * @param file
     * @return
     */
    public static MBTileSource createFromFile(File file) {
        SQLiteDatabase db;
        int flags = SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READONLY;

        int value;
        int minZoomLevel;
        int maxZoomLevel;
        int tileSize = tileSizePixels;
        InputStream is;

        // Open the database
        db = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, flags);

        // Get the minimum zoomlevel from the MBTiles file
        value = getInt(db, "SELECT MIN(zoom_level) FROM tiles;");
        minZoomLevel = value > -1 ? value : minZoom;

        // Get the maximum zoomlevel from the MBTiles file
        value = getInt(db, "SELECT MAX(zoom_level) FROM tiles;");
        maxZoomLevel = value > -1 ? value : maxZoom;

        // Get the tile size
        Cursor cursor = db.rawQuery("SELECT tile_data FROM tiles LIMIT 0,1",
                new String[]{});

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            is = new ByteArrayInputStream(cursor.getBlob(0));

            Bitmap bitmap = BitmapFactory.decodeStream(is);
            tileSize = bitmap.getHeight();
        }

        cursor.close();
        return new MBTileSource(minZoomLevel, maxZoomLevel, tileSize, file, db);
    }

    protected static int getInt(SQLiteDatabase db, String sql) {
        Cursor cursor = db.rawQuery(sql, new String[]{});
        int value = -1;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            value = cursor.getInt(0);
        }

        cursor.close();
        return value;
    }

    public InputStream getInputStream(Long pMapTileIndex) {

        try {
            InputStream ret = null;
            final String[] tile = {COL_TILES_TILE_DATA};
            final String[] xyz = {Integer.toString(MapTileIndex.getX(pMapTileIndex)),  
                    Double.toString(Math.pow(2, MapTileIndex.getZoom(pMapTileIndex)) - MapTileIndex.getY(pMapTileIndex) - 1),  
                    Integer.toString(MapTileIndex.getZoom(pMapTileIndex))};  
            final Cursor cur = database.query(TABLE_TILES,
                    tile,
                    "tile_column=? and tile_row=? and zoom_level=?",
                    xyz,
                    null,
                    null,
                    null);

            if (cur.getCount() != 0) {
                cur.moveToFirst();
                ret = new ByteArrayInputStream(cur.getBlob(0));
            }

            cur.close();

            if (ret != null) {
                return ret;
            }

        } catch (final Throwable e) {
        }

        return null;

    }

}
