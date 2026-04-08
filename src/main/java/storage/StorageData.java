package storage;

import domain.Measurement;
import domain.Protocol;
import domain.Sample;
import java.util.Set;

//это контейнер, который позволяет вернуть несколько коллекций из метода load

public class StorageData {
        private final Set<Sample> samples;
        private final Set<Measurement> measurements;
        private final Set<Protocol> protocols;

        //передаю данные из xml
        public StorageData(Set<Sample> samples,
                           Set<Measurement> measurements,
                           Set<Protocol> protocols) {
            this.samples = samples;
            this.measurements = measurements;
            this.protocols = protocols;
        }

        public Set<Sample> getSamples() {
            return samples;
        }

        public Set<Measurement> getMeasurements() {
            return measurements;
        }

        public Set<Protocol> getProtocols() {
            return protocols;
        }
    }