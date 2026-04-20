package utilits;

import domain.Measurement;
import domain.Protocol;
import domain.Sample;

import java.util.Set;

public class IDgenerator {

    private static long currentId = 1;

    // получить следующий id
    public static long nextId() {
        return currentId++;
    }

    // 🔥 универсальное обновление по всем сущностям
    public static void updateAll(Set<Sample> samples,
                                 Set<Measurement> measurements,
                                 Set<Protocol> protocols) {

        long maxId = 0;

        // samples
        for (Sample s : samples) {
            if (s.getId() > maxId) {
                maxId = s.getId();
            }
        }

        // measurements
        for (Measurement m : measurements) {
            if (m.getId() > maxId) {
                maxId = m.getId();
            }
        }

        // protocols
        for (Protocol p : protocols) {
            if (p.getId() > maxId) {
                maxId = p.getId();
            }
        }

        // обновляем
        if (maxId >= currentId) {
            currentId = maxId + 1;
        }
    }
}