package it.swim.traffic.model;

import recon.Form;
import recon.Record;
import recon.Value;
import swim.util.Murmur3;

public final class SignalPhaseEvent {
  public long time;
  public int state;

  public SignalPhaseEvent(long time, int state) {
    this.time = time;
    this.state = state;
  }

  public SignalPhaseEvent() {
    this(0L, 0);
  }

  public void update(long time, SignalPhaseTensor current, SignalPhaseTensor future) {
    final int currentState = (int) Math.round(current.red);
    final int futureState = (int) Math.round(future.red);
    if (this.time == 0L && currentState != futureState && Math.abs(future.red - current.red) > 0.4) {
      this.time = time;
      this.state = futureState;
    }
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public SignalPhaseEvent clone() {
    return new SignalPhaseEvent(time, state);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof SignalPhaseEvent) {
      final SignalPhaseEvent that = (SignalPhaseEvent)other;
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
    return "new" + ' ' + "SignalPhaseEvent" + '(' + time + "L, " + state + ')';
  }

  public static final Form<SignalPhaseEvent> FORM = new SignalPhaseEventForm();
}

final class SignalPhaseEventForm extends Form<SignalPhaseEvent> {
  @Override
  public Class<?> getType() {
    return SignalPhaseEvent.class;
  }

  @Override
  public SignalPhaseEvent getDefault() {
    return new SignalPhaseEvent();
  }

  @Override
  public Value mold(SignalPhaseEvent event) {
    return Record.empty(2)
        .slot("clk", event.time)
        .slot("st", event.state);
  }

  @Override
  public SignalPhaseEvent cast(Value value) {
    final long time = value.get("clk").longValue(0L);
    final int state = value.get("st").intValue(0);
    if (time != 0L && state != 0) {
      return new SignalPhaseEvent(time, state);
    } else {
      return null;
    }
  }
}
