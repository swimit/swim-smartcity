package it.swim.traffic.model;

import recon.Form;
import recon.Item;
import recon.Number;
import recon.Record;
import recon.Value;
import swim.util.HashTrieMap;
import swim.util.Murmur3;

public class IntersectionEvent {
  public HashTrieMap<Integer, SignalPhaseEvent> signalPhases;
  public HashTrieMap<Integer, VehicleDetectorEvent> vehicleDetectors;

  public IntersectionEvent(HashTrieMap<Integer, SignalPhaseEvent> signalPhases,
      HashTrieMap<Integer, VehicleDetectorEvent> vehicleDetectors) {
    this.signalPhases = signalPhases;
    this.vehicleDetectors = vehicleDetectors;
  }

  public IntersectionEvent() {
    this(HashTrieMap.empty(), HashTrieMap.empty());
  }

  public SignalPhaseEvent signalPhase(int id) {
    SignalPhaseEvent event = signalPhases.get(id);
    if (event == null) {
      event = new SignalPhaseEvent();
      signalPhases = signalPhases.updated(id, event);
    }
    return event;
  }

  public VehicleDetectorEvent vehicleDetector(int id) {
    VehicleDetectorEvent event = vehicleDetectors.get(id);
    if (event == null) {
      event = new VehicleDetectorEvent();
      vehicleDetectors = vehicleDetectors.updated(id, event);
    }
    return event;
  }

  public void update(long time, IntersectionTensor current, IntersectionTensor future) {
    for (HashTrieMap.Entry<Integer, SignalPhaseTensor> entry : current.signalPhases) {
      final SignalPhaseEvent event = signalPhase(entry.getKey());
      event.update(time, entry.getValue(), future.signalPhase(entry.getKey()));
    }
    for (HashTrieMap.Entry<Integer, VehicleDetectorTensor> entry : current.vehicleDetectors) {
      final VehicleDetectorEvent event = vehicleDetector(entry.getKey());
      event.update(time, entry.getValue(), future.vehicleDetector(entry.getKey()));
    }
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public IntersectionEvent clone() {
    HashTrieMap<Integer, SignalPhaseEvent> signalPhases = HashTrieMap.empty();
    for (HashTrieMap.Entry<Integer, SignalPhaseEvent> entry : this.signalPhases) {
      signalPhases = signalPhases.updated(entry.getKey(), entry.getValue().clone());
    }
    HashTrieMap<Integer, VehicleDetectorEvent> vehicleDetectors = HashTrieMap.empty();
    for (HashTrieMap.Entry<Integer, VehicleDetectorEvent> entry : this.vehicleDetectors) {
      vehicleDetectors = vehicleDetectors.updated(entry.getKey(), entry.getValue().clone());
    }
    return new IntersectionEvent(signalPhases, vehicleDetectors);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof IntersectionEvent) {
      final IntersectionEvent that = (IntersectionEvent)other;
      return signalPhases.equals(that.signalPhases)
          && vehicleDetectors.equals(that.vehicleDetectors);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Murmur3.mash(Murmur3.mix(Murmur3.mix(0xE3A15A3B,
      signalPhases.hashCode()), vehicleDetectors.hashCode()));
  }

  @Override
  public String toString() {
    return "new" + ' ' + "IntersectionEvent" + '('
        + signalPhases + ", " + vehicleDetectors + ')';
  }

  public static final Form<IntersectionEvent> FORM = new IntersectionEventForm();
}

final class IntersectionEventForm extends Form<IntersectionEvent> {
  @Override
  public Class<?> getType() {
    return IntersectionEvent.class;
  }

  @Override
  public Value mold(IntersectionEvent event) {
    final Record signalPhases = Record.of();
    for (HashTrieMap.Entry<Integer, SignalPhaseEvent> entry : event.signalPhases) {
      signalPhases.slot(Number.valueOf(entry.getKey()), entry.getValue().toValue());
    }

    final Record vehicleDetectors = Record.of();
    for (HashTrieMap.Entry<Integer, VehicleDetectorEvent> entry : event.vehicleDetectors) {
      vehicleDetectors.slot(Number.valueOf(entry.getKey()), entry.getValue().toValue());
    }

    return Record.empty(2)
        .slot("signalPhases", signalPhases)
        .slot("vehicleDetectors", vehicleDetectors);
  }

  @Override
  public IntersectionEvent cast(Value value) {
    HashTrieMap<Integer, SignalPhaseEvent> signalPhases = HashTrieMap.empty();
    for (Item item : value.get("signalPhases")) {
      final int id = item.getKey().intValue();
      final SignalPhaseEvent event = item.getValue().coerce(SignalPhaseEvent.FORM);
      signalPhases = signalPhases.updated(id, event);
    }

    HashTrieMap<Integer, VehicleDetectorEvent> vehicleDetectors = HashTrieMap.empty();
    for (Item item : value.get("vehicleDetectors")) {
      final int id = item.getKey().intValue();
      final VehicleDetectorEvent event = item.getValue().coerce(VehicleDetectorEvent.FORM);
      vehicleDetectors = vehicleDetectors.updated(id, event);
    }
    return new IntersectionEvent(signalPhases, vehicleDetectors);
  }
}
