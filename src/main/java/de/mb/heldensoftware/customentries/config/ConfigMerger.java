package de.mb.heldensoftware.customentries.config;

import java.util.*;

public class ConfigMerger {
    public static class ConfigMergeResult {
        public Config config = new Config();
        public ArrayList<String> duplicates = new ArrayList<>();
    }

    public static ConfigMergeResult mergeAll(List<Config> configs) {
        ConfigMerger m = new ConfigMerger();
        for (Config config : configs) {
            m.merge(config);
        }
        return m.getResult();
    }

    private static class Entity {
        String what;
        String name;
        Object obj;
        int count = 0;
        HashSet<String> sources = new HashSet<>();
        boolean differences = false;

        public Entity(String what, String name, Object obj) {
            this.what = what;
            this.name = name;
            this.obj = obj;
        }
    }

    private final HashMap<String, Entity> entities = new HashMap<>();
    private final Config config = new Config();

    private Entity getEntity(String what, String name, Object obj) {
        String key = what + "|" + name;
        if (entities.containsKey(key)) {
            return entities.get(key);
        }
        Entity entity = new Entity(what, name, obj);
        entities.put(key, entity);
        return entity;
    }

    private boolean addEntity(String what, String name, Object obj, String source) {
        Entity entity = getEntity(what, name, obj);
        entity.count += 1;
        entity.sources.add(source);
        if (entity.count > 1 && !entity.obj.equals(obj)) {
            entity.differences = true;
        }
        return entity.count == 1;
    }

    public void merge(Config newConfig) {
        for (ZauberConfig z : newConfig.zauber) {
            if (addEntity("Zauber", z.name, z, newConfig.source))
                config.zauber.add(z);
        }
        for (MyranorZauberConfig z : newConfig.myranor_zauber) {
            if (addEntity("Myranor-Zauber", z.name, z, newConfig.source))
                config.myranor_zauber.add(z);
        }
        for (SonderfertigkeitConfig sf : newConfig.sonderfertigkeiten) {
            if (addEntity("Sonderfertigkeit", sf.name, sf, newConfig.source))
                config.sonderfertigkeiten.add(sf);
        }
        for (RepraesentationConfig r : newConfig.repraesentationen) {
            if (addEntity("Repräsentation", r.name, r, newConfig.source))
                config.repraesentationen.add(r);
        }
        for (MerkmalConfig m : newConfig.merkmale) {
            if (addEntity("Merkmal", m.name, m, newConfig.source))
                config.merkmale.add(m);
        }
    }

    public ConfigMergeResult getResult() {
        ConfigMergeResult r = new ConfigMergeResult();
        r.config = config;
        for (Entity entity : entities.values()) {
            if (entity.count > 1) {
                if (entity.differences) {
                    String msg = "Unterschiedliche Definitionen für " + entity.what + " " + entity.name +
                            " (in " + String.join(", ", entity.sources) + ")!";
                    System.err.println("[CustomEntryLoader] " + msg);
                    r.duplicates.add(msg);
                } else {
                    System.err.println("[CustomEntryLoader] " + entity.what + " " + entity.name + " mehrfach definiert " +
                            "(in " + String.join(", ", entity.sources) + ")!");
                }
            }
        }
        return r;
    }

}
