package com.zahraEbra.osmdroid;

import java.io.File;
import java.io.InputStream;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.modules.CantContinueException;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;


import android.graphics.drawable.Drawable;


public class MBTileModuleProvider extends MapTileFileStorageProviderBase {


    protected MBTileSource tileSource;
    public static final int TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE = 40;

    public static final int NUMBER_OF_TILE_FILESYSTEM_THREADS = 8;
    /**
     * Constructor
     *
     * @param receiverRegistrar
     * @param file
     * @param tileSource
     */
    public MBTileModuleProvider(IRegisterReceiver receiverRegistrar,
                                File file,
                                MBTileSource tileSource) {

        // Call the super constructor
        super(receiverRegistrar,
                NUMBER_OF_TILE_FILESYSTEM_THREADS,
                TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

        // Initialize fields
        this.tileSource = tileSource;

    }

    @Override
    protected String getName() {
        return "MBTiles File Archive Provider";
    }

    @Override
    protected String getThreadGroupName() {
        return "mbtilesarchive";
    }

    @Override
    public MapTileModuleProviderBase.TileLoader getTileLoader() {
        return new TileLoader();
    }

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    public int getMinimumZoomLevel() {
        return tileSource.getMinimumZoomLevel();
    }

    @Override
    public int getMaximumZoomLevel() {
        return tileSource.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        if (tileSource instanceof MBTileSource) {
            this.tileSource = (MBTileSource) tileSource;
        }
    }

    private class TileLoader extends MapTileModuleProviderBase.TileLoader{

        @Override
        public Drawable loadTile(long pMapTileIndex) throws CantContinueException {
            InputStream inputStream = null;
            try {
                inputStream = tileSource.getInputStream(pMapTileIndex);

                if (inputStream != null) {

                    // Note that the finally clause will be called before
                    // the value is returned!
                    return tileSource.getDrawable(inputStream);
                }

            } catch (Throwable e) {

            } finally {
                if (inputStream != null) {
                    StreamUtils.closeStream(inputStream);
                }
            }

            return null;        }
    }

}
