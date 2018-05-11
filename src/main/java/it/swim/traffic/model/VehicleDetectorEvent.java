package it.swim.traffic.model;

import recon.Form;
import recon.Record;
import recon.Value;
import swim.util.Murmur3;

public final class VehicleDetectorEvent {
  public long time;
  public int state;

  public VehicleDetectorEvent(long time, int state) {
    this.time = time;
    this.state = state;
  }

  public VehicleDetectorEvent() {
    this(0L, 0);
  }

  public void update(long time, VehicleDetectorTensor current, VehicleDetectorTensor future) {
    final int currentState = (int) Math.round(current.occupancy);
    final int futureState = (int) Math.round(future.occupancy);
    if (this.time == 0L && currentState != futureState && Math.abs(future.occupancy - current.occupancy) > 0.4) {
      this.time = time;
      this.state = futureState;
    }
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public VehicleDetectorEvent clone() {
    return new VehicleDetectorEvent(time, state);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof VehicleDetectorEvent) {
      final VehicleDetectorEvent that = (VehicleDetectorEvent)other;
      return time == that.time && state == that.state;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Murmur3.mash(Murmur3.mix(Murmur3.mix(0x7A3554D3,
        Murmur3.hash(time)), Murmur3.hash(state)));
  }

  @Override
  public String toString() {
    return "new" + ' ' + "VehicleDetectorEvent" + '(' + time + "L, " + state + ')';
  }

  public static final Form<VehicleDetectorEvent> FORM = new VehicleDetectorEventForm();
}

final class VehicleDetectorEventForm extends Form<VehicleDetectorEvent> {
  @Override
  public Class<?> getType() {
    return VehicleDetectorEvent.class;
  }

  @Override
  public VehicleDetectorEvent getDefault() {
    return new VehicleDetectorEvent();
  }

  @Override
  public Value mold(VehicleDetectorEvent event) {
    return Record.empty(2)
        .slot("clk", event.time)
        .slot("st", event.state);
  }

  @Override
  public VehicleDetectorEvent cast(Value value) {
    final long time = value.get("clk").longValue(0L);
    final int state = value.get("st").intValue(0);
    if (time != 0L && state != 0) {
      return new VehicleDetectorEvent(time, state);
    } else {
      return null;
    }
  }
}
