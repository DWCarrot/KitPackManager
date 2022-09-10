package io.github.dwcarrot.kitpackmgr.storage;

import java.util.Collection;
import java.util.UUID;

public class KitPackAll extends KitPack {

    public static final UUID QUERY = UUID.randomUUID();

    public final Collection<KitPack> kitPacks;

    public KitPackAll(Collection<KitPack> kitPacks) {
        super();
        this.kitPacks = kitPacks;
    }
}
