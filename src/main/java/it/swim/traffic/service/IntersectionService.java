package it.swim.traffic.service;

import swim.api.AbstractService;
import swim.api.EventDownlink;
import swim.api.MapLane;
import swim.api.SwimLane;
import swim.api.ValueLane;
import swim.concurrent.TimerRef;
import recon.Form;
import recon.Item;
import recon.Number;
import recon.Record;
import recon.Slot;
import recon.Value;
import it.swim.traffic.model.*;
import swim.util.HashTrieMap;
import swim.util.HashTrieSet;
import swim.util.Uri;

public class IntersectionService extends AbstractService {
  long lastScanTime;
  TimerRef sampleTimer;
  IntersectionTensor intersectionTensor;
  HashTrieMap<Integer, Long> lastSignalPhaseEvent = HashTrieMap.empty();
  HashTrieMap<Integer, SignalPhaseModel> signalPhaseModels = HashTrieMap.empty();
  EventDownlink<Value> infoLink;
  EventDownlink<Value> schematicLink;
  EventDownlink<Value> scanLink;
  EventDownlink<Value> latencyLink;

  // 4 minute window; 1 second samples
  static final Long SAMPLE_WINDOW = 1000L;
  static final int SAMPLE_COUNT = 240; // MUST BE EVEN

  @SwimLane("intersection/info")
  public ValueLane<Value> info = valueLane()
      .isResident(true);

  @SwimLane("intersection/schematic")
  public ValueLane<Value> schematic = valueLane()
      .isResident(true);

  @SwimLane("intersection/mode")
  public ValueLane<Value> mode = valueLane()
      .isResident(true);

  @SwimLane("intersection/latency")
  public ValueLane<Value> latency = valueLane()
      .isResident(true);

  @SwimLane("intersection/history")
  public MapLane<Long, IntersectionTensor> intersectionHistory = mapLane()
      .keyForm(Form.LONG)
      .valueForm(IntersectionTensor.FORM);

  @SwimLane("phase/state")
  public MapLane<Integer, Integer> signalPhaseState = mapLane()
      .keyForm(Form.INTEGER)
      .valueForm(Form.INTEGER)
      .isResident(true)
      .didUpdate(this::didUpdateSignalPhase);

  void didUpdateSignalPhase(Integer phaseId, Integer newPhase, Integer oldPhase) {
    //if (ENABLED.contains(nodeUri())) {
    //  System.out.println(nodeUri().toUri() + " didUpdateSignalPhase " + phaseId + ": " + newPhase);
    //}
    updateSignalPhaseTensor(phaseId, newPhase, oldPhase, System.currentTimeMillis());
  }

  void updateSignalPhaseTensor(Integer phaseId, Integer newPhase, Integer oldPhase, long t1) {
    final Long t0 = lastSignalPhaseEvent.get(phaseId);
    lastSignalPhaseEvent = lastSignalPhaseEvent.updated(phaseId, t1);
    if (t0 == null || oldPhase == null) {
      return;
    }
    final long dt = t1 - t0;
    final double dw = (double)dt / (double)SAMPLE_WINDOW;
    final SignalPhaseTensor signalTensor = intersectionTensor.signalPhase(phaseId);
    if (oldPhase == 1) {
      signalTensor.red = Math.min(signalTensor.red + dw, 1.0);
    } else if (oldPhase == 2) {
      signalTensor.yellow = Math.min(signalTensor.yellow + dw, 1.0);
    } else if (oldPhase == 3) {
      signalTensor.green = Math.min(signalTensor.green + dw, 1.0);
    }
  }

  @SwimLane("phase/event")
  public MapLane<Integer, SignalPhaseEvent> signalPhaseEvents = mapLane()
      .keyForm(Form.INTEGER)
      .valueForm(SignalPhaseEvent.FORM)
      .isResident(true);

  @SwimLane("detector/state")
  public MapLane<Integer, Integer> vehicleDetectorState = mapLane()
      .keyForm(Form.INTEGER)
      .valueForm(Form.INTEGER)
      .isResident(true);

  @SwimLane("detector/event")
  public MapLane<Integer, VehicleDetectorEvent> vehicleDetectorEvents = mapLane()
      .keyForm(Form.INTEGER)
      .valueForm(VehicleDetectorEvent.FORM)
      .isResident(true);

  @SwimLane("pedPhase/state")
  public MapLane<Integer, Integer> pedPhaseState = mapLane()
      .keyForm(Form.INTEGER)
      .valueForm(Form.INTEGER)
      .isResident(true);

  @SwimLane("pedCall/state")
  public MapLane<Integer, Integer> pedCallState = mapLane()
      .keyForm(Form.INTEGER)
      .valueForm(Form.INTEGER)
      .isResident(true)
      .didUpdate(this::didUpdatePedCall);

  void didUpdatePedCall(Integer phaseId, Integer newPhase, Integer oldPhase) {
    int st = 0;
    for (Integer state : pedCallState.values()) {
      st = Math.max(st, state);
    }
    //System.out.println(nodeUri().toUri() + " pedCall st: " + st);
    pedCall.set(st);
  }

  @SwimLane("pedCall")
  public ValueLane<Integer> pedCall = valueLane()
      .valueForm(Form.INTEGER)
      .isResident(true);

  void sampleIntersectionTensor() {
    try {
      final long t = System.currentTimeMillis();
      for (Integer phaseId : lastSignalPhaseEvent.keySet()) {
        final Integer state = signalPhaseState.get(phaseId);
        updateSignalPhaseTensor(phaseId, state, state, t);
      }
      //if (ENABLED.contains(nodeUri())) {
      //  System.out.println(nodeUri().toUri() + " intersection tensor: " + intersectionTensor.toValue().toRecon());
      //}
      intersectionHistory.put(t, intersectionTensor);
      try {
        while (intersectionHistory.size() > SAMPLE_COUNT + 1) {
          intersectionHistory.remove(intersectionHistory.getEntry(0).getKey());
        }
      } catch (Throwable cause) {
        cause.printStackTrace();
      }

      for (SignalPhaseTensor signalTensor : intersectionTensor.signalPhases.values()) {
        signalTensor.red = 0.0;
        signalTensor.yellow = 0.0;
        signalTensor.green = 0.0;
      }
      for (VehicleDetectorTensor vehicleTensor : intersectionTensor.vehicleDetectors.values()) {
        vehicleTensor.occupancy = 0.0;
        vehicleTensor.count = 0;
      }
    } catch (Throwable cause) {
      cause.printStackTrace();
    } finally {
      sampleTimer = setTimer(SAMPLE_WINDOW, this::sampleIntersectionTensor);
    }
  }

  void initIntersectionTensor() {
    intersectionTensor = new IntersectionTensor();
    for (Integer phaseId : signalPhaseState.keySet()) {
      intersectionTensor.signalPhase(phaseId);
    }
    for (Integer detectorId : vehicleDetectorState.keySet()) {
      intersectionTensor.vehicleDetector(detectorId);
    }
  }

  public void linkInfo() {
    if (infoLink == null) {
      infoLink = downlink()
          .hostUri(TRAFFIC_HOST_URI)
          .nodeUri(Uri.EMPTY.withPath(nodeUri().getPath()))
          .laneUri("info")
          .onEvent(this::didSetRemoteInfo)
          .keepSynced(true)
          .open();
    }
  }

  public void unlinkInfo() {
    if (infoLink != null) {
      infoLink.close();
      infoLink = null;
    }
  }

  void didSetRemoteInfo(Value newValue) {
    info.set(newValue);
  }

  public void linkSchematic() {
    if (schematicLink == null) {
      schematicLink = downlink()
          .hostUri(TRAFFIC_HOST_URI)
          .nodeUri(Uri.EMPTY.withPath(nodeUri().getPath()))
          .laneUri("schematic")
          .onEvent(this::didSetRemoteSchematic)
          .keepSynced(true)
          .open();
    }
  }

  public void unlinkSchematic() {
    if (schematicLink != null) {
      schematicLink.close();
      schematicLink = null;
    }
  }

  void didSetRemoteSchematic(Value newValue) {
    //System.out.println(nodeUri().toUri() + " didSetRemoteSchematic: " + newValue.toRecon());
    schematic.set(newValue);
  }

  public void linkScan() {
    if (scanLink == null) {
      scanLink = downlink()
          .hostUri(TRAFFIC_HOST_URI)
          .nodeUri(Uri.EMPTY.withPath(nodeUri().getPath()))
          .laneUri("scan/state")
          .onEvent(this::didUpdateRemoteScan)
          .open();
    }
  }

  public void unlinkScan() {
    if (scanLink != null) {
      scanLink.close();
      scanLink = null;
    }
  }

  void didUpdateRemoteScan(Value value) {
    if (value instanceof Record) {
      final Record state = (Record) value;
      final long clk = state.get("clk").longValue(0L);

      final Value st = state.get("st");

      final Value p = state.get("p");
      if (p.isDefined()) {
        didUpdateRemoteSignalPhase(p.intValue(), st.intValue(), clk);
      }

      final Value d = state.get("d");
      if (d.isDefined()) {
        didUpdateRemoteVehicleDetector(d.intValue(), st.intValue(), clk);
      }

      final Value pp = state.get("pp");
      if (pp.isDefined()) {
        didUpdateRemotePedPhase(pp.intValue(), st.intValue(), clk);
      }

      final Value pc = state.get("pc");
      if (pc.isDefined()) {
        didUpdateRemotePedCall(pc.intValue(), st.intValue(), clk);
      }

      final String coord = state.get("coord").stringValue("");
      if (!this.mode.get().get("coord").stringValue("").equals(coord)) {
        this.mode.set(Record.empty().withSlot("coord", coord));
      }
      lastScanTime = System.currentTimeMillis();
    }
  }

  static final HashTrieSet<Uri> ENABLED = HashTrieSet.of(Uri.parse("/intersection/US/CA/PaloAlto/24"));

  void didUpdateRemoteSignalPhase(int p, int st, long clk) {
    //System.out.println(nodeUri().toUri() + " didUpdateRemoteSignalPhase p: " + p + "; st: " + st);
    signalPhaseState.put(p, st);

    SignalPhaseModel model = signalPhaseModels.get(p);
    if (model == null) {
      model = new SignalPhaseModel(p);
      signalPhaseModels = signalPhaseModels.updated(p, model);
    }
    final boolean modelChanged = model.updateState(st, clk);
    if (modelChanged && model.hasCycled() && !ENABLED.contains(nodeUri())) {
      //System.out.println(nodeUri().toUri() + " didUpdateRemoteSignalPhase p: " + p + "; st: " + st + "; cycleTime: " + model.cycleTime() + "ms; cycleDeviation: " + model.cycleDeviation());
      if (model.isPredictable()) {
        final long t13 = model.nextRedToGreen();
        final long t32 = model.nextGreenToYellow();
        final long t21 = model.nextYellowToRed();
        if (t13 < t32 && t13 < t21) {
          //System.out.println(nodeUri().toUri() + " signal phase " + p + " to state 3 in " + (Math.abs(Math.round(t13 - System.currentTimeMillis())) / 1000) + " seconds");
          signalPhaseEvents.put(p, new SignalPhaseEvent(t13, 3));
        } else if (t32 < t21 && t32 < t13) {
          //System.out.println(nodeUri().toUri() + " signal phase " + p + " to state 2 in " + (Math.abs(Math.round(t32 - System.currentTimeMillis())) / 1000) + " seconds");
          signalPhaseEvents.put(p, new SignalPhaseEvent(t32, 2));
        } else if (t21 < t13 && t21 < t32) {
          //System.out.println(nodeUri().toUri() + " signal phase " + p + " to state 1 in " + (Math.abs(Math.round(t21 - System.currentTimeMillis())) / 1000) + " seconds");
          signalPhaseEvents.put(p, new SignalPhaseEvent(t21, 1));
        }
      } else if (model.isUnpredictable()) {
        // enable learning
      }
    }
  }

  void didUpdateRemoteVehicleDetector(int d, int st, long clk) {
    //System.out.println(nodeUri().toUri() + " didUpdateRemoteVehicleDetector d: " + d + "; st: " + st);
    vehicleDetectorState.put(d, st);
  }

  void didUpdateRemotePedPhase(int pp, int st, long clk) {
    //System.out.println(nodeUri().toUri() + " didUpdateRemotePedPhase pp: " + pp + "; st: " + st);
    pedPhaseState.put(pp, st);
  }

  void didUpdateRemotePedCall(int pc, int st, long clk) {
    //System.out.println(nodeUri().toUri() + " didUpdateRemotePedCall pc: " + pc + "; st: " + st);
    pedCallState.put(pc, st);
  }

  public void linkLatency() {
    if (latencyLink == null) {
      latencyLink = downlink()
          .hostUri(TRAFFIC_HOST_URI)
          .nodeUri(Uri.EMPTY.withPath(nodeUri().getPath()))
          .laneUri("latency")
          .onEvent(this::didSetRemoteLatency)
          .keepSynced(true)
          .open();
    }
    //System.out.println("---------------nodeUri: " + Uri.EMPTY.withPath(nodeUri().getPath()));
  }

  public void unlinkLatency() {
    if (latencyLink != null) {
      latencyLink.close();
      latencyLink = null;
    }
  }

  void didSetRemoteLatency(Value newValue) {
    //System.out.println(nodeUri().toUri() + " didSetRemoteLatency: " + newValue.toRecon());
    latency.set(newValue);
  }

  @Override
  public void didStart() {
    System.out.println(nodeUri().toUri() + " didStart");
    linkInfo();
    linkSchematic();
    linkScan();
    linkLatency();
    initIntersectionTensor();
    sampleTimer = setTimer(SAMPLE_WINDOW, this::sampleIntersectionTensor);
  }

  @Override
  public void willStop() {
    unlinkScan();
    unlinkSchematic();
    unlinkInfo();
    unlinkLatency();
    if (sampleTimer != null) {
      sampleTimer.cancel();
      sampleTimer = null;
    }
  }

  static final String TRAFFIC_HOST = System.getProperty("trafficware.api.host",
      "swims://trafficware.swim.services?key=ab21cfe05ba-7d43-69b2-0aef-94d9d54b6f65");
  static final Uri TRAFFIC_HOST_URI = Uri.parse(TRAFFIC_HOST);
}