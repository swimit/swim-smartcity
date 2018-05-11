package it.swim.traffic.model;

import it.swim.traffic.tensor.TensorDims;
import it.swim.traffic.tensor.TensorForm;
import recon.Item;
import recon.Number;
import recon.Record;
import recon.Value;
import swim.util.HashTrieMap;
import swim.util.Murmur3;

public class IntersectionTensor {
  public HashTrieMap<Integer, SignalPhaseTensor> signalPhases;
  public HashTrieMap<Integer, VehicleDetectorTensor> vehicleDetectors;

  public IntersectionTensor(HashTrieMap<Integer, SignalPhaseTensor> signalPhases,
      HashTrieMap<Integer, VehicleDetectorTensor> vehicleDetectors) {
    this.signalPhases = signalPhases;
    this.vehicleDetectors = vehicleDetectors;
  }

  public IntersectionTensor() {
    this(HashTrieMap.empty(), HashTrieMap.empty());
  }

  public SignalPhaseTensor signalPhase(int id) {
    SignalPhaseTensor tensor = signalPhases.get(id);
    if (tensor == null) {
      tensor = new SignalPhaseTensor();
      signalPhases = signalPhases.updated(id, tensor);
    }
    return tensor;
  }

  public VehicleDetectorTensor vehicleDetector(int id) {
    VehicleDetectorTensor tensor = vehicleDetectors.get(id);
    if (tensor == null) {
      tensor = new VehicleDetectorTensor();
      vehicleDetectors = vehicleDetectors.updated(id, tensor);
    }
    return tensor;
  }

  public int tensorSize() {
    return signalPhases.size() * SignalPhaseTensor.TENSOR_SIZE +
           vehicleDetectors.size() * VehicleDetectorTensor.TENSOR_SIZE;
  }

  public void toTensor(double[] array, int offset) {
    for (SignalPhaseTensor signalTensor : signalPhases.values()) {
      array[offset++] = signalTensor.red;
      array[offset++] = signalTensor.yellow;
      array[offset++] = signalTensor.green;
    }
    for (VehicleDetectorTensor vehicleTensor : vehicleDetectors.values()) {
      array[offset++] = vehicleTensor.occupancy;
    }
  }

  public void fromTensor(double[] array, int offset) {
    for (SignalPhaseTensor signalTensor : signalPhases.values()) {
      signalTensor.red = array[offset++];
      signalTensor.yellow = array[offset++];
      signalTensor.green = array[offset++];
    }
    for (VehicleDetectorTensor vehicleTensor : vehicleDetectors.values()) {
      vehicleTensor.occupancy = array[offset++];
    }
  }

  public void fromTensor(float[] array, int offset) {
    for (SignalPhaseTensor signalTensor : signalPhases.values()) {
      signalTensor.red = (double)array[offset++];
      signalTensor.yellow = (double)array[offset++];
      signalTensor.green = (double)array[offset++];
    }
    for (VehicleDetectorTensor vehicleTensor : vehicleDetectors.values()) {
      vehicleTensor.occupancy = (double)array[offset++];
    }
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public IntersectionTensor clone() {
    HashTrieMap<Integer, SignalPhaseTensor> signalPhases = HashTrieMap.empty();
    for (HashTrieMap.Entry<Integer, SignalPhaseTensor> entry : this.signalPhases) {
      signalPhases = signalPhases.updated(entry.getKey(), entry.getValue().clone());
    }
    HashTrieMap<Integer, VehicleDetectorTensor> vehicleDetectors = HashTrieMap.empty();
    for (HashTrieMap.Entry<Integer, VehicleDetectorTensor> entry : this.vehicleDetectors) {
      vehicleDetectors = vehicleDetectors.updated(entry.getKey(), entry.getValue().clone());
    }
    return new IntersectionTensor(signalPhases, vehicleDetectors);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof IntersectionTensor) {
      final IntersectionTensor that = (IntersectionTensor)other;
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
    return "new" + ' ' + "IntersectionTensor" + '('
        + signalPhases + ", " + vehicleDetectors + ')';
  }

  public static final TensorForm<IntersectionTensor> FORM = new IntersectionTensorForm();
}

final class IntersectionTensorForm extends TensorForm<IntersectionTensor> {
  @Override
  public Class<?> getType() {
    return IntersectionTensor.class;
  }

  @Override
  public Value mold(IntersectionTensor tensor) {
    final Record signalPhases = Record.of();
    for (HashTrieMap.Entry<Integer, SignalPhaseTensor> entry : tensor.signalPhases) {
      signalPhases.slot(Number.valueOf(entry.getKey()), entry.getValue().toValue());
    }

    final Record vehicleDetectors = Record.of();
    for (HashTrieMap.Entry<Integer, VehicleDetectorTensor> entry : tensor.vehicleDetectors) {
      vehicleDetectors.slot(Number.valueOf(entry.getKey()), entry.getValue().toValue());
    }

    return Record.empty(2)
        .slot("signalPhases", signalPhases)
        .slot("vehicleDetectors", vehicleDetectors);
  }

  @Override
  public IntersectionTensor cast(Value value) {
    HashTrieMap<Integer, SignalPhaseTensor> signalPhases = HashTrieMap.empty();
    for (Item item : value.get("signalPhases")) {
      final int id = item.getKey().intValue();
      final SignalPhaseTensor tensor = item.getValue().coerce(SignalPhaseTensor.FORM);
      signalPhases = signalPhases.updated(id, tensor);
    }

    HashTrieMap<Integer, VehicleDetectorTensor> vehicleDetectors = HashTrieMap.empty();
    for (Item item : value.get("vehicleDetectors")) {
      final int id = item.getKey().intValue();
      final VehicleDetectorTensor tensor = item.getValue().coerce(VehicleDetectorTensor.FORM);
      vehicleDetectors = vehicleDetectors.updated(id, tensor);
    }
    return new IntersectionTensor(signalPhases, vehicleDetectors);
  }

  @Override
  public void toTensor(IntersectionTensor object, TensorDims dims, float[] tensor, int offset) {
    for (SignalPhaseTensor signalPhaseTensor : object.signalPhases.values()) {
      SignalPhaseTensor.FORM.toTensor(signalPhaseTensor, dims, tensor, offset);
      offset += SignalPhaseTensor.TENSOR_SIZE * dims.stride();
    }
    for (VehicleDetectorTensor vehicleDetectorTensor : object.vehicleDetectors.values()) {
      VehicleDetectorTensor.FORM.toTensor(vehicleDetectorTensor, dims, tensor, offset);
      offset += VehicleDetectorTensor.TENSOR_SIZE * dims.stride();
    }
  }

  @Override
  public void toTensor(IntersectionTensor object, TensorDims dims, double[] tensor, int offset) {
    for (SignalPhaseTensor signalPhaseTensor : object.signalPhases.values()) {
      SignalPhaseTensor.FORM.toTensor(signalPhaseTensor, dims, tensor, offset);
      offset += SignalPhaseTensor.TENSOR_SIZE * dims.stride();
    }
    for (VehicleDetectorTensor vehicleDetectorTensor : object.vehicleDetectors.values()) {
      VehicleDetectorTensor.FORM.toTensor(vehicleDetectorTensor, dims, tensor, offset);
      offset += VehicleDetectorTensor.TENSOR_SIZE * dims.stride();
    }
  }

  @Override
  public IntersectionTensor fromTensor(TensorDims dims, float[] tensor, int offset) {
    return null;
  }

  @Override
  public IntersectionTensor fromTensor(TensorDims dims, double[] tensor, int offset) {
    return null;
  }
}
