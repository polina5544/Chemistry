package storage;

import domain.Measurement;
import domain.Protocol;
import domain.Sample;
import domain.User;

import java.util.HashSet;
import java.util.Set;

//это контейнер, который позволяет вернуть несколько коллекций из метода load
// передаю данные из хмл

public record StorageData(Set<Sample> samples,
                       Set<Measurement> measurements,
                       Set<Protocol> protocols,
                       Set<User> users) {
}