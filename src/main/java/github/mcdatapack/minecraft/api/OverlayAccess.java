package github.mcdatapack.minecraft.api;

import java.io.File;

public interface OverlayAccess {
    void registerOverlay(int version);

    int minVersion();
    int maxVersion();
    File resourceDict();
}